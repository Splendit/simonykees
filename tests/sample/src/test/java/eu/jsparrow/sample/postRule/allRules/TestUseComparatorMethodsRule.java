package eu.jsparrow.sample.postRule.allRules;

import java.util.ArrayDeque;
import java.util.Comparator;

@SuppressWarnings({ "unused" })
public class TestUseComparatorMethodsRule {

	void testComparatorsForInteger() {
		Comparator<Integer> comparator = Comparator.naturalOrder();
		comparator = Comparator.naturalOrder();
		comparator = Comparator.naturalOrder();
	}

	void testComparatorsForIntegerReversed() {
		Comparator<Integer> comparator = Comparator.reverseOrder();
		comparator = Comparator.reverseOrder();
		comparator = Comparator.reverseOrder();
	}

	void testComparatorsForArrayDeque() {
		Comparator<ArrayDeque<Integer>> comparator = Comparator.comparingInt(ArrayDeque::getFirst);
		comparator = Comparator.comparingInt(ArrayDeque::getFirst);
		comparator = Comparator.comparingInt(ArrayDeque::getFirst);
	}

	void testWithLossOfInformationAfterTransformation() {
		final Comparator<?> comparator1 = Comparator.naturalOrder();
		final Comparator<? extends Comparable<?>> comparator2 = Comparator.naturalOrder();
	}
}