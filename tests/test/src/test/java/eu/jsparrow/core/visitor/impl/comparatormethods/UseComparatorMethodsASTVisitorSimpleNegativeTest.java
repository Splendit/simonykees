package eu.jsparrow.core.visitor.impl.comparatormethods;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesSimpleJDTUnitFixture;

public class UseComparatorMethodsASTVisitorSimpleNegativeTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	public void setUp() throws Exception {
		setVisitor(new UseComparatorMethodsASTVisitor());
		fixture.addImport(java.util.Comparator.class.getName());
	}

	@Test
	public void visit_InitializeComparatorRawTypeWithComparatorOfComparableRawType_shouldNotTransform()
			throws Exception {
		fixture.addImport(java.lang.Comparable.class.getName());
		String original = "Comparator comparator = (Comparator<Comparable>) (lhs, rhs) -> lhs.compareTo(rhs);";
		assertNoChange(original);
	}

	@Test
	public void visit_TypeCastToComparatorWithJoker_shouldNotTransform() throws Exception {
		fixture.addImport(java.util.Deque.class.getName());
		String original = "Comparator comparator = (Comparator<?>) (Deque<Integer> lhs, Deque<Integer> rhs) -> lhs.getFirst().compareTo(rhs.getFirst());";
		assertNoChange(original);
	}
}
