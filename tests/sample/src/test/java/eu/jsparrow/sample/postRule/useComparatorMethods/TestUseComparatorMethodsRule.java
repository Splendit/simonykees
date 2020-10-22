package eu.jsparrow.sample.postRule.useComparatorMethods;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;

@SuppressWarnings({ "unused" })
public class TestUseComparatorMethodsRule {

	<T> ArrayList<T> getCollectionForComparator(Comparator<T> comparator) {
		return new ArrayList<>();
	}

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

	void testTransformationWithoutLossOfInformation() {
		Comparator<?> comparator1 = Comparator.<Integer>naturalOrder();
		Comparator<? extends Comparable<?>> comparator2 = Comparator.<Integer>naturalOrder();
	}

	void testGetCollectionForIntegerComparator() {
		ArrayList<?> arrayList = getCollectionForComparator(Comparator.<Integer>naturalOrder());
		ArrayList<? extends Comparable<?>> arrayList5 = getCollectionForComparator(
				Comparator.<Integer>naturalOrder());
	}
}