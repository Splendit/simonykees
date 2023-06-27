package eu.jsparrow.core.visitor.lambdaforeach;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;

/**
 * A super class for the visitors targeting {@link Stream#forEach(Consumer)}
 * method. Provides the functionality for checking whether a method invocation
 * is a {@link Stream#forEach(Consumer)} invocation.
 * 
 * @author Ardit Ymeri
 * @since 1.2
 *
 */
public class AbstractLambdaForEachASTVisitor extends AbstractAddImportASTVisitor {

	protected static final String JAVA_UTIL_STREAM_STREAM = java.util.stream.Stream.class.getName();
	protected static final String JAVA_UTIL_COLLECTION = java.util.Collection.class.getName();
	protected static final String JAVA_LANG_ITERABLE = java.lang.Iterable.class.getName();
	protected static final String FOR_EACH = "forEach"; //$NON-NLS-1$
	protected static final String FILTER = "filter"; //$NON-NLS-1$
	protected static final String VALUE_OF = "valueOf"; //$NON-NLS-1$
	protected static final String MAP = "map"; //$NON-NLS-1$
	protected static final String MAP_TO_INT = "mapToInt"; //$NON-NLS-1$
	protected static final String MAP_TO_LONG = "mapToLong"; //$NON-NLS-1$
	protected static final String MAP_TO_DOUBLE = "mapToDouble"; //$NON-NLS-1$
	protected static final String STREAM = "stream"; //$NON-NLS-1$
	protected static final String PARALLEL_STREAM = "parallelStream"; //$NON-NLS-1$
	protected static final String FLAT_MAP = "flatMap"; //$NON-NLS-1$

	/**
	 * Checks whether a {@link MethodInvocation} node, is an invocation of
	 * {@link Stream#forEach(Consumer)} method.
	 * 
	 * @param methodInvocation
	 *            a node representing a method invocation
	 * 
	 * @return {@code true} if the the given node is represents an invocation of
	 *         {@link Stream#forEach(Consumer)} or {@code false} otherwise.
	 */
	protected boolean isStreamForEachInvocation(MethodInvocation methodInvocation) {
		return isForEachInvocationOf(methodInvocation, JAVA_UTIL_STREAM_STREAM);
	}

	/**
	 * Checks whether a {@link MethodInvocation} node, is an invocation of
	 * {@link Collection#forEach(Consumer)} method.
	 * 
	 * @param methodInvocation
	 *            a node representing a method invocation
	 * 
	 * @return {@code true} if the the given node is represents an invocation of
	 *         {@link Collection#forEach(Consumer)} or {@code false} otherwise.
	 */
	protected boolean isCollectionForEachInvocation(MethodInvocation methodInvocation) {
		return isForEachInvocationOf(methodInvocation, JAVA_UTIL_COLLECTION);
	}

	/**
	 * Checks if the expression of the given method invocation is a (sub)type of
	 * the given qualified name.
	 * 
	 * @param methodInvocation
	 *            a method invocation to be checked
	 * @param qualifiedName
	 *            the qualified name of the expected type.
	 * @return {@code true} if the aforementioned condition is met, or
	 *         {@code false} otherwise.
	 */
	private boolean isForEachInvocationOf(MethodInvocation methodInvocation, String qualifiedName) {
		SimpleName methodName = methodInvocation.getName();

		if (FOR_EACH.equals(methodName.getIdentifier()) && ASTNode.EXPRESSION_STATEMENT == methodInvocation.getParent()
			.getNodeType()) {
			Expression expression = methodInvocation.getExpression();
			if (expression == null) {
				return false;
			}

			ITypeBinding expressionBinding = expression.resolveTypeBinding();
			if (expressionBinding == null) {
				return false;
			}

			List<String> qualifiedNameList = Collections.singletonList(qualifiedName);
			ITypeBinding expressionErasure = expressionBinding.getErasure();
			if (ClassRelationUtil.isInheritingContentOfTypes(expressionErasure, qualifiedNameList)
					|| ClassRelationUtil.isContentOfTypes(expressionErasure, qualifiedNameList)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Checks whether a lambda expression has a single parameter.
	 * 
	 * @param lambdaExpression
	 *            lambda expression to check for.
	 * @return the name of the parameter or {@code null} if the lambda
	 *         expression has more than one ore zero parameters.
	 */
	protected Optional<SimpleName> extractSingleParameter(LambdaExpression lambdaExpression) {
		Optional<VariableDeclarationFragment> fragmentAsOnlyParameter = ASTNodeUtil
			.findSingletonListElement(lambdaExpression.parameters(), VariableDeclarationFragment.class);
		if (fragmentAsOnlyParameter.isPresent()) {
			return fragmentAsOnlyParameter.map(VariableDeclarationFragment::getName);
		}

		return ASTNodeUtil
			.findSingletonListElement(lambdaExpression.parameters(), SingleVariableDeclaration.class)
			.map(SingleVariableDeclaration::getName);
	}
}
