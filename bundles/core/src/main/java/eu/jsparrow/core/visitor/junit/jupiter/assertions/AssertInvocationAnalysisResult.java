package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import java.util.Optional;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

class AssertInvocationAnalysisResult {
	private final MethodInvocation originalInvocation;
	private final boolean newInvocationWithoutQualifier;
	private final boolean nameChangedToAssertArrayEquals;
	private final Expression messageAsFirstArgument;

	AssertInvocationAnalysisResult(MethodInvocation originalInvocation, boolean newInvocationWithoutQualifier,
			boolean nameChangedToAssertArrayEquals, Expression messageAsFirstArgument) {
		this.originalInvocation = originalInvocation;
		this.newInvocationWithoutQualifier = newInvocationWithoutQualifier;
		this.nameChangedToAssertArrayEquals = nameChangedToAssertArrayEquals;
		this.messageAsFirstArgument = messageAsFirstArgument;
	}
	
	AssertInvocationAnalysisResult(MethodInvocation originalInvocation, boolean newInvocationWithoutQualifier,
			boolean nameChangedToAssertArrayEquals) {
		this.originalInvocation = originalInvocation;
		this.newInvocationWithoutQualifier = newInvocationWithoutQualifier;
		this.nameChangedToAssertArrayEquals = nameChangedToAssertArrayEquals;
		this.messageAsFirstArgument = null;
	}

	MethodInvocation getOriginalInvocation() {
		return originalInvocation;
	}

	boolean isWithoutQualifierToChange() {
		return newInvocationWithoutQualifier;
	}

	boolean isNameChangedToAssertArrayEquals() {
		return nameChangedToAssertArrayEquals;
	}

	Optional<Expression> getMessageAsFirstArgument() {
		return Optional.ofNullable(messageAsFirstArgument);
	}
}
