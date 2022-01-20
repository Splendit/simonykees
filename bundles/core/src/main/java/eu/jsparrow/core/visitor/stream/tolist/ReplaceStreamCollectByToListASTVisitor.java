package eu.jsparrow.core.visitor.stream.tolist;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

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

import eu.jsparrow.core.markers.common.ReplaceStreamCollectByToListEvent;
import eu.jsparrow.core.visitor.sub.SignatureData;
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
public class ReplaceStreamCollectByToListASTVisitor extends AbstractASTRewriteASTVisitor implements ReplaceStreamCollectByToListEvent {

	private static final String TO_LIST = "toList"; //$NON-NLS-1$
	private static final String TO_UNMODIFIABLE_LIST = "toUnmodifiableList"; //$NON-NLS-1$
	@SuppressWarnings("nls")
	private static final List<String> NOT_MODIFYING_LIST_METHOD_NAMES = Collections.unmodifiableList(Arrays.asList(
			"hashCode", "equals", "toString", "notify", "notifyAll", "wait",
			"forEach", //
			"size", "isEmpty", "contains", "toArray", "containsAll", "stream", "parallelStream", //
			"indexOf", "lastIndexOf", "get"));
	private static final SignatureData COLLECTORS_TO_UNMODIFIABLE_LIST = new SignatureData(
			java.util.stream.Collectors.class, TO_UNMODIFIABLE_LIST);
	private static final SignatureData COLLECTORS_TO_LIST = new SignatureData(java.util.stream.Collectors.class,
			TO_LIST);
	private static final SignatureData STREAM_COLLECT = new SignatureData(java.util.stream.Stream.class, "collect", //$NON-NLS-1$
			java.util.stream.Collector.class);

	private static final Function<IMethodBinding, ITypeBinding[]> GET_PARAMETER_TYPES = methodBinding -> methodBinding
		.getMethodDeclaration()
		.getParameterTypes();

	@Override
	public boolean visit(MethodInvocation node) {

		if (COLLECTORS_TO_UNMODIFIABLE_LIST.isSignatureMatching(node, GET_PARAMETER_TYPES)) {
			findParentStreamCollectInvocation(node).ifPresent(this::transform);

		} else if (COLLECTORS_TO_LIST.isSignatureMatching(node, GET_PARAMETER_TYPES)) {
			findParentStreamCollectInvocation(node)
				.filter(this::analyzeStreamCollectUsingCollectorsToList)
				.ifPresent(this::transform);
		}
		return true;
	}

	private void transform(MethodInvocation supportedStreamCollectInvocation) {
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
		addMarkerEvent(supportedStreamCollectInvocation);
		onRewrite();
	}

	private Optional<MethodInvocation> findParentStreamCollectInvocation(MethodInvocation invocation) {

		if (invocation.getLocationInParent() != MethodInvocation.ARGUMENTS_PROPERTY) {
			return Optional.empty();
		}

		MethodInvocation parentInvocation = (MethodInvocation) invocation.getParent();
		if (STREAM_COLLECT.isSignatureMatching(parentInvocation, GET_PARAMETER_TYPES)) {
			return Optional.of(parentInvocation);
		}

		return Optional.empty();
	}

	private boolean analyzeStreamCollectUsingCollectorsToList(MethodInvocation collectInvocation) {

		if (collectInvocation.getLocationInParent() == MethodInvocation.ARGUMENTS_PROPERTY) {
			return NotModifiedListArgumentAnalyzer
				.keepsListArgumentUnmodified((MethodInvocation) collectInvocation.getParent());
		}

		if (collectInvocation.getLocationInParent() == VariableDeclarationFragment.INITIALIZER_PROPERTY) {

			VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) collectInvocation
				.getParent();
			ITypeBinding expectedType = variableDeclarationFragment.resolveBinding()
				.getType();
			return checkStreamExpressionElementType(expectedType, collectInvocation)
					&& isDeclaringEffectivelyImmutableLocalVariable(variableDeclarationFragment);
		}

		if (collectInvocation.getLocationInParent() == Assignment.RIGHT_HAND_SIDE_PROPERTY) {
			Expression leftHandSide = ((Assignment) collectInvocation.getParent()).getLeftHandSide();
			if (leftHandSide.getNodeType() == ASTNode.SIMPLE_NAME
					&& checkStreamExpressionElementType(leftHandSide.resolveTypeBinding(), collectInvocation)) {
				SimpleName simpleName = (SimpleName) leftHandSide;
				ASTNode declaringNode = getCompilationUnit().findDeclaringNode(simpleName.resolveBinding());
				if (declaringNode != null && declaringNode.getNodeType() == ASTNode.VARIABLE_DECLARATION_FRAGMENT) {
					return isDeclaringEffectivelyImmutableLocalVariable((VariableDeclarationFragment) declaringNode);
				}
			}
			return false;
		}

		if (collectInvocation.getLocationInParent() == MethodInvocation.EXPRESSION_PROPERTY) {
			MethodInvocation listMethodInvocation = (MethodInvocation) collectInvocation.getParent();
			return NOT_MODIFYING_LIST_METHOD_NAMES.contains(listMethodInvocation.getName()
				.getIdentifier());
		}

		return collectInvocation.getLocationInParent() == EnhancedForStatement.EXPRESSION_PROPERTY;
	}

	private boolean checkStreamExpressionElementType(ITypeBinding expectedType, MethodInvocation collectInvocation) {
		ITypeBinding[] expectedTypeArguments = expectedType.getTypeArguments();
		if (expectedTypeArguments.length != 1) {
			return true;
		}
		ITypeBinding[] streamExpressionTypeArguments = collectInvocation.getExpression()
			.resolveTypeBinding()
			.getTypeArguments();

		return ClassRelationUtil.compareITypeBinding(expectedTypeArguments, streamExpressionTypeArguments);
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
			return NotModifiedListArgumentAnalyzer
				.keepsListArgumentUnmodified((MethodInvocation) usage.getParent());
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