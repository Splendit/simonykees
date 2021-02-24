package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.Expression;

/**
 * 
 * @since 3.28.0
 *
 */
public class AssertTransformationData {
	private final String newMethodName;
	private final List<Expression> assertionArguments;
	private final Expression assertionMessage;

	public AssertTransformationData(String newMethodName, List<Expression> assertionArguments,
			Expression assertionMessage) {
		this.newMethodName = newMethodName;
		this.assertionArguments = assertionArguments;
		this.assertionMessage = assertionMessage;
	}

	public AssertTransformationData(String newMethodName, List<Expression> assertionArguments) {
		this.newMethodName = newMethodName;
		this.assertionArguments = assertionArguments;
		this.assertionMessage = null;
	}

	public AssertTransformationData(List<Expression> assertionArguments, Expression assertionMessage) {
		this.newMethodName = null;
		this.assertionArguments = assertionArguments;
		this.assertionMessage = assertionMessage;
	}

	public AssertTransformationData(List<Expression> assertionArguments) {
		this.newMethodName = null;
		this.assertionArguments = assertionArguments;
		this.assertionMessage = null;
	}

	public Optional<String> getNewMethodName() {
		return Optional.ofNullable(newMethodName);
	}

	public List<Expression> getAssertionArguments() {
		return assertionArguments;
	}

	public Optional<Expression> getAssertionMessage() {
		return Optional.ofNullable(assertionMessage);
	}

}