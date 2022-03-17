package eu.jsparrow.core.visitor.assertj.dedicated;

import java.util.Optional;

import org.eclipse.jdt.core.dom.Expression;

/**
 * Stores the following informations for an AssertJ assertThat invocation in
 * combination with one AssertJ assertion which may undergo analysis about
 * possible simplifications.
 * <ul>
 * <li>the argument of the given AssertJ assertThat invocation</li>
 * <li>the name of a given assertion</li>
 * <li>an optional argument for a given assertion</li>
 * </ul>
 * <p>
 * <p>
 * For an example like
 * <p>
 * {@code  assertThat(string.equals("Hello World!")).isTrue();}
 * <p>
 * <ul>
 * <li>the assertThat argument is {@code string.equals("Hello World!")}</li>
 * <li>the assertion name is {@code isTrue}</li>
 * <li>and there is no assertion argument</li>
 * </ul>
 * .
 * <p>
 * <p>
 * For an example like
 * <p>
 * {@code assertThat(string).isEqualTo("Hello World!");}
 * <ul>
 * <li>the assertThat argument is {@code string}</li>
 * <li>the assertion name is {@code isEqualTo}</li>
 * <li>the assertion argument is {@code"Hello World!"}</li>
 * </ul>
 * .
 * 
 * @since 4.8.0
 */
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
