package eu.jsparrow.sample.postRule.allRules;

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
		comparator = Comparator.naturalOrder();
		comparator = Comparator.<Integer>naturalOrder();
	}

	void testComparatorsForIntegerReversed() {
		Comparator<Integer> comparator = Comparator.reverseOrder();
		comparator = Comparator.reverseOrder();
		comparator = Comparator.<Integer>reverseOrder();
	}

	void testComparatorsForArrayDeque() {
		Comparator<ArrayDeque<Integer>> comparator = Comparator.comparingInt(ArrayDeque::getFirst);
		comparator = Comparator.comparingInt(ArrayDeque::getFirst);
		comparator = Comparator.comparingInt(ArrayDeque::getFirst);
	}

	void testTransformationWithoutLossOfInformation() {
		final Comparator<?> comparator1 = Comparator.<Integer>naturalOrder();
		final Comparator<? extends Comparable<?>> comparator2 = Comparator.<Integer>naturalOrder();
	}

	void testGetCollectionForIntegerComparator() {
		final ArrayList<?> arrayList = getCollectionForComparator(Comparator.<Integer>naturalOrder());
		final ArrayList<? extends Comparable<?>> arrayList5 = getCollectionForComparator(
				Comparator.<Integer>naturalOrder());
	}

	class NotCompilerCleanAfterTransformation {

		void testMethodReferencesNotPossible() {
			final Comparator<?> comparatorDequeOfInt = Comparator.comparingInt(Deque::getFirst);

			final Comparator<?> comparatorDequeOfString = Comparator.comparing(Deque::getFirst);

			final ArrayList<?> arrayList = getCollectionForComparator(Comparator.comparingInt(Deque::getFirst));
		}
	}
}