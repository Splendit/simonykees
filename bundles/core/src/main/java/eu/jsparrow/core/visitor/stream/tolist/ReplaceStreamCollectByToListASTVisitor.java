package eu.jsparrow.core.visitor.stream.tolist;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;
import eu.jsparrow.rules.common.visitor.helper.LocalVariableUsagesVisitor;

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
	@SuppressWarnings("nls")
	private static final List<String> NOT_MODIFYING_LIST_METHOD_NAMES = Collections.unmodifiableList(Arrays.asList(
			"forEach", //
			"size", "isEmpty", "contains", "toArray", "containsAll", "equals", "hashCode", "stream", "parallelStream", //
			"indexOf", "lastIndexOf"));

	@Override
	public boolean visit(MethodInvocation node) {

		MethodInvocation supportedStreamCollectInvocation = findSupportedStreamCollectInvocation(node)
			.orElse(null);

		if (supportedStreamCollectInvocation != null) {
			AST ast = astRewrite.getAST();
			MethodInvocation streamToListInvocation = ast.newMethodInvocation();
			streamToListInvocation.setName(ast.newSimpleName(TO_LIST)); // $NON-NLS-1$

			Expression invocationExpression = supportedStreamCollectInvocation.getExpression();
			if (invocationExpression != null) {
				Expression streamToListInvocationExpression = (Expression) astRewrite
					.createCopyTarget(invocationExpression);
				streamToListInvocation.setExpression(streamToListInvocationExpression);
			}
			astRewrite.replace(supportedStreamCollectInvocation, streamToListInvocation, null);
			return false;
		}
		return true;
	}

	Optional<MethodInvocation> findSupportedStreamCollectInvocation(MethodInvocation invocation) {

		if (!isSupportedCollectorsMethod(invocation)) {
			return Optional.empty();
		}

		if (invocation.getLocationInParent() != MethodInvocation.ARGUMENTS_PROPERTY) {
			return Optional.empty();
		}

		MethodInvocation parentInvocation = (MethodInvocation) invocation.getParent();
		if (!isStreamCollectInvocation(parentInvocation)) {
			return Optional.empty();
		}

		boolean collectorsToUnmodiifiableList = invocation.getName()
			.getIdentifier()
			.equals(TO_UNMODIFIABLE_LIST);

		if (collectorsToUnmodiifiableList ||
				analyzeInvocationUsingCollectorsToList(parentInvocation)) {
			return Optional.of(parentInvocation);
		}

		return Optional.empty();
	}

	private boolean isSupportedCollectorsMethod(MethodInvocation invocation) {
		String identifier = invocation.getName()
			.getIdentifier();

		if (!identifier.equals(TO_UNMODIFIABLE_LIST) && !identifier.equals(TO_LIST)) {
			return false;
		}

		if (!invocation.arguments()
			.isEmpty()) {
			return false;
		}

		IMethodBinding methodBinding = invocation.resolveMethodBinding();
		if (methodBinding == null) {
			return false;
		}
		ITypeBinding declaringClass = methodBinding.getDeclaringClass();
		return ClassRelationUtil.isContentOfType(declaringClass, "java.util.stream.Collectors"); //$NON-NLS-1$

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

	private boolean analyzeInvocationUsingCollectorsToList(MethodInvocation collectInvocation) {

		if (collectInvocation.getLocationInParent() == MethodInvocation.ARGUMENTS_PROPERTY) {
			return isCollectionsUnmodifiableListInvocation((MethodInvocation) collectInvocation.getParent());
		}

		if (collectInvocation.getLocationInParent() == VariableDeclarationFragment.INITIALIZER_PROPERTY) {
			VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) collectInvocation
				.getParent();
			return isDeclaringEffectivelyImmutableLocalVariable(variableDeclarationFragment);
		}

		if (collectInvocation.getLocationInParent() == Assignment.RIGHT_HAND_SIDE_PROPERTY) {
			Expression leftHandSide = ((Assignment) collectInvocation.getParent()).getLeftHandSide();
			if (leftHandSide.getNodeType() == ASTNode.SIMPLE_NAME) {
				SimpleName simpleName = (SimpleName) leftHandSide;
				ASTNode declaringNode = getCompilationUnit().findDeclaringNode(simpleName.resolveBinding());
				if (declaringNode != null && declaringNode.getNodeType() == ASTNode.VARIABLE_DECLARATION_FRAGMENT) {
					return isDeclaringEffectivelyImmutableLocalVariable((VariableDeclarationFragment) declaringNode);
				}
			}
		}
		return false;
	}

	private boolean isCollectionsUnmodifiableListInvocation(MethodInvocation invocation) {
		if (!"unmodifiableList".equals(invocation.getName() //$NON-NLS-1$
			.getIdentifier())) {
			return false;
		}
		IMethodBinding methodBinding = invocation.resolveMethodBinding();
		if (methodBinding == null) {
			return false;
		}
		ITypeBinding declaringClass = methodBinding.getDeclaringClass();

		if (!ClassRelationUtil.isContentOfType(declaringClass, "java.util.Collections")) { //$NON-NLS-1$
			return false;
		}
		ITypeBinding[] parameterTypes = methodBinding.getMethodDeclaration()
			.getParameterTypes();
		if (parameterTypes.length != 1) {
			return false;
		}
		return ClassRelationUtil.isContentOfType(parameterTypes[0], "java.util.List"); //$NON-NLS-1$
	}

	private boolean isDeclaringEffectivelyImmutableLocalVariable(
			VariableDeclarationFragment variableDeclarationFragment) {
		if (variableDeclarationFragment.getLocationInParent() != VariableDeclarationStatement.FRAGMENTS_PROPERTY) {
			return false;
		}
		SimpleName nameAtDeclaration = variableDeclarationFragment.getName();
		ASTNode scopeOfVariableUsage = variableDeclarationFragment.getParent()
			.getParent();

		LocalVariableUsagesVisitor localVariableUsagesVisitor = new LocalVariableUsagesVisitor(nameAtDeclaration);
		scopeOfVariableUsage.accept(localVariableUsagesVisitor);
		return localVariableUsagesVisitor.getUsages()
			.stream()
			.filter(usage -> usage != nameAtDeclaration)
			.allMatch(this::isSupportedVariableUsage);
	}

	private boolean isSupportedVariableUsage(SimpleName usage) {
		if (usage.getLocationInParent() == MethodInvocation.ARGUMENTS_PROPERTY) {
			return isCollectionsUnmodifiableListInvocation((MethodInvocation) usage.getParent());
		}

		if (usage.getLocationInParent() == MethodInvocation.EXPRESSION_PROPERTY) {
			MethodInvocation listMethodInvocation = (MethodInvocation) usage.getParent();
			return NOT_MODIFYING_LIST_METHOD_NAMES.contains(listMethodInvocation.getName()
				.getIdentifier());
		}

		return usage.getLocationInParent() == Assignment.LEFT_HAND_SIDE_PROPERTY
				|| usage.getLocationInParent() == EnhancedForStatement.EXPRESSION_PROPERTY;
	}

}