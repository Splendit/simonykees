package eu.jsparrow.sample.preRule;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;

@SuppressWarnings({ "unused" })
public class TestUseComparatorMethodsRule {

	<T> ArrayList<T> getCollectionForComparator(Comparator<T> comparator) {
		return new ArrayList<>();
	}

	void testComparatorsForInteger() {
		Comparator<Integer> comparator = (lhs, rhs) -> lhs.compareTo(rhs);
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
		Comparator<Integer> comparator = (lhs, rhs) -> rhs.compareTo(lhs);
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
		Comparator<ArrayDeque<Integer>> comparator = (lhs, rhs) -> lhs.getFirst()
			.compareTo(rhs.getFirst());
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
		Comparator<?> comparator1 = (Integer u1, Integer u2) -> u1.compareTo(u2);
		Comparator<? extends Comparable<?>> comparator2 = (Integer u1, Integer u2) -> u1.compareTo(u2);
	}

	void testGetCollectionForIntegerComparator() {
		ArrayList<?> arrayList = getCollectionForComparator((Integer u1, Integer u2) -> u1.compareTo(u2));
		ArrayList<? extends Comparable<?>> arrayList5 = getCollectionForComparator(
				(Integer u1, Integer u2) -> u1.compareTo(u2));
	}
}