package eu.jsparrow.core.visitor.lambda2methdref;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.LambdaToMethodReferenceASTVisitor;
import eu.jsparrow.core.visitor.impl.UsesSimpleJDTUnitFixture;

public class BugfixSim1826Test extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	public void setUp() throws Exception {
		setVisitor(new LambdaToMethodReferenceASTVisitor());
	}

	@Test
	public void visit_FunctionOfDequeOfIntegerInitialization_shouldTtransform() throws Exception {

		fixture.addImport(java.util.Deque.class.getName());
		fixture.addImport(java.util.function.Function.class.getName());

		String original = "Function<Deque<Integer>, Integer> dequeToInteger = (Deque<Integer> x1) -> x1.getFirst();";
		String expected = "Function<Deque<Integer>, Integer> dequeToInteger = Deque::getFirst;";

		assertChange(original, expected);
	}

	/**
	 * SIM-1826: This test is expected to fail as soon as bug fix SIM-1826 has
	 * been solved.
	 */
	@Test
	public void visit_FunctionOfJokerUsingDequeOfIntegerInitialization_invalidTransformation() throws Exception {

		fixture.addImport(java.util.Deque.class.getName());
		fixture.addImport(java.util.function.Function.class.getName());

		String original = "Function<?, Integer> dequeToInteger = (Deque<Integer> x1) -> x1.getFirst();";
		String expected = "Function<?, Integer> dequeToInteger = Deque::getFirst;";

		assertChange(original, expected);
	}

	@Test
	public void visit_FunctionOfDequeOfIntegerAsAssignmentLHS_shouldTtransform() throws Exception {

		fixture.addImport(java.util.Deque.class.getName());
		fixture.addImport(java.util.function.Function.class.getName());

		String original = "" +
				"Function<Deque<Integer>, Integer> dequeToInteger;\n" +
				"dequeToInteger = (Deque<Integer> x1) -> x1.getFirst();";

		String expected = "" +
				"Function<Deque<Integer>, Integer> dequeToInteger;\n" +
				"dequeToInteger = Deque::getFirst;";

		assertChange(original, expected);
	}

	/**
	 * SIM-1826: This test is expected to fail as soon as bug fix SIM-1826 has
	 * been solved.
	 */
	@Test
	public void visit_FunctionOfJokerUsingDequeOfIntegerAsAssignmentLHS_invalidTransformation() throws Exception {

		fixture.addImport(java.util.Deque.class.getName());
		fixture.addImport(java.util.function.Function.class.getName());

		String original = "" +
				"Function<?, Integer> dequeToInteger;\n" +
				"dequeToInteger = (Deque<Integer> x1) -> x1.getFirst();";

		String expected = "" +
				"Function<?, Integer> dequeToInteger;\n" +
				"dequeToInteger = Deque::getFirst;";

		assertChange(original, expected);
	}

	@Disabled
	@Test
	public void visit___invalidTransformation() throws Exception {

		// fixture.addImport(.class.getName());

		String original = "" +
				"";

		String expected = "" +
				"";

		assertChange(original, expected);
	}

	@Disabled
	@Test
	public void visit___shouldTtransform() throws Exception {

		// fixture.addImport(.class.getName());

		String original = "" +
				"";

		String expected = "" +
				"";

		assertChange(original, expected);
	}
}
