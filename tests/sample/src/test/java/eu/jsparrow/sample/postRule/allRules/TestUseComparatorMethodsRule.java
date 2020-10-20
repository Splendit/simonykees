package eu.jsparrow.sample.postRule.allRules;

import java.util.ArrayDeque;
import java.util.Comparator;

@SuppressWarnings({ "unused" })
public class TestUseComparatorMethodsRule {

	void testComparatorsForInteger() {
		Comparator<Integer> comparator = Comparator.naturalOrder();
		comparator = Comparator.naturalOrder();
		comparator = Integer::compareTo;
	}

	void testComparatorsForIntegerReversed() {
		Comparator<Integer> comparator = Comparator.reverseOrder();
		comparator = Comparator.reverseOrder();
		comparator = (Integer lhs1, Integer rhs1) -> rhs1.compareTo(lhs1);
	}

	void testComparatorsForArrayDeque() {
		Comparator<ArrayDeque<Integer>> comparator = Comparator.comparingInt(ArrayDeque::getFirst);
		comparator = Comparator.comparingInt(ArrayDeque::getFirst);
		comparator = (ArrayDeque<Integer> lhs1, ArrayDeque<Integer> rhs1) -> lhs1.getFirst()
			.compareTo(rhs1.getFirst());
	}
}