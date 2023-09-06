package eu.jsparrow.core.visitor.security;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import eu.jsparrow.core.visitor.sub.VariableDeclarationsUtil;
import eu.jsparrow.rules.common.builder.NodeBuilder;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.visitor.helper.LiveVariableScope;

/**
 * Used for preventing injection of SQL code by escaping of the user input that
 * may contain SQL code coming form an attack and changing the intent of a
 * query.
 * <p>
 * The visitor looks for variables which store SQL queries. Then it analyzes the
 * concatenation of the query string for user input. Each user input of the type
 * String is wrapped by an invocation of
 * {@code ESAPI.encoder().encodeForSql(...)}.
 * <p>
 * Example:
 * <p>
 * {@code String query = "SELECT * FROM employee WHERE id ='" + id + "' ORDER BY last_name"; }
 * <p>
 * is transformed to
 * <p>
 * {@code Codec<Character> oracleCodec = new OracleCodec();} <br>
 * {@code String query = "SELECT * FROM employee WHERE id ='" + ESAPI.encoder().encodeForSQL(oracleCodec, id) + "' ORDER BY last_name";}
 * <p>
 * 
 * @since 3.17.0
 * 
 *
 */
public class EscapeUserInputsInSQLQueriesASTVisitor extends AbstractDynamicQueryASTVisitor {

	public static final String QUALIFIED_NAME_CODEC = "org.owasp.esapi.codecs.Codec"; //$NON-NLS-1$
	public static final String QUALIFIED_NAME_ORACLE_CODEC = "org.owasp.esapi.codecs.OracleCodec"; //$NON-NLS-1$
	public static final String QUALIFIED_NAME_ESAPI = "org.owasp.esapi.ESAPI"; //$NON-NLS-1$
	private static final String VAR_NAME_ORACLE_CODEC = "oracleCodec"; //$NON-NLS-1$

