package eu.jsparrow.core.visitor.assertj;

import java.util.List;

import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;

/**
 * Wrapper class storing all necessary information about an
 * {@link ExpressionStatement} which has a structure like for example
 * 
 * <pre>
 * callMethod1(arg1)
 * 	.callMethod2(arg2)
 * 	.callMethod3(arg3);
 * </pre>
 * 
 * where at least two - or more - method invocations must be chained together by
 * a '.' member selector.
 * 
 * In the example from above {@code callMethod1(arg1)} is the left-most
 * invocation chain element, followed by the subsequent invocation chain
 * elements, in this example by {@code callMethod2(arg2)} and
 * {@code callMethod3(arg3)}
 * <p>
 * 
 * @since 4.6.0
 *
 */
class InvocationChainData {

	private final MethodInvocation leftMostInvocation;
	private final List<MethodInvocation> subsequentInvocations;
	private final ExpressionStatement invocationChainStatement;

	InvocationChainData(MethodInvocation leftMostInvocation, List<MethodInvocation> subsequentInvocations,
			ExpressionStatement invocationChainStatement) {
		this.leftMostInvocation = leftMostInvocation;
		this.subsequentInvocations = subsequentInvocations;
		this.invocationChainStatement = invocationChainStatement;
	}

	ExpressionStatement getInvocationChainStatement() {
		return invocationChainStatement;
	}

	MethodInvocation getLeftMostInvocation() {
		return leftMostInvocation;
	}

	List<MethodInvocation> getSubsequentInvocations() {
		return subsequentInvocations;
	}
}
