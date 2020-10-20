package eu.jsparrow.sample.postRule.useComparatorMethods;

import java.util.ArrayDeque;
import java.util.Comparator;

@SuppressWarnings({ "unused" })
public class TestUseComparatorMethodsRule {

	void testComparatorsForInteger() {
		Comparator<Integer> comparator = Comparator.naturalOrder();
		comparator = (lhs, rhs) -> {
			return lhs.compareTo(rhs);
		};
		comparator = new Comparator<Integer>() {

			@Override
			public int compare(Integer lhs, Integer rhs) {
				return lhs.compareTo(rhs);
			}
		};
	}

	void testComparatorsForIntegerReversed() {
		Comparator<Integer> comparator = Comparator.reverseOrder();
		comparator = (lhs, rhs) -> {
			return rhs.compareTo(lhs);
		};
		comparator = new Comparator<Integer>() {

			@Override
			public int compare(Integer lhs, Integer rhs) {
				return rhs.compareTo(lhs);
			}
		};
	}

	void testComparatorsForArrayDeque() {
		Comparator<ArrayDeque<Integer>> comparator = Comparator.comparingInt(ArrayDeque::getFirst);
		comparator = (lhs, rhs) -> {
			return lhs.getFirst()
				.compareTo(rhs.getFirst());
		};
		comparator = new Comparator<ArrayDeque<Integer>>() {

			@Override
			public int compare(ArrayDeque<Integer> lhs, ArrayDeque<Integer> rhs) {
				return lhs.getFirst()
					.compareTo(rhs.getFirst());
			}
		};
	}
}