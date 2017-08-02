package at.splendit.simonykees.core.visitor.lambdaForEach;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import at.splendit.simonykees.core.util.ASTNodeUtil;
import at.splendit.simonykees.core.util.ClassRelationUtil;
import at.splendit.simonykees.core.visitor.AbstractAddImportASTVisitor;

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
	protected static final String FOR_EACH = "forEach"; //$NON-NLS-1$
	protected static final String VALUE_OF = "valueOf"; //$NON-NLS-1$
	protected static final String MAP = "map"; //$NON-NLS-1$
	protected static final String MAP_TO_INT = "mapToInt"; //$NON-NLS-1$
	protected static final String MAP_TO_LONG = "mapToLong"; //$NON-NLS-1$
	protected static final String MAP_TO_DOUBLE = "mapToDouble"; //$NON-NLS-1$
	protected static final String STREAM = "stream"; //$NON-NLS-1$
	protected static final String PARALLEL_STREAM = "parallelStream"; //$NON-NLS-1$

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
		SimpleName methodName = methodInvocation.getName();
		boolean isForEachInvocation = false;
		if (FOR_EACH.equals(methodName.getIdentifier())
				&& ASTNode.EXPRESSION_STATEMENT == methodInvocation.getParent().getNodeType()) {
			IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
			if (methodBinding != null && ClassRelationUtil.isContentOfTypes(methodBinding.getDeclaringClass(),
					Collections.singletonList(JAVA_UTIL_STREAM_STREAM))) {

				isForEachInvocation = true;
			}
		}

		return isForEachInvocation;
	}

	/**
	 * Checks whether a lambda expression has a single parameter.
	 * 
	 * @param lambdaExpression
	 *            lambda expression to check for.
	 * @return the name of the parameter or {@code null} if the lambda
	 *         expression has more than one ore zero parameters.
	 */
	protected SimpleName extractSingleParameter(LambdaExpression lambdaExpression) {
		SimpleName parameter = null;
		List<VariableDeclarationFragment> fragments = ASTNodeUtil.returnTypedList(lambdaExpression.parameters(),
				VariableDeclarationFragment.class);
		if (fragments.size() == 1) {
			VariableDeclarationFragment fragment = fragments.get(0);
			parameter = fragment.getName();
		} else {
			List<SingleVariableDeclaration> declarations = ASTNodeUtil.returnTypedList(lambdaExpression.parameters(),
					SingleVariableDeclaration.class);
			if (declarations.size() == 1) {
				SingleVariableDeclaration declaration = declarations.get(0);
				parameter = declaration.getName();
			}
		}
		return parameter;
	}

}
