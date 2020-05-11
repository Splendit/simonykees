package eu.jsparrow.core.visitor.security;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
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

import eu.jsparrow.core.visitor.loop.DeclaredTypesASTVisitor;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;

public abstract class AbstractDynamicQueryASTVisitor extends AbstractAddImportASTVisitor {

	protected static final String EXECUTE = "execute"; //$NON-NLS-1$
	protected static final String EXECUTE_QUERY = "executeQuery"; //$NON-NLS-1$

	protected boolean containsTypeDeclarationWithName(CompilationUnit compilationUnit, String simpleTypeName) {
		DeclaredTypesASTVisitor visitor = new DeclaredTypesASTVisitor();
		compilationUnit.accept(visitor);
		return visitor.getAllTypes()
			.stream()
			.map(ITypeBinding::getName)
			.anyMatch(name -> name.equals(simpleTypeName));
	}

	protected boolean containsImport(List<ImportDeclaration> importDeclarations, String qualifiedTypeName) {
		return importDeclarations
			.stream()
			.map(ImportDeclaration::getName)
			.map(Name::getFullyQualifiedName)
			.anyMatch(qualifiedName -> qualifiedName.equals(qualifiedTypeName));
	}

	protected boolean isImportClashing(List<ImportDeclaration> importDeclarations, String simpleTypeName) {
		boolean clashing = importDeclarations.stream()
			.map(ImportDeclaration::getName)
			.filter(Name::isQualifiedName)
			.map(name -> (QualifiedName) name)
			.map(QualifiedName::getName)
			.map(SimpleName::getIdentifier)
			.anyMatch(simpleTypeName::equals);

		if (!clashing) {
			clashing = importDeclarations.stream()
				.map(ImportDeclaration::getName)
				.filter(Name::isSimpleName)
				.map(name -> (SimpleName) name)
				.map(SimpleName::getIdentifier)
				.anyMatch(simpleTypeName::equals);
		}
		return clashing;
	}

	private SqlVariableAnalyzerVisitor analyzeSqlVariableReferences(MethodInvocation methodInvocation) {
		SimpleName query = (SimpleName) methodInvocation.arguments()
			.get(0);
		IBinding queryVariableBinding = query.resolveBinding();
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

		SqlVariableAnalyzerVisitor sqlVariableVisitor = new SqlVariableAnalyzerVisitor(query, declaringNode,
				getCompilationUnit());
		Block enclosingBlock = ASTNodeUtil.getSpecificAncestor(declaringNode, Block.class);
		enclosingBlock.accept(sqlVariableVisitor);
		if (sqlVariableVisitor.isUnsafe()) {
			return null;
		}
		return sqlVariableVisitor;
	}

	private boolean analyzeStatementExecuteQuery(MethodInvocation methodInvocation) {
		SimpleName methodName = methodInvocation.getName();
		if (!EXECUTE.equals(methodName.getIdentifier()) && !EXECUTE_QUERY.equals(methodName.getIdentifier())) {
			return false;
		}

		Expression methodExpression = methodInvocation.getExpression();
		if (methodExpression == null) {
			return false;
		}

		IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
		ITypeBinding declaringClass = methodBinding.getDeclaringClass();
		if (!ClassRelationUtil.isContentOfType(declaringClass, java.sql.Statement.class.getName())) {
			return false;
		}

		List<Expression> arguments = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(), Expression.class);
		if (arguments.size() != 1) {
			return false;
		}

		Expression argument = arguments.get(0);
		ITypeBinding argumentTypeBinding = argument.resolveTypeBinding();
		boolean isString = ClassRelationUtil.isContentOfType(argumentTypeBinding, java.lang.String.class.getName());
		if (!isString) {
			return false;
		}

		if (argument.getNodeType() != ASTNode.SIMPLE_NAME) {
			return false;
		}

		if (EXECUTE.equals(methodName.getIdentifier())
				&& methodInvocation.getLocationInParent() != ExpressionStatement.EXPRESSION_PROPERTY) {
			return false;
		}

		ITypeBinding methodExpressionTypeBinding = methodExpression.resolveTypeBinding();
		return ClassRelationUtil.isContentOfType(methodExpressionTypeBinding, java.sql.Statement.class.getName());
	}

	protected SqlVariableAnalyzerVisitor createSqlVariableAnalyzerVisitor(MethodInvocation methodInvocation) {
		boolean hasRightTypeAndName = analyzeStatementExecuteQuery(methodInvocation);
		if (!hasRightTypeAndName) {
			return null;
		}

		SqlVariableAnalyzerVisitor sqlVariableVisitor = analyzeSqlVariableReferences(methodInvocation);
		if (sqlVariableVisitor == null || sqlVariableVisitor.isUnsafe()) {
			return null;
		}
		return sqlVariableVisitor;
	}

	protected String getSimpleName(String qualifiedName) {
		int lastIndexOfDot = qualifiedName.lastIndexOf('.');
		if (lastIndexOfDot == -1) {
			return qualifiedName;
		}
		return qualifiedName.substring(lastIndexOfDot + 1);
	}

	protected boolean isSafeToAddImport(CompilationUnit compilationUnit, String qualifiedTypeName) {

		String simpleTypeName = getSimpleName(qualifiedTypeName);

		if (containsTypeDeclarationWithName(compilationUnit, simpleTypeName)) {
			return false;
		}
		List<ImportDeclaration> importDeclarations = ASTNodeUtil.convertToTypedList(compilationUnit.imports(),
				ImportDeclaration.class);

		if (containsImport(importDeclarations, qualifiedTypeName)) {
			return true;
		}
		if (isImportClashing(importDeclarations, simpleTypeName)) {
			return false;
		}
		return importDeclarations.stream()
			.noneMatch(
					importDeclaration -> ClassRelationUtil.importsTypeOnDemand(importDeclaration, qualifiedTypeName));
	}
}
