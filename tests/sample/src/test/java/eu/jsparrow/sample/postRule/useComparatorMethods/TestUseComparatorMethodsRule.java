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

	class TestUseComparatorOfDequeOfInteger {
		void useComparator(Comparator<Deque<Integer>> comparator) {
		}

		void test() {
			useComparator(
					Comparator.comparingInt(Deque::getFirst));
		}
	}

	class TestUseComparatorOfJoker {

		void useComparator(Comparator<?> comparator) {
		}

		void test() {
			useComparator(
					Comparator.comparingInt((Deque<Integer> lhs) -> lhs.getFirst()));

			useComparator((Comparator<Deque<Integer>>) Comparator.comparingInt((Deque<Integer> x1) -> x1.getFirst()));

			useComparator((Comparator<Integer>) Comparator.<Integer>naturalOrder());

			useComparator(Comparator.<Integer>naturalOrder());
		}
	}

	class TestUseObjectWithTypeParameter {
		<T> void useObject(T t) {
		}

		void test() {
			this.<Comparator<?>>useObject(Comparator.<Integer>naturalOrder());
		}
	}

	class TestNotTransformedDueToambdsStructure {
		Integer useInteger(Integer integer) {
			return integer;
		}

		<T> T useObject(T object) {
			return object;
		}

		Integer getFirst(Deque<Integer> deque) {
			return deque.getFirst();
		}

		void test(Integer x1, Integer x2) {
			Comparator<Integer> comparator0 = (lhs, rhs) -> lhs.compareTo(x2);
			Comparator<Integer> comparator1 = (lhs, rhs) -> x1.compareTo(rhs);
			Comparator<Integer> comparator2 = (lhs, rhs) -> x1.compareTo(x2);

			Comparator<Integer> comparator3 = (lhs, rhs) -> lhs.compareTo(useInteger(rhs));
			Comparator<Integer> comparator4 = (lhs, rhs) -> useInteger(lhs).compareTo(rhs);

		}

		void test(Deque<Integer> x1, Deque<Integer> x2) {

			Comparator<Deque<Integer>> comparator = Comparator.comparingInt(Deque::getFirst);

			Comparator<Deque<Integer>> comparator3 = (lhs, rhs) -> useObject(lhs).getFirst()
				.compareTo(rhs.getFirst());
			Comparator<Deque<Integer>> comparator4 = (lhs, rhs) -> lhs.getFirst()
				.compareTo(useObject(rhs).getFirst());

			Comparator<Deque<Integer>> comparator5 = (lhs, rhs) -> getFirst(lhs).compareTo(rhs.getFirst());
			Comparator<Deque<Integer>> comparator6 = (lhs, rhs) -> lhs.getFirst()
				.compareTo(getFirst(rhs));
		}
	}
}