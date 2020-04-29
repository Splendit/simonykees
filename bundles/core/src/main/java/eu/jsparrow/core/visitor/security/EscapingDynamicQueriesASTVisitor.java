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
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import eu.jsparrow.core.visitor.loop.DeclaredTypesASTVisitor;
import eu.jsparrow.core.visitor.sub.LiveVariableScope;
import eu.jsparrow.rules.common.builder.NodeBuilder;
import eu.jsparrow.rules.common.util.ASTNodeUtil;

/**
 * 
 * @since 3.17.0
 *
 */
public class EscapingDynamicQueriesASTVisitor extends DynamicQueryASTVisitor {

	private final Map<Block, String> mapBlockToOracleCodecVariable = new HashMap<>();

	private static final String ORACLE_CODEC_NAME = "ORACLE_CODEC"; //$NON-NLS-1$

	private final LiveVariableScope liveVariableScope = new LiveVariableScope();

	private final Set<String> conflictingOracleCodecNames = new HashSet<>();

	public static final List<String> IMPORTS_FOR_ESCAPE = Collections.unmodifiableList(Arrays.asList(
			"org.owasp.esapi.codecs.Codec", //$NON-NLS-1$
			"org.owasp.esapi.codecs.OracleCodec", //$NON-NLS-1$
			"org.owasp.esapi.ESAPI" //$NON-NLS-1$
	));

	@Override
	public void endVisit(TypeDeclaration typeDeclaration) {
		liveVariableScope.clearLocalVariablesScope(typeDeclaration);
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

	@Override
	public boolean visit(CompilationUnit compilationUnit) {

		List<ImportDeclaration> importDeclarations = ASTNodeUtil.convertToTypedList(compilationUnit.imports(),
				ImportDeclaration.class);
		//
		importDeclarations.stream()
			.map(ImportDeclaration::getName)
			.filter(Name::isQualifiedName)
			.map(name -> (QualifiedName) name)
			.map(QualifiedName::getName)
			.map(SimpleName::getIdentifier)
			.filter(name -> name.startsWith(ORACLE_CODEC_NAME))
			.forEach(conflictingOracleCodecNames::add);

		importDeclarations.stream()
			.map(ImportDeclaration::getName)
			.filter(Name::isSimpleName)
			.map(name -> (SimpleName) name)
			.map(SimpleName::getIdentifier)
			.filter(name -> name.startsWith(ORACLE_CODEC_NAME))
			.forEach(conflictingOracleCodecNames::add);

		// oracleCodecNamesFromTypeDeclarations

		DeclaredTypesASTVisitor declaredTypesVisitor = new DeclaredTypesASTVisitor();
		compilationUnit.accept(declaredTypesVisitor);

		declaredTypesVisitor.getAllTypes()
			.stream()
			.map(ITypeBinding::getName)
			.filter(name -> name.startsWith(ORACLE_CODEC_NAME))
			.forEach(conflictingOracleCodecNames::add);
		;

		for (String qualifiedName : IMPORTS_FOR_ESCAPE) {
			if (!isSafeToAddImport(compilationUnit, qualifiedName)) {
				return false;
			}
		}
		return super.visit(compilationUnit);
	}

	@Override
	public void endVisit(CompilationUnit compilationUnit) {
		conflictingOracleCodecNames.clear();
		super.endVisit(compilationUnit);
	}

	@Override
	public boolean visit(MethodInvocation methodInvocation) {
		SqlVariableAnalyzerVisitor sqlVariableVisitor = createSqlVariableAnalyzerVisitor(methodInvocation);
		if (sqlVariableVisitor == null) {
			return true;
		}

		List<Expression> expressionsToEscape = analyzeQueryComponents(sqlVariableVisitor);
		if (expressionsToEscape.isEmpty()) {
			return true;
		}
		Expression expression0 = sqlVariableVisitor.getDynamicQueryComponents()
			.get(0);
		Statement statement = ASTNodeUtil.getSpecificAncestor(expression0, Statement.class);
		if (statement.getLocationInParent() != Block.STATEMENTS_PROPERTY) {
			return true;
		}
		Block enclosingBlock = (Block) statement.getParent();

		ASTNode enclosingScope = this.liveVariableScope.findEnclosingScope(statement)
			.orElse(null);
		liveVariableScope.lazyLoadScopeNames(enclosingScope);
		if (liveVariableScope.isInScope("ESAPI")) { //$NON-NLS-1$
			return true;
		}

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

	@SuppressWarnings("nls")
	private Expression createEscapeExpression(String oracleCodecName, Expression expressionToEscape) {
		AST ast = astRewrite.getAST();
		MethodInvocation encoderInvocationOfESAPI = NodeBuilder.newMethodInvocation(ast, ast.newSimpleName("ESAPI"),
				"encoder");
		SimpleName encodeForSQLName = ast.newSimpleName("encodeForSQL");
		List<Expression> arguments = new ArrayList<>();
		arguments.add(ast.newSimpleName(oracleCodecName));
		arguments.add((Expression) astRewrite.createCopyTarget(expressionToEscape));
		return NodeBuilder.newMethodInvocation(ast, encoderInvocationOfESAPI, encodeForSQLName, arguments);
	}

	private String createOracleCodecName() {
		String name = ORACLE_CODEC_NAME;
		int suffix = 1;
		while (liveVariableScope.isInScope(name) || conflictingOracleCodecNames.contains(name)) {
			name = ORACLE_CODEC_NAME + suffix;
			suffix++;
		}
		return name;
	}

	@SuppressWarnings("nls")
	private VariableDeclarationStatement createOracleCODECDeclarationStatement(String oracleCodecName) {
		for (String qualifiedName : IMPORTS_FOR_ESCAPE) {
			addImports.add(qualifiedName);
		}
		AST ast = astRewrite.getAST();

		VariableDeclarationFragment fragment = ast.newVariableDeclarationFragment();
		fragment.setName(ast.newSimpleName(oracleCodecName));
		ClassInstanceCreation oracleCODECinitializer = ast.newClassInstanceCreation();
		oracleCODECinitializer.setType(ast.newSimpleType(ast.newSimpleName("OracleCodec")));
		fragment.setInitializer(oracleCODECinitializer);
		VariableDeclarationStatement oracleCODECDeclarationStatement = ast.newVariableDeclarationStatement(fragment);

		SimpleType codecSimpleType = ast.newSimpleType(ast.newSimpleName("Codec"));
		ParameterizedType codecParameterizedType = ast.newParameterizedType(codecSimpleType);
		Type characterTypeArg = ast.newSimpleType(ast.newSimpleName(Character.class.getSimpleName()));
		@SuppressWarnings("unchecked")
		List<Type> typeArguments = codecParameterizedType.typeArguments();
		typeArguments.add(characterTypeArg);
		oracleCODECDeclarationStatement.setType(codecParameterizedType);

		return oracleCODECDeclarationStatement;
	}

	private List<Expression> analyzeQueryComponents(SqlVariableAnalyzerVisitor sqlVariableVisitor) {
		List<Expression> queryComponents = sqlVariableVisitor.getDynamicQueryComponents();
		QueryComponentsAnalyzerForEscaping componentsAnalyzer = new QueryComponentsAnalyzerForEscaping(queryComponents);
		componentsAnalyzer.analyze();
		return componentsAnalyzer.getExpressionsToEscape();
	}

}