	private final Map<Block, String> mapBlockToOracleCodecVariable = new HashMap<>();
	private final LiveVariableScope liveVariableScope = new LiveVariableScope();

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		boolean continueVisiting = super.visit(compilationUnit);
		if (continueVisiting) {
			verifyImport(compilationUnit, QUALIFIED_NAME_CODEC);
			verifyImport(compilationUnit, QUALIFIED_NAME_ORACLE_CODEC);
			verifyImport(compilationUnit, QUALIFIED_NAME_ESAPI);
		}
		return continueVisiting;
	}

	@Override
	public boolean visit(MethodInvocation methodInvocation) {
		Expression queryMethodArgument = analyzeStatementExecuteQuery(methodInvocation);
		if (queryMethodArgument == null) {
			return true;
		}
		List<Expression> dynamicQueryComponents = findDynamicQueryComponents(queryMethodArgument);
		List<Expression> expressionsToEscape = new QueryComponentsAnalyzerForEscaping(dynamicQueryComponents)
			.createListOfExpressionsToEscape();
		if (expressionsToEscape.isEmpty()) {
			return true;
		}

		Statement statementAfterOracleCodec;
		if (queryMethodArgument.getNodeType() == ASTNode.SIMPLE_NAME) {
			statementAfterOracleCodec = VariableDeclarationsUtil
				.findLocalVariableDeclarationStatement((SimpleName) queryMethodArgument);
		} else {
			statementAfterOracleCodec = ASTNodeUtil.getSpecificAncestor(queryMethodArgument, Statement.class);
		}

		if (statementAfterOracleCodec == null) {
			return true;
		}

		if (statementAfterOracleCodec.getLocationInParent() != Block.STATEMENTS_PROPERTY) {
			return true;
		}

		Block enclosingBlock = (Block) statementAfterOracleCodec.getParent();
		ASTNode enclosingScope = liveVariableScope.findEnclosingScope(statementAfterOracleCodec)
			.orElse(null);
		if (enclosingScope == null) {
			return true;
		}

		liveVariableScope.lazyLoadScopeNames(enclosingScope);

		String oracleCodecName = mapBlockToOracleCodecVariable.computeIfAbsent(enclosingBlock, block -> {
			String newName = createOracleCodecName();
			liveVariableScope.addName(enclosingScope, newName);
			VariableDeclarationStatement oracleCodecDeclarationStatement = createOracleCODECDeclarationStatement(
					newName, methodInvocation);
			ListRewrite listRewrite = astRewrite.getListRewrite(enclosingBlock, Block.STATEMENTS_PROPERTY);
			listRewrite.insertBefore(oracleCodecDeclarationStatement, statementAfterOracleCodec, null);
			return newName;
		});

		for (Expression expressionToEscape : expressionsToEscape) {
			astRewrite.replace(expressionToEscape, createEscapeExpression(oracleCodecName, expressionToEscape), null);
		}
		onRewrite();
		return true;
	}
	
	@Override
	public void endVisit(CompilationUnit compilationUnit) {
		liveVariableScope.clearCompilationUnitScope(compilationUnit);
		super.endVisit(compilationUnit);
	}

	@Override
	public void endVisit(TypeDeclaration typeDeclaration) {
		liveVariableScope.clearFieldScope(typeDeclaration);
		mapBlockToOracleCodecVariable.clear();
		super.endVisit(typeDeclaration);
	}

	@Override
	public void endVisit(MethodDeclaration methodDeclaration) {
		liveVariableScope.clearLocalVariablesScope(methodDeclaration);
		mapBlockToOracleCodecVariable.clear();
		super.endVisit(methodDeclaration);
	}

	@Override
	public void endVisit(FieldDeclaration fieldDeclaration) {
		liveVariableScope.clearLocalVariablesScope(fieldDeclaration);
		mapBlockToOracleCodecVariable.clear();
		super.endVisit(fieldDeclaration);
	}

	@Override
	public void endVisit(Initializer initializer) {
		mapBlockToOracleCodecVariable.clear();
		super.endVisit(initializer);
	}

	private DynamicQueryComponentsStore collectQueryComponents(SimpleName queryVariableName) {
		VariableDeclarationFragment variableDeclarationFragment = VariableDeclarationsUtil
			.findVariableDeclarationFragment(queryVariableName);
		if (variableDeclarationFragment == null) {
			return null;
		}
		Block blockAroundLocalDeclarationFragment = VariableDeclarationsUtil
			.findBlockSurroundingDeclaration(variableDeclarationFragment).orElse(null);
		if (blockAroundLocalDeclarationFragment == null) {
			return null;
		}

		SqlVariableAnalyzerVisitor visitor = new SqlVariableAnalyzerVisitor(variableDeclarationFragment);
		blockAroundLocalDeclarationFragment.accept(visitor);
		List<SimpleName> variableReferences = visitor.getVariableReferences();
		int lastIndex = variableReferences.size() - 1;
		if (variableReferences.get(lastIndex) != queryVariableName) {
			return null;
		}

		DynamicQueryComponentsStore componentStore = new DynamicQueryComponentsStore();
		Expression initializer = VariableDeclarationsUtil.findInitializer(variableDeclarationFragment);
		if (initializer != null) {
			componentStore.storeComponents(initializer);
		}

		for (SimpleName simpleName : variableReferences) {
			if (simpleName.getLocationInParent() == Assignment.LEFT_HAND_SIDE_PROPERTY) {
				Assignment assignment = (Assignment) simpleName.getParent();
				componentStore.storeComponents(assignment.getRightHandSide());
			} else if (simpleName != queryVariableName) {
				return null;
			}
		}
		return componentStore;
	}

	@Override
	protected List<Expression> findDynamicQueryComponents(Expression queryExpression) {
		if (queryExpression.getNodeType() == ASTNode.SIMPLE_NAME) {
			DynamicQueryComponentsStore componentStore = collectQueryComponents((SimpleName) queryExpression);
			if (componentStore != null) {
				return componentStore.getComponents();
			}
			return Collections.emptyList();
		}
		return super.findDynamicQueryComponents(queryExpression);
	}

	@SuppressWarnings("nls")
	private Expression createEscapeExpression(String oracleCodecName, Expression expressionToEscape) {
		AST ast = astRewrite.getAST();

		Name nameESAPI = addImport(QUALIFIED_NAME_ESAPI, expressionToEscape);
		MethodInvocation encoderInvocationOfESAPI = NodeBuilder.newMethodInvocation(ast, nameESAPI, "encoder");
		SimpleName encodeForSQLName = ast.newSimpleName("encodeForSQL");
		List<Expression> arguments = new ArrayList<>();
		arguments.add(ast.newSimpleName(oracleCodecName));
		arguments.add((Expression) astRewrite.createCopyTarget(expressionToEscape));
		return NodeBuilder.newMethodInvocation(ast, encoderInvocationOfESAPI, encodeForSQLName, arguments);
	}

	private String createOracleCodecName() {
		String name = VAR_NAME_ORACLE_CODEC;
		int suffix = 1;
		while (this.liveVariableScope.isInScope(name)) {
			name = VAR_NAME_ORACLE_CODEC + suffix;
			suffix++;
		}
		return name;
	}

	private VariableDeclarationStatement createOracleCODECDeclarationStatement(String oracleCodecName,
			ASTNode context) {
		AST ast = astRewrite.getAST();

		VariableDeclarationFragment fragment = ast.newVariableDeclarationFragment();
		fragment.setName(ast.newSimpleName(oracleCodecName));
		ClassInstanceCreation oracleCODECinitializer = ast.newClassInstanceCreation();

		Name oracleCodecTypeName = addImport(QUALIFIED_NAME_ORACLE_CODEC, context);
		SimpleType oracleCodecType = ast.newSimpleType(oracleCodecTypeName);
		oracleCODECinitializer.setType(oracleCodecType);
		fragment.setInitializer(oracleCODECinitializer);
		VariableDeclarationStatement oracleCODECDeclarationStatement = ast.newVariableDeclarationStatement(fragment);

		Name codecTypeName = addImport(QUALIFIED_NAME_CODEC, context);
		SimpleType codecSimpleType = ast.newSimpleType(codecTypeName);
		ParameterizedType codecParameterizedType = ast.newParameterizedType(codecSimpleType);
		Type characterTypeArg = ast.newSimpleType(ast.newSimpleName(Character.class.getSimpleName()));
		@SuppressWarnings("unchecked")
		List<Type> typeArguments = codecParameterizedType.typeArguments();
		typeArguments.add(characterTypeArg);
		oracleCODECDeclarationStatement.setType(codecParameterizedType);

		return oracleCODECDeclarationStatement;
	}
}
