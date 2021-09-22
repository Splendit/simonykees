package eu.jsparrow.core.visitor.impl;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * This visitor looks for {@link MethodInvocation} nodes which represent
 * invocations of the method {@code Stream#collect(Collector)} and replaces them
 * by invocations of the Java 16 method {@code Stream#toList()}
 * <p>
 * Example:
 * 
 * <pre>
 * collection
 * 	.stream()
 * 	.map(function)
 * 	.filter(predicate)
 * 	.collect(Collectors.toUnmodifiableList());
 * </pre>
 * 
 * is transformed to
 * 
 * <pre>
 * collection
 * 	.stream()
 * 	.map(function)
 * 	.filter(predicate)
 * 	.toList();
 * </pre>
 * 
 * @since 4.4.0
 * 
 */
public class ReplaceStreamCollectByToListASTVisitor extends AbstractASTRewriteASTVisitor {

	private static final String TO_LIST = "toList"; //$NON-NLS-1$
	private static final String TO_UNMODIFIABLE_LIST = "toUnmodifiableList"; //$NON-NLS-1$

	/**
	 * This is a prototype with almost no validation. The only condition of
	 * validation is that the method invocation must have the method name
	 * "collect".
	 */
	@Override
	public boolean visit(MethodInvocation invocation) {
		Expression invocationExpression = invocation.getExpression();
		if (invocationExpression != null && isTransformableStreamCollectInvocation(invocation)) {
			Expression streamToListInvocationExpression = (Expression) astRewrite
				.createCopyTarget(invocationExpression);
			AST ast = astRewrite.getAST();
			MethodInvocation streamToListInvocation = ast.newMethodInvocation();
			streamToListInvocation.setName(ast.newSimpleName(TO_LIST)); // $NON-NLS-1$
			streamToListInvocation.setExpression(streamToListInvocationExpression);
			astRewrite.replace(invocation, streamToListInvocation, null);
			return false;
		}
		return true;
	}

	private boolean isTransformableStreamCollectInvocation(MethodInvocation collectInvocation) {
		if (!isStreamCollectInvocation(collectInvocation)) {
			return false;
		}

		String collectorsInvocationMethodName = ASTNodeUtil
			.convertToTypedList(collectInvocation.arguments(), Expression.class)
			.stream()
			.filter(MethodInvocation.class::isInstance)
			.map(MethodInvocation.class::cast)
			.filter(this::isSupportedCollectorsMethodInvocation)
			.map(MethodInvocation::getName)
			.map(SimpleName::getIdentifier)
			.findFirst()
			.orElse(null);

		if (collectorsInvocationMethodName == null) {
			return false;
		}

		if (TO_UNMODIFIABLE_LIST.equals(collectorsInvocationMethodName)) { // $NON-NLS-1$
			return true;
		}

		return analyzeStreamCollectInvocationWithCollectorsToList(collectInvocation);

	}

	private boolean isStreamCollectInvocation(MethodInvocation invocation) {
		if (!"collect".equals(invocation.getName() //$NON-NLS-1$
			.getIdentifier())) {
			return false;
		}

		IMethodBinding methodBinding = invocation.resolveMethodBinding();
		if (methodBinding == null) {
			return false;
		}
		ITypeBinding declaringClass = methodBinding.getDeclaringClass();
		if (!ClassRelationUtil.isContentOfType(declaringClass, "java.util.stream.Stream")) { //$NON-NLS-1$
			return false;
		}

		ITypeBinding[] parameterTypes = methodBinding.getMethodDeclaration()
			.getParameterTypes();
		if (parameterTypes.length != 1) {
			return false;
		}
		return ClassRelationUtil.isContentOfType(parameterTypes[0], "java.util.stream.Collector"); //$NON-NLS-1$
	}

	private boolean isSupportedCollectorsMethodInvocation(MethodInvocation invocation) {
		String identifier = invocation.getName()
			.getIdentifier();

		if (!identifier.equals(TO_LIST) &&
				!identifier.equals(TO_UNMODIFIABLE_LIST)) {
			return false;
		}

		IMethodBinding methodBinding = invocation.resolveMethodBinding();
		if (methodBinding == null) {
			return false;
		}
		ITypeBinding declaringClass = methodBinding.getDeclaringClass();
		return ClassRelationUtil.isContentOfType(declaringClass, "java.util.stream.Collectors"); //$NON-NLS-1$
	}

	private boolean analyzeStreamCollectInvocationWithCollectorsToList(MethodInvocation collectInvocation) {

		if (collectInvocation.getLocationInParent() == MethodInvocation.ARGUMENTS_PROPERTY) {
			MethodInvocation parentMethodInvocation = (MethodInvocation) collectInvocation.getParent();
			if (!"unmodifiableList".equals(parentMethodInvocation.getName() //$NON-NLS-1$
				.getIdentifier())) {
				return false;
			}
			IMethodBinding methodBinding = parentMethodInvocation.resolveMethodBinding();
			if (methodBinding == null) {
				return false;
			}
			ITypeBinding declaringClass = methodBinding.getDeclaringClass();
			return ClassRelationUtil.isContentOfType(declaringClass, "java.util.Collections"); //$NON-NLS-1$

		}

		VariableDeclarationFragment variableDeclarationFragment = null;
		if (collectInvocation.getLocationInParent() == Assignment.RIGHT_HAND_SIDE_PROPERTY) {
			Expression leftHandSide = ((Assignment) collectInvocation.getParent()).getLeftHandSide();
			if (leftHandSide.getNodeType() != ASTNode.SIMPLE_NAME) {
				return false;
			}
			SimpleName simpleName = (SimpleName) leftHandSide;
			ASTNode declaringNode = getCompilationUnit().findDeclaringNode(simpleName.resolveBinding());
			if (declaringNode != null && declaringNode.getNodeType() == ASTNode.VARIABLE_DECLARATION_FRAGMENT) {
				variableDeclarationFragment = (VariableDeclarationFragment) declaringNode;
			}
		} else if (collectInvocation.getLocationInParent() == VariableDeclarationFragment.INITIALIZER_PROPERTY) {
			variableDeclarationFragment = (VariableDeclarationFragment) collectInvocation
				.getParent();
		}

		if (variableDeclarationFragment == null ||
				variableDeclarationFragment.getLocationInParent() != VariableDeclarationStatement.FRAGMENTS_PROPERTY) {
			return false;
		}

		ASTNode scopeOfVariableUsage = variableDeclarationFragment.getParent()
			.getParent();
		return analyzeLocalVariableUsage(variableDeclarationFragment.getName(), scopeOfVariableUsage);

	}

	private boolean analyzeLocalVariableUsage(SimpleName name, ASTNode scopeOfVariableUsage) {
		return true;
	}

}