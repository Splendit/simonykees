package eu.jsparrow.sample.preRule;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.function.Supplier;

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
		comparator1 = (Integer u1, Integer u2) -> u2.compareTo(u1);
		comparator2 = (Integer u1, Integer u2) -> u2.compareTo(u1);
		class LocalClass<T> implements Comparable<LocalClass<T>> {

			@Override
			public int compareTo(LocalClass<T> o) {
				return 0;
			}
		}
		comparator1 = (LocalClass<Object> u1, LocalClass<Object> u2) -> u1.compareTo(u2);
		comparator2 = (LocalClass<Object> u1, LocalClass<Object> u2) -> u1.compareTo(u2);
		comparator1 = (LocalClass<Object> u1, LocalClass<Object> u2) -> u2.compareTo(u1);
		comparator2 = (LocalClass<Object> u1, LocalClass<Object> u2) -> u2.compareTo(u1);
	}

	void testGetCollectionForIntegerComparator() {
		ArrayList<?> arrayList = getCollectionForComparator((Integer u1, Integer u2) -> u1.compareTo(u2));
		ArrayList<? extends Comparable<?>> arrayList5 = getCollectionForComparator(
				(Integer u1, Integer u2) -> u1.compareTo(u2));
	}

	class TestUseComparatorOfDequeOfInteger {
		void useComparator(Comparator<Deque<Integer>> comparator) {
		}

		void test() {
			useComparator(
					(Deque<Integer> lhs, Deque<Integer> rhs) -> lhs.getFirst()
						.compareTo(rhs.getFirst()));
		}
	}

	class TestUseComparatorOfJoker {

		Comparator<?> useComparator(Comparator<?> comparator) {
			return comparator;
		}

		void testWithComparatorOfInteger() {
			Comparator<?> comparator0 = useComparator((Comparator<Integer>) (t1, t2) -> t1.compareTo(t2));
			Comparator<?> comparator1 = useComparator((Integer t1, Integer t2) -> t1.compareTo(t2));
		}

		void testWithComparatorOfJokerExtendingComparableRawtype() {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			Comparator<?> comparator = useComparator((Comparator<? extends Comparable>) (t1, t2) -> t1.compareTo(t2));
		}

		void testWithComparatorOfComparableRawtype() {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			Comparator<?> comparator0 = useComparator((Comparator<Comparable>) (t1, t2) -> t1.compareTo(t2));
			@SuppressWarnings({ "rawtypes", "unchecked" })
			Comparator<?> comparator1 = useComparator((Comparable t1, Comparable t2) -> t1.compareTo(t2));
		}

		void testWithComparatorOfDeque() {
			Comparator<?> comparator1 = useComparator((Comparator<Deque<Integer>>) (x1, x2) -> x1.getFirst()
				.compareTo(x2.getFirst()));
			Comparator<?> comparator0 = useComparator(
					(Deque<Integer> lhs, Deque<Integer> rhs) -> lhs.getFirst()
						.compareTo(rhs.getFirst()));
		}
	}

	class TestUseObjectWithTypeParameter {
		<T> T useObject(T t) {
			return t;
		}

		void testWithComparatorOfInteger() {
			Comparator<Integer> comparator;
			comparator = this.useObject((Comparator<Integer>) (t1, t2) -> t1.compareTo(t2));
			comparator = this.<Comparator<Integer>>useObject((t1, t2) -> t1.compareTo(t2));
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		void testWithComparatorOfJoker() {
			Comparator<?> comparator;
			comparator = this.<Comparator<?>>useObject((Integer t1, Integer t2) -> t1.compareTo(t2));
			comparator = this.<Comparator<?>>useObject((Comparable t1, Comparable t2) -> t1.compareTo(t2));
		}

		void testWithComparatorOfDequeOfInteger() {
			Comparator<Deque<Integer>> comparator;
			comparator = this.useObject((Comparator<Deque<Integer>>) (lhs, rhs) -> lhs.getFirst()
				.compareTo(rhs.getFirst()));
			comparator = this.<Comparator<Deque<Integer>>>useObject(((lhs, rhs) -> lhs.getFirst()
				.compareTo(rhs.getFirst())));
		}
	}

	class TestNotTransformedDueToLambdaStructure {
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

			Comparator<Deque<Integer>> comparator0 = (lhs, rhs) -> x1.getFirst()
				.compareTo(rhs.getFirst());
			Comparator<Deque<Integer>> comparator1 = (lhs, rhs) -> lhs.getFirst()
				.compareTo(x2.getFirst());
			Comparator<Deque<Integer>> comparator2 = (lhs, rhs) -> x1.getFirst()
				.compareTo(x2.getFirst());

			Comparator<Deque<Integer>> comparator3 = (lhs, rhs) -> useObject(lhs).getFirst()
				.compareTo(rhs.getFirst());
			Comparator<Deque<Integer>> comparator4 = (lhs, rhs) -> lhs.getFirst()
				.compareTo(useObject(rhs).getFirst());

			Comparator<Deque<Integer>> comparator5 = (lhs, rhs) -> getFirst(lhs).compareTo(rhs.getFirst());
			Comparator<Deque<Integer>> comparator6 = (lhs, rhs) -> lhs.getFirst()
				.compareTo(getFirst(rhs));
		}
	}

	class TestClassWithComparableTypeParameter<T extends Comparable<T>> {

		Comparator<T> comparatorOfT;

		@SuppressWarnings("unchecked")
		void test() {
			comparatorOfT = (x1, x2) -> x1.compareTo(x2);
			comparatorOfT = (x1, x2) -> x2.compareTo(x1);

			comparatorOfT = (T x1, T x2) -> x1.compareTo(x2);
			comparatorOfT = (T x1, T x2) -> x2.compareTo(x1);

			comparatorOfT = (Comparator<T>) (x1, x2) -> x1.compareTo(x2);
			comparatorOfT = (Comparator<T>) (x1, x2) -> x2.compareTo(x1);

			comparatorOfT = (Comparator<T>) (Comparator<Integer>) (Integer x1, Integer x2) -> x1.compareTo(x2);
		}
	}

	class TestMethodWithComparableTypeParameter {

		@SuppressWarnings("unchecked")
		<T extends Comparable<T>> void test() {
			Comparator<T> comparatorOfT;

			comparatorOfT = (x1, x2) -> x1.compareTo(x2);
			comparatorOfT = (x1, x2) -> x2.compareTo(x1);

			comparatorOfT = (T x1, T x2) -> x1.compareTo(x2);
			comparatorOfT = (T x1, T x2) -> x2.compareTo(x1);

			comparatorOfT = (Comparator<T>) (x1, x2) -> x1.compareTo(x2);
			comparatorOfT = (Comparator<T>) (x1, x2) -> x2.compareTo(x1);

			comparatorOfT = (Comparator<T>) (Comparator<Integer>) (Integer x1, Integer x2) -> x1.compareTo(x2);
		}
	}

	class TestClassWithTypeParameterSupplyingComparable<T extends Supplier<Integer>> {

		Comparator<T> comparatorOfT;

		void test() {
			comparatorOfT = (x1, x2) -> x1.get()
				.compareTo(x2.get());
			comparatorOfT = (x1, x2) -> x2.get()
				.compareTo(x1.get());

			comparatorOfT = (T x1, T x2) -> x1.get()
				.compareTo(x2.get());
			comparatorOfT = (T x1, T x2) -> x2.get()
				.compareTo(x1.get());

			comparatorOfT = (Comparator<T>) (x1, x2) -> x1.get()
				.compareTo(x2.get());
			comparatorOfT = (Comparator<T>) (x1, x2) -> x2.get()
				.compareTo(x1.get());
		}
	}

	class TestSortingOfArrayList {

		<T extends Comparable<T>> void testWithArrayListOfT() {
			ArrayList<T> arrayList = new ArrayList<>();
			arrayList.sort((x1, x2) -> x1.compareTo(x2));
			arrayList.sort((Comparator<T>) (x1, x2) -> x1.compareTo(x2));
			arrayList.sort((T x1, T x2) -> x1.compareTo(x2));
		}

		<T extends Supplier<Integer>> void testWithArrayListOfTSupplyingInteger() {
			ArrayList<T> arrayList = new ArrayList<>();
			arrayList.sort((x1, x2) -> x1.get()
				.compareTo(x2.get()));
			arrayList.sort((Comparator<T>) (x1, x2) -> x1.get()
				.compareTo(x2.get()));
			arrayList.sort((T x1, T x2) -> x1.get()
				.compareTo(x2.get()));
		}

		void testWithArrayListOfInteger() {
			ArrayList<Integer> arrayList = new ArrayList<>();
			arrayList.sort((x1, x2) -> x1.compareTo(x2));
			arrayList.sort((Comparator<Integer>) (x1, x2) -> x1.compareTo(x2));
			arrayList.sort((Integer x1, Integer x2) -> x1.compareTo(x2));
		}
	}
}