package eu.jsparrow.core.visitor.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;

import eu.jsparrow.core.visitor.sub.UnusedImportsVisitor;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;

/**
 * A visitor for replacing invocations of
 * {@link java.util.Arrays#asList(Object...)} with
 * {@link java.util.Collections#emptyList()} or
 * {@link java.util.Collections#singletonList(Object)} in case the number of
 * arguments is respectively 0 or 1.
 * 
 * @since 3.8.0
 */
public class UseCollectionsSingletonListASTVisitor extends AbstractAddImportASTVisitor {

	private static final String JAVA_UTIL_COLLECTIONS = java.util.Collections.class.getName();
	private static final String AS_LIST = "asList"; //$NON-NLS-1$
	private static final String SINGLETON_LIST = "singletonList"; //$NON-NLS-1$
	private static final String EMPTY_LIST = "emptyList"; //$NON-NLS-1$
	private static final String ARRAYS_FULLY_QUALIFIED_NAME = java.util.Arrays.class.getName();

	private List<ASTNode> replacedNames = new ArrayList<>();

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		boolean continueVisiting = super.visit(compilationUnit);
		if(continueVisiting) {
			verifyImport(compilationUnit, JAVA_UTIL_COLLECTIONS);
		}
		return continueVisiting;
	}

	@Override
	public void endVisit(CompilationUnit compilationUnit) {
		super.endVisit(compilationUnit);
		if (replacedNames.isEmpty()) {
			return;
		}
		List<ImportDeclaration> imports = ASTNodeUtil.convertToTypedList(compilationUnit.imports(),
				ImportDeclaration.class);
		List<ImportDeclaration> arraysImports = imports.stream()
			.filter(this::isArraysAsListImport)
			.filter(declaration -> !declaration.isOnDemand())
			.collect(Collectors.toList());

		arraysImports.forEach(arraysImport -> {
			UnusedImportsVisitor visitor = new UnusedImportsVisitor(arraysImport, replacedNames);
			compilationUnit.accept(visitor);
			if (!visitor.isUsageFound()) {
				astRewrite.remove(arraysImport, null);
			}
		});
		replacedNames.clear();
	}

	private boolean isArraysAsListImport(ImportDeclaration declaration) {
		Name name = declaration.getName();
		return ARRAYS_FULLY_QUALIFIED_NAME.equals(name.getFullyQualifiedName())
				|| (ARRAYS_FULLY_QUALIFIED_NAME + ".asList").equals(name.getFullyQualifiedName()); //$NON-NLS-1$
	}

	@Override
	public boolean visit(MethodInvocation methodInvocation) {
		SimpleName methodName = methodInvocation.getName();
		String methodIdentifier = methodName.getIdentifier();
		if (!AS_LIST.equals(methodIdentifier)) {
			return true;
		}
		List<Expression> arguments = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(), Expression.class);
		if (arguments.size() > 1) {
			return true;
		}
		IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
		if (methodBinding == null) {
			return true;
		}

		ITypeBinding declaringClass = methodBinding.getDeclaringClass();
		if (!ClassRelationUtil.isContentOfType(declaringClass, ARRAYS_FULLY_QUALIFIED_NAME)) {
			return true;
		}

		if (arguments.size() == 1) {
			Expression argument = arguments.get(0);
			ITypeBinding argumentType = argument.resolveTypeBinding();
			if (argumentType == null || argumentType.isArray()) {
				return false;
			}
		}

		AST ast = astRewrite.getAST();
		String newMethodIdentifier = arguments.isEmpty() ? EMPTY_LIST : SINGLETON_LIST;
		SimpleName newMethodName = ast.newSimpleName(newMethodIdentifier);

		Expression originalExpression = methodInvocation.getExpression();
		Name newExpressionName = addImport(JAVA_UTIL_COLLECTIONS);
		if (originalExpression != null) {
			astRewrite.replace(originalExpression, newExpressionName, null);
			Expression simpleName = originalExpression.getNodeType() == ASTNode.QUALIFIED_NAME
					? ((QualifiedName) originalExpression).getName()
					: originalExpression;
			replacedNames.add(simpleName);
		} else {
			astRewrite.set(methodInvocation, MethodInvocation.EXPRESSION_PROPERTY, newExpressionName, null);
		}
		astRewrite.replace(methodName, newMethodName, null);
		replacedNames.add(methodName);
		onRewrite();

		return true;
	}
}