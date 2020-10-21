package eu.jsparrow.sample.postRule.allRules;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;

@SuppressWarnings({ "unused" })
public class TestUseComparatorMethodsRule {

	<T> ArrayList<T> getCollectionForComparator(Comparator<T> comparator) {
		return new ArrayList<>();
	}

	void testComparatorsForInteger() {
		Comparator<Integer> comparator = Comparator.naturalOrder();
		comparator = Comparator.naturalOrder();
		comparator = Comparator.<java.lang.Integer>naturalOrder();
	}

	void testComparatorsForIntegerReversed() {
		Comparator<Integer> comparator = Comparator.reverseOrder();
		comparator = Comparator.reverseOrder();
		comparator = Comparator.<java.lang.Integer>reverseOrder();
	}

	void testComparatorsForArrayDeque() {
		Comparator<ArrayDeque<Integer>> comparator = Comparator.comparingInt(ArrayDeque::getFirst);
		comparator = Comparator.comparingInt(ArrayDeque::getFirst);
		comparator = Comparator.comparingInt(ArrayDeque::getFirst);
	}

	void testTransformationWithoutLossOfInformation() {
		final Comparator<?> comparator1 = Comparator.<java.lang.Integer>naturalOrder();
		final Comparator<? extends Comparable<?>> comparator2 = Comparator.<java.lang.Integer>naturalOrder();
	}

	void testGetCollectionForIntegerComparator() {
		final ArrayList<?> arrayList = getCollectionForComparator(Comparator.<java.lang.Integer>naturalOrder());
		final ArrayList<? extends Comparable<?>> arrayList5 = getCollectionForComparator(
				Comparator.<java.lang.Integer>naturalOrder());
	}
}