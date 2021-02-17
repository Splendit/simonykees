package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.Expression;

public class AssertTransformationData {
	private final List<Expression> assertionArguments;
	private final Expression assertionMessage;

	public AssertTransformationData(List<Expression> newArguments, Expression assertionMessage) {
		this.assertionArguments = newArguments;
		this.assertionMessage = assertionMessage;
	}
	
	public AssertTransformationData(List<Expression> newArguments) {
		this.assertionArguments = newArguments;
		this.assertionMessage = null;
	}

	public List<Expression> getAssertionArguments() {
		return assertionArguments;
	}

	public Optional<Expression> getAssertionMessage() {
		return Optional.ofNullable(assertionMessage);
	}

}