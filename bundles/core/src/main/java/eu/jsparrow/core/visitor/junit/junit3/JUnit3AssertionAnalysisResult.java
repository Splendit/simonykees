package eu.jsparrow.core.visitor.junit.junit3;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

/**
 * Stores all analysis data which have been found by the
 * {@link JUnit3AssertionAnalyzer} for a given JUnit 3 assertion.
 *
 */
public class JUnit3AssertionAnalysisResult {

	private final MethodInvocation methodInvocation;
	private final List<Expression> originalArguments;
	private final String methodName;
	private Expression messageMovingToLastPosition;

	public JUnit3AssertionAnalysisResult(MethodInvocation methodInvocation, List<Expression> originalArguments,
			Expression messageMovingToLastPosition) {
		this(methodInvocation, originalArguments);
		this.messageMovingToLastPosition = messageMovingToLastPosition;
	}

	public JUnit3AssertionAnalysisResult(MethodInvocation methodInvocation, List<Expression> originalArguments) {
		this.methodInvocation = methodInvocation;
		this.methodName = methodInvocation.getName()
			.getIdentifier();
		this.originalArguments = originalArguments;

	}

	String getMethodName() {
		return methodName;
	}

	MethodInvocation getMethodInvocation() {
		return methodInvocation;
	}

	public List<Expression> getOriginalArguments() {
		return originalArguments;
	}

	Optional<Expression> getMessageMovingToLastPosition() {
		return Optional.ofNullable(messageMovingToLastPosition);
	}
}