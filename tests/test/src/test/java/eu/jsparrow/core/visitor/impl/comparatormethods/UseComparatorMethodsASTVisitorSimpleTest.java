package eu.jsparrow.core.visitor.impl.comparatormethods;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesSimpleJDTUnitFixture;

public class UseComparatorMethodsASTVisitorSimpleTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	public void setUp() {
		setVisitor(new UseComparatorMethodsASTVisitor());
	}

	@Test
	public void visit_LambdaExpressionComparatorOfInteger_shouldTransform() throws Exception {
		fixture.addImport(java.util.Comparator.class.getName());
		
		String original = "Comparator<Integer> comparator = (lhs, rhs) -> lhs.compareTo(rhs);";
		String expected = "Comparator<Integer> comparator = Comparator.naturalOrder();";
		
		assertChange(original, expected);
	}

	@Test
	public void visit_LambdaExpressionNotComparator_shouldNotTransform() throws Exception {
		fixture.addImport(java.util.function.BiFunction.class.getName());
		
		assertNoChange("BiFunction<Integer, Integer, Integer> bifunction = (lhs, rhs) -> lhs.compareTo(rhs);");
	}
}
