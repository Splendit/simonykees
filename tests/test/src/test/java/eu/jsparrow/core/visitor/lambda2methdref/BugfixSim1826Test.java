package eu.jsparrow.core.visitor.lambda2methdref;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.LambdaToMethodReferenceASTVisitor;
import eu.jsparrow.core.visitor.impl.UsesSimpleJDTUnitFixture;

public class BugfixSim1826Test extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	public void setUp() throws Exception {
		setVisitor(new LambdaToMethodReferenceASTVisitor());
	}

	@Test
	public void visit_FunctionOfDequeOfIntegerInitialization_shouldTransform() throws Exception {

		fixture.addImport(java.util.Deque.class.getName());
		fixture.addImport(java.util.function.Function.class.getName());

		String original = "Function<Deque<Integer>, Integer> dequeToInteger = (Deque<Integer> x1) -> x1.getFirst();";
		String expected = "Function<Deque<Integer>, Integer> dequeToInteger = Deque<Integer>::getFirst;";

		assertChange(original, expected);
	}

	@Test
	public void visit_FunctionOfJokerUsingDequeOfIntegerInitialization_shouldtransform() throws Exception {

		fixture.addImport(java.util.Deque.class.getName());
		fixture.addImport(java.util.function.Function.class.getName());

		String original = "Function<?, Integer> dequeToInteger = (Deque<Integer> x1) -> x1.getFirst();";
		String expected = "Function<?, Integer> dequeToInteger = Deque<Integer>::getFirst;";

		assertChange(original, expected);
	}

	@Test
	public void visit_FunctionOfDequeOfIntegerAsAssignmentLHS_shouldTransform() throws Exception {

		fixture.addImport(java.util.Deque.class.getName());
		fixture.addImport(java.util.function.Function.class.getName());

		String original = "" +
				"Function<Deque<Integer>, Integer> dequeToInteger;\n" +
				"dequeToInteger = (Deque<Integer> x1) -> x1.getFirst();";

		String expected = "" +
				"Function<Deque<Integer>, Integer> dequeToInteger;\n" +
				"dequeToInteger = Deque<Integer>::getFirst;";

		assertChange(original, expected);
	}

	@Test
	public void visit_FunctionOfJokerUsingDequeOfIntegerAsAssignmentLHS_shouldTransform() throws Exception {

		fixture.addImport(java.util.Deque.class.getName());
		fixture.addImport(java.util.function.Function.class.getName());

		String original = "" +
				"Function<?, Integer> dequeToInteger;\n" +
				"dequeToInteger = (Deque<Integer> x1) -> x1.getFirst();";

		String expected = "" +
				"Function<?, Integer> dequeToInteger;\n" +
				"dequeToInteger = Deque<Integer>::getFirst;";

		assertChange(original, expected);
	}

}
