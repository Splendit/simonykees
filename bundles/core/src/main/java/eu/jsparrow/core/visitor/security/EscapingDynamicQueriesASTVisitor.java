package eu.jsparrow.core.visitor.security;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import eu.jsparrow.core.visitor.loop.DeclaredTypesASTVisitor;
import eu.jsparrow.rules.common.builder.NodeBuilder;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;

/**
 * 
 * @since 3.17.0
 *
 */
public class EscapingDynamicQueriesASTVisitor extends AbstractAddImportASTVisitor {

	// Copy from UseParameterizedQueryASTVisitor
	private static final String EXECUTE = "execute"; //$NON-NLS-1$
	// Copy from UseParameterizedQueryASTVisitor
	private static final String EXECUTE_QUERY = "executeQuery"; //$NON-NLS-1$

	private static final List<String> IMPORTS_FOR_ESCAPE = Collections.unmodifiableList(Arrays.asList(
			"org.owasp.esapi.codecs.Codec", //$NON-NLS-1$
			"org.owasp.esapi.codecs.OracleCodec", //$NON-NLS-1$
			"org.owasp.esapi.ESAPI" //$NON-NLS-1$
	));

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		for (String qualifiedName : IMPORTS_FOR_ESCAPE) {
			if (!isSafeToAddImport(compilationUnit, qualifiedName)) {
				return false;
			}
		}
		return super.visit(compilationUnit);
	}

	@Override
	public boolean visit(MethodInvocation methodInvocation) {
		SimpleName queryAsMethodArgument = findQueryAsExecutionArgument(methodInvocation);

		if (queryAsMethodArgument == null) {
			return true;
		}

		VariableDeclarationFragment queryVariableDeclarationFragment = findQueryVariableDeclarationFragment(
				queryAsMethodArgument);
		if (queryVariableDeclarationFragment == null) {
			return true;
		}
		SqlVariableAnalyzerVisitor sqlVariableVisitor = new SqlVariableAnalyzerVisitor(queryAsMethodArgument,
				queryVariableDeclarationFragment,
				getCompilationUnit());
		Block enclosingBlock = ASTNodeUtil.getSpecificAncestor(queryVariableDeclarationFragment, Block.class);
		enclosingBlock.accept(sqlVariableVisitor);

		if (sqlVariableVisitor.isUnsafe()) {
			return true;
		}

		List<Expression> expressionsToEscape = analyzeQueryComponents(sqlVariableVisitor);
		if (expressionsToEscape.isEmpty()) {
			return true;
		}

		VariableDeclarationStatement queryVariableDeclarationStatement = ASTNodeUtil
			.getSpecificAncestor(queryVariableDeclarationFragment, VariableDeclarationStatement.class);
		if(queryVariableDeclarationStatement == null) {
			return true;
		}
		ListRewrite listRewrite = astRewrite.getListRewrite(enclosingBlock, Block.STATEMENTS_PROPERTY);
		int indexOfVariableDeclaration = enclosingBlock.statements()
			.indexOf(queryVariableDeclarationStatement);
		listRewrite.insertAt(createOracleCODECDeclarationStatement(), indexOfVariableDeclaration, null);

		for (Expression expression : expressionsToEscape) {
			astRewrite.replace(expression, createEscapeExpression(expression), null);
		}
		onRewrite();
		return true;
	}

	@SuppressWarnings("nls")
	private Expression createEscapeExpression(Expression expressionToEscape) {

		AST ast = astRewrite.getAST();
		MethodInvocation encoderInvocationOfESAPI = NodeBuilder.newMethodInvocation(ast, ast.newSimpleName("ESAPI"),
				"encoder");
		SimpleName encodeForSQLName = ast.newSimpleName("encodeForSQL");
		List<Expression> arguments = new ArrayList<>();
		arguments.add(ast.newSimpleName("ORACLE_CODEC"));
		arguments.add((Expression) astRewrite.createCopyTarget(expressionToEscape));
		return NodeBuilder.newMethodInvocation(ast, encoderInvocationOfESAPI, encodeForSQLName, arguments);
	}

	@SuppressWarnings("nls")
	private VariableDeclarationStatement createOracleCODECDeclarationStatement() {
		for (String qualifiedName : IMPORTS_FOR_ESCAPE) {
			addImports.add(qualifiedName);
		}
		AST ast = astRewrite.getAST();
		VariableDeclarationFragment fragment = ast.newVariableDeclarationFragment();
		fragment.setName(ast.newSimpleName("ORACLE_CODEC"));
		ClassInstanceCreation oracleCODECinitializer = ast.newClassInstanceCreation();
		oracleCODECinitializer.setType(ast.newSimpleType(ast.newSimpleName("OracleCodec")));
		fragment.setInitializer(oracleCODECinitializer);
		VariableDeclarationStatement oracleCODECDeclarationStatement = ast.newVariableDeclarationStatement(fragment);
		oracleCODECDeclarationStatement.setType(ast.newSimpleType(ast.newSimpleName("Codec")));
		return oracleCODECDeclarationStatement;
	}

	// Copy from UseParameterizedQueryASTVisitor
	private SimpleName findQueryAsExecutionArgument(MethodInvocation methodInvocation) {
		SimpleName methodName = methodInvocation.getName();
		if (!EXECUTE.equals(methodName.getIdentifier()) && !EXECUTE_QUERY.equals(methodName.getIdentifier())) {
			return null;
		}

		Expression methodExpression = methodInvocation.getExpression();
		if (methodExpression == null) {
			return null;
		}

		IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
		ITypeBinding declaringClass = methodBinding.getDeclaringClass();
		if (!ClassRelationUtil.isContentOfType(declaringClass, java.sql.Statement.class.getName())) {
			return null;
		}

		List<Expression> arguments = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(), Expression.class);
		if (arguments.size() != 1) {
			return null;
		}

		Expression argument = arguments.get(0);
		ITypeBinding argumentTypeBinding = argument.resolveTypeBinding();
		boolean isString = ClassRelationUtil.isContentOfType(argumentTypeBinding, java.lang.String.class.getName());
		if (!isString) {
			return null;
		}

		if (argument.getNodeType() != ASTNode.SIMPLE_NAME) {
			return null;
		}
		SimpleName simpleNameArgument = (SimpleName) argument;

		if (EXECUTE.equals(methodName.getIdentifier())
				&& methodInvocation.getLocationInParent() != ExpressionStatement.EXPRESSION_PROPERTY) {
			return null;
		}

		ITypeBinding methodExpressionTypeBinding = methodExpression.resolveTypeBinding();
		if (ClassRelationUtil.isContentOfType(methodExpressionTypeBinding, java.sql.Statement.class.getName())) {
			return simpleNameArgument;
		}
		return null;
	}

	private VariableDeclarationFragment findQueryVariableDeclarationFragment(SimpleName queryExecutionArgument) {

		IBinding queryVariableBinding = queryExecutionArgument.resolveBinding();
		if (queryVariableBinding.getKind() != IBinding.VARIABLE) {
			return null;
		}

		IVariableBinding variableBinding = (IVariableBinding) queryVariableBinding;
		if (variableBinding.isField()) {
			return null;
		}

		ASTNode declaringNode = this.getCompilationUnit()
			.findDeclaringNode(queryVariableBinding);
		if (declaringNode == null || declaringNode.getNodeType() != ASTNode.VARIABLE_DECLARATION_FRAGMENT) {
			return null;
		}
		return (VariableDeclarationFragment) declaringNode;

	}

	private List<Expression> analyzeQueryComponents(SqlVariableAnalyzerVisitor sqlVariableVisitor) {
		List<Expression> queryComponents = sqlVariableVisitor.getDynamicQueryComponents();
		QueryComponentsAnalyzerForEscaping componentsAnalyzer = new QueryComponentsAnalyzerForEscaping(queryComponents);
		componentsAnalyzer.analyze();
		return componentsAnalyzer.getExpressionsToEscape();
	}

	// copied:
	// UseParameterizedQueryASTVisitor#isSafeToAddImport(CompilationUnit, Class)
	// and modified it
	private boolean isSafeToAddImport(CompilationUnit compilationUnit, String qualifiedTypeName) {
		int lastIndexOfDot = qualifiedTypeName.lastIndexOf('.');
		String simpleTypeName = qualifiedTypeName.substring(lastIndexOfDot + 1);
		DeclaredTypesASTVisitor visitor = new DeclaredTypesASTVisitor();
		compilationUnit.accept(visitor);
		boolean matchesInnerClassName = visitor.getDeclaredTypes()
			.keySet()
			.stream()
			.anyMatch(name -> name.equals(simpleTypeName));
		if (matchesInnerClassName) {
			return false;
		}

		List<ImportDeclaration> importDeclarations = ASTNodeUtil.convertToTypedList(compilationUnit.imports(),
				ImportDeclaration.class);

		boolean importAlreadyExists = importDeclarations.stream()
			.map(ImportDeclaration::getName)
			.map(Name::getFullyQualifiedName)
			.anyMatch(qualifiedName -> qualifiedName.equals(qualifiedTypeName));
		if (importAlreadyExists) {
			return true;
		}

		boolean clashing = importDeclarations.stream()
			.map(ImportDeclaration::getName)
			.filter(Name::isQualifiedName)
			.map(name -> (QualifiedName) name)
			.map(QualifiedName::getName)
			.anyMatch(importedName -> simpleTypeName
				.equals(importedName.getIdentifier()));
		if (clashing) {
			return false;
		}

		clashing = importDeclarations.stream()
			.map(ImportDeclaration::getName)
			.filter(Name::isSimpleName)
			.anyMatch(name -> simpleTypeName
				.equals(((SimpleName) name).getIdentifier()));

		if (clashing) {
			return false;
		}

		return importDeclarations.stream()
			.noneMatch(
					importDeclaration -> ClassRelationUtil.importsTypeOnDemand(importDeclaration, qualifiedTypeName));
	}
}
