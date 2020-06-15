package eu.jsparrow.core.visitor.security;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
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

import eu.jsparrow.core.visitor.sub.LiveVariableScope;
import eu.jsparrow.rules.common.builder.NodeBuilder;
import eu.jsparrow.rules.common.util.ASTNodeUtil;

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

	private static final String QUALIFIED_NAME_CODEC = "org.owasp.esapi.codecs.Codec"; //$NON-NLS-1$
	private static final String QUALIFIED_NAME_ORACLE_CODEC = "org.owasp.esapi.codecs.OracleCodec"; //$NON-NLS-1$
	private static final String QUALIFIED_NAME_ESAPI = "org.owasp.esapi.ESAPI"; //$NON-NLS-1$
	private static final String VAR_NAME_ORACLE_CODEC = "oracleCodec"; //$NON-NLS-1$
	public static final List<String> CODEC_TYPES_QUALIFIED_NAMES = Collections.unmodifiableList(Arrays.asList(
			QUALIFIED_NAME_CODEC,
			QUALIFIED_NAME_ORACLE_CODEC,
			QUALIFIED_NAME_ESAPI));
	private final Map<Block, String> mapBlockToOracleCodecVariable = new HashMap<>();
	private final LiveVariableScope liveVariableScope = new LiveVariableScope();
	private final Set<String> codecTypesAbleToBeImported = new HashSet<>();

	@Override
	public boolean visit(CompilationUnit compilationUnit) {

		for (String fullyQuallifiedClassName : CODEC_TYPES_QUALIFIED_NAMES) {
			if (isSafeToAddImport(compilationUnit, fullyQuallifiedClassName)) {
				codecTypesAbleToBeImported.add(fullyQuallifiedClassName);
			}
		}
		return super.visit(compilationUnit);
	}

	@Override
	public boolean visit(MethodInvocation methodInvocation) {
		Expression queryMethodArgument = analyzeStatementExecuteQuery(methodInvocation);
		SqlVariableAnalyzerVisitor sqlVariableVisitor = createSqlVariableAnalyzerVisitor(queryMethodArgument);
		if (sqlVariableVisitor == null) {
			return true;
		}

		List<Expression> queryComponents = sqlVariableVisitor.getDynamicQueryComponents();
		List<Expression> expressionsToEscape = new QueryComponentsAnalyzerForEscaping(queryComponents).createListOfExpressionsToEscape();
		if (expressionsToEscape.isEmpty()) {
			return true;
		}
		Expression expression = sqlVariableVisitor.getDynamicQueryComponents()
			.get(0);
		Statement statement = ASTNodeUtil.getSpecificAncestor(expression, Statement.class);
		if (statement.getLocationInParent() != Block.STATEMENTS_PROPERTY) {
			return true;
		}
		Block enclosingBlock = (Block) statement.getParent();

		ASTNode enclosingScope = this.liveVariableScope.findEnclosingScope(statement)
			.orElse(null);
		if (enclosingScope == null) {
			return true;
		}

		liveVariableScope.lazyLoadScopeNames(enclosingScope);

		String oracleCodecName = mapBlockToOracleCodecVariable.computeIfAbsent(enclosingBlock, block -> {
			String newName = createOracleCodecName();
			liveVariableScope.addName(enclosingScope, newName);
			VariableDeclarationStatement oracleCodecDeclarationStatement = createOracleCODECDeclarationStatement(
					newName);
			ListRewrite listRewrite = astRewrite.getListRewrite(enclosingBlock, Block.STATEMENTS_PROPERTY);
			listRewrite.insertBefore(oracleCodecDeclarationStatement, statement, null);
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
		codecTypesAbleToBeImported.clear();
		super.endVisit(compilationUnit);
	}

	@Override
	public void endVisit(TypeDeclaration typeDeclaration) {
		liveVariableScope.clearFieldScope(typeDeclaration);
		mapBlockToOracleCodecVariable.clear();
	}

	@Override
	public void endVisit(MethodDeclaration methodDeclaration) {
		liveVariableScope.clearLocalVariablesScope(methodDeclaration);
		mapBlockToOracleCodecVariable.clear();
	}

	@Override
	public void endVisit(FieldDeclaration fieldDeclaration) {
		liveVariableScope.clearLocalVariablesScope(fieldDeclaration);
		mapBlockToOracleCodecVariable.clear();
	}

	@Override
	public void endVisit(Initializer initializer) {
		liveVariableScope.clearLocalVariablesScope(initializer);
		mapBlockToOracleCodecVariable.clear();
	}

	private Name createTypeName(String qualifiedName) {
		AST ast = astRewrite.getAST();
		String simpleName = getSimpleName(qualifiedName);
		if (codecTypesAbleToBeImported.contains(qualifiedName)) {
			addImports.add(qualifiedName);
			return ast.newSimpleName(simpleName);
		}
		return ast.newName(qualifiedName);
	}

	@SuppressWarnings("nls")
	private Expression createEscapeExpression(String oracleCodecName, Expression expressionToEscape) {
		AST ast = astRewrite.getAST();
		Name nameESAPI;
		String simpleNameESAPI = "ESAPI";
		if (codecTypesAbleToBeImported.contains(QUALIFIED_NAME_ESAPI)
				&& !liveVariableScope.isInScope(simpleNameESAPI)) {
			addImports.add(QUALIFIED_NAME_ESAPI);
			nameESAPI = ast.newSimpleName(simpleNameESAPI);
		} else {
			nameESAPI = ast.newName(QUALIFIED_NAME_ESAPI);
		}
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
		while (liveVariableScope.isInScope(name)) {
			name = VAR_NAME_ORACLE_CODEC + suffix;
			suffix++;
		}
		return name;
	}

	private VariableDeclarationStatement createOracleCODECDeclarationStatement(String oracleCodecName) {
		AST ast = astRewrite.getAST();

		VariableDeclarationFragment fragment = ast.newVariableDeclarationFragment();
		fragment.setName(ast.newSimpleName(oracleCodecName));
		ClassInstanceCreation oracleCODECinitializer = ast.newClassInstanceCreation();

		oracleCODECinitializer.setType(ast.newSimpleType(createTypeName(QUALIFIED_NAME_ORACLE_CODEC)));
		fragment.setInitializer(oracleCODECinitializer);
		VariableDeclarationStatement oracleCODECDeclarationStatement = ast.newVariableDeclarationStatement(fragment);

		SimpleType codecSimpleType = ast.newSimpleType(createTypeName(QUALIFIED_NAME_CODEC));
		ParameterizedType codecParameterizedType = ast.newParameterizedType(codecSimpleType);
		Type characterTypeArg = ast.newSimpleType(ast.newSimpleName(Character.class.getSimpleName()));
		@SuppressWarnings("unchecked")
		List<Type> typeArguments = codecParameterizedType.typeArguments();
		typeArguments.add(characterTypeArg);
		oracleCODECDeclarationStatement.setType(codecParameterizedType);

		return oracleCODECDeclarationStatement;
	}
}
