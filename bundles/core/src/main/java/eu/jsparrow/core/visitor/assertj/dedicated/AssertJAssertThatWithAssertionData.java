package eu.jsparrow.core.visitor.assertj.dedicated;

import java.util.Optional;

import org.eclipse.jdt.core.dom.Expression;

public class AssertJAssertThatWithAssertionData {
	private final Expression assertThatArgument;
	private final String assertionName;
	private Expression assertionArgument;

	AssertJAssertThatWithAssertionData(Expression newAssertThatArgument, String assertionName) {
		this.assertThatArgument = newAssertThatArgument;
		this.assertionName = assertionName;
	}

	AssertJAssertThatWithAssertionData(Expression newAssertThatArgument, String assertionName,
			Expression assertionArgument) {
		this(newAssertThatArgument, assertionName);
		this.assertionArgument = assertionArgument;
	}

	Expression getAssertThatArgument() {
		return assertThatArgument;
	}

	String getAssertionName() {
		return assertionName;
	}

	Optional<Expression> getAssertionArgument() {
		return Optional.ofNullable(assertionArgument);
	}

}
