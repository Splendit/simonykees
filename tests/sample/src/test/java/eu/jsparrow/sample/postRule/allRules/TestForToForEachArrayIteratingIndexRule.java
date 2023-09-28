package eu.jsparrow.sample.postRule.allRules;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings({ "nls", "unused", "rawtypes" })
public class TestForToForEachArrayIteratingIndexRule {

	private List<String> generateList(String input) {
		return Arrays.asList(input.split(";"));
	}

	public String multiDimensionArrayUnderIf(String input) {

		final String[][] ms = { { "3", "1", "2" }, { "5", "6", "7" }, { "8", "9", "4" } };
		final StringBuilder sb = new StringBuilder();
		int i = 0;
		if (sb != null) {
			for (String[] inner : ms) {
				final String[] another = inner;
				// comment after inner
				for (String anInner : inner // comment after inner
				) {
					sb.append(anInner);
				}
			}
		}

		return sb.toString();
	}

	public String testForToForEachWithArray(String input) {

		final List<String> foo = generateList(input);
		final String[] ms = (String[]) foo.toArray();
		final StringBuilder sb = new StringBuilder();

		for (String s : ms) {
			sb.append(s);
		}

		return sb.toString();
	}

	public String testingComments(String input) {

		final List<String> foo = generateList(input);
		final String[] ms = (String[]) foo.toArray();
		final StringBuilder sb = new StringBuilder();

		/* leading comment */
		/* condition */
		/* initialization */
		/* incrementing */
		for (String s : ms) {
			// iterator assignment
			sb.append(s);
		}

		return sb.toString();
	}

	public String emptyInitStatement(String input) {

		final List<String> foo = generateList(input);
		final String[] ms = (String[]) foo.toArray();
		final StringBuilder sb = new StringBuilder();
		for (String s : ms) {
			sb.append(s);
		}

		return sb.toString();
	}

	public String doubleInitialization(String input) {

		final List<String> foo = generateList(input);
		final String[] ms = (String[]) foo.toArray();
		final StringBuilder sb = new StringBuilder();
		for (String s : ms) {
			sb.append(s);
		}

		return sb.toString();
	}

	public String emptyUdater(String input) {

		final List<String> foo = generateList(input);
		final String[] ms = (String[]) foo.toArray();
		final StringBuilder sb = new StringBuilder();
		for (String s : ms) {
			sb.append(s);
		}

		return sb.toString();
	}

	public String emptyInfixUpdater(String input) {

		final List<String> foo = generateList(input);
		final String[] ms = (String[]) foo.toArray();
		final StringBuilder sb = new StringBuilder();
		for (String s : ms) {
			sb.append(s);
		}

		return sb.toString();
	}

	public String referencedIndex(String input) {

		final List<String> foo = generateList(input);
		final String[] ms = (String[]) foo.toArray();
		final StringBuilder sb = new StringBuilder();
		int i = 0;
		for (i = 0; i < ms.length; i += 1) {
			final String s = ms[i % 2];
			sb.append(s);
		}

		return sb.toString();
	}

	public String modifiedIndex(String input) {

		final List<String> foo = generateList(input);
		final String[] ms = (String[]) foo.toArray();
		final StringBuilder sb = new StringBuilder();
		int i = 0;
		for (i = 0; i < ms.length; i += 1) {
			final String s = ms[i];
			i++;
			sb.append(s);
		}

		return sb.toString();
	}

	public String referencedIndexOutsideLoop(String input) {

		final List<String> foo = generateList(input);
		final String[] ms = (String[]) foo.toArray();
		final StringBuilder sb = new StringBuilder();
		int i = 0;
		for (i = 0; i < ms.length; i += 1) {
			final String s = ms[i];
			sb.append(s);
		}

		final int c = i;

		return sb.toString();
	}

	public String nestedLoops(String input) {

		final List<String> foo = generateList(input);
		final String[] ms = (String[]) foo.toArray();
		final StringBuilder sb = new StringBuilder();
		for (String s : ms) {
			for (String m : ms) {
				sb.append(m);
				sb.append(s);
			}
		}

		return sb.toString();
	}

	public String singleBodyStatementNestedLoops(String input) {

		final List<String> foo = generateList(input);
		final String[] ms = (String[]) foo.toArray();
		final StringBuilder sb = new StringBuilder();
		for (String m : ms) {
			for (String m1 : ms) {
				sb.append(m1 + m);
			}
		}

		return sb.toString();
	}

	public String singleBodyStatementNestedLoops2(String input) {

		final List<String> foo = generateList(input);
		final String[] ms = (String[]) foo.toArray();
		final StringBuilder sb = new StringBuilder();

		for (String iterator : ms) {
			for (String m : ms) {
				sb.append(m + iterator);
			}
		}

		return sb.toString();
	}

	public String customArrayType(String input) {

		final List<String> foo = generateList(input);
		final MyCollection[] ms = { new MyCollection<>(), new MyCollection<>() };
		final StringBuilder sb = new StringBuilder();
		for (MyCollection m : ms) {
			final String s = m.toString();
			sb.append(s);
		}

		return sb.toString();
	}

	public String confusingDeclaringIndex(String input) {

		final List<String> foo = generateList(input);
		final MyCollection[] ms = { new MyCollection<>(), new MyCollection<>() };
		final StringBuilder sb = new StringBuilder();

		if (foo.isEmpty()) {
			int i = 1;
			i++;
		} else {
			int i = 2;
			i++;
		}
		final Runnable r = () -> {
			final int i1 = 0;
		};
		for (MyCollection m : ms) {
			final String s = m.toString();
			sb.append(s);
		}

		return sb.toString();
	}

	public String clashingWithMethodParamter(String iterator) {

		final String[] ms = { "3", "1", "2" };
		final StringBuilder sb = new StringBuilder();
		final String iterator1;
		for (String m : ms) {
			sb.append(m);
		}

		return sb.toString();
	}

	public String multiDimensionArray(String input) {

		final String[][] ms = { { "3", "1", "2" }, { "5", "6", "7" }, { "8", "9", "4" } };
		final StringBuilder sb = new StringBuilder();
		for (String[] inner : ms) {
			final String[] another = inner;
			for (String anInner : inner) {
				sb.append(anInner);
			}
		}

		return sb.toString();
	}

	public String assigningArrayAccess(String input) {

		final String[] ms = { "", "", "" };
		final StringBuilder sb = new StringBuilder();

		for (int i = 0; i < ms.length; i += 1) {
			ms[i] = "-";
		}

		for (String m : ms) {
			sb.append(m);
		}

		return sb.toString();
	}

	public String cascadedLoopsSameIndexName(String input) {

		final String[] ms = { "1", "2", "3" };
		final String[] ms2 = { "4", "5", "6" };
		final StringBuilder sb = new StringBuilder();

		for (String m : ms) {
			sb.append(m);
		}

		for (String aMs2 : ms2) {
			sb.append(aMs2);
		}

		return sb.toString();
	}

	public String confusingIteratingIndex(String input) {

		final String[] ms = { "1", "2", "3" };
		final StringBuilder sb = new StringBuilder();
		int i = 1;
		for (int j = 0; i < ms.length; i += 1) {
			sb.append(ms[i]);
		}

		return sb.toString();
	}

	public String confusingIteratingIndex2(String input) {

		final String[] ms = {};
		final StringBuilder sb = new StringBuilder();
		int j = 0;
		if (ms.length == 0) {
			for (int i = 0; i < ms.length; j++) {
				sb.append(ms[i]);
			}
		}

		return sb.toString();
	}

	public String confusingIteratingIndex3(String input) {

		final String[] ms = { "1", "2", "3" };
		final StringBuilder sb = new StringBuilder();
		int i = 0;
		for (int j = 0; i < ms.length; i += 1) {
			sb.append(ms[i]);
			sb.append(ms[j]);
		}

		return sb.toString();
	}

	public String confusingIteratingIndex4(String input) {

		final String[] ms = { "1", "2", "3" };
		final StringBuilder sb = new StringBuilder();
		int j = 0;
		for (int i = 0; i < ms.length; j++) {
			sb.append(ms[i]);
			sb.append(ms[j]);
			i++;
		}

		return sb.toString();
	}

	public String compoundCondition(String input) {

		final String[] ms = {};
		final StringBuilder sb = new StringBuilder();
		final int j = 0;
		if (ms.length == 0) {
			for (int i = 0; i < ms.length || i == 2; i++) {
				sb.append(ms[i]);
			}
		}

		return sb.toString();
	}

	public static boolean hasDuplicateItems(Object[] array) {
		// jFreeChart corner case
		for (int i = 0; i < array.length; i++) {
			for (int j = 0; j < i; j++) {
				final Object o1 = array[i];
				final Object o2 = array[j];
				final boolean condition = o1 != null && o2 != null && o1.equals(o2);
				if (condition) {
					return true;
				}
			}
		}
		return false;
	}

	public String qualifiedNameType() {
		final java.lang.Boolean[] javaLangBooleans = { true, true, false };
		final StringBuilder sb = new StringBuilder();
		for (java.lang.Boolean javaLangBoolean : javaLangBooleans) {
			sb.append(javaLangBoolean);
		}
		return sb.toString();
	}

	public String unQualifiedNameType() {
		final Boolean[] javaLangBooleans = {};
		final StringBuilder sb = new StringBuilder();
		for (Boolean javaLangBoolean : javaLangBooleans) {
			sb.append(javaLangBoolean);
		}
		return sb.toString();
	}

	public String arrayOfInnerTypesFromOtherClasses(String input) {
		final Wrapper.Foo[] foos = { Wrapper.createFoo(input) };
		final StringBuilder sb = new StringBuilder();
		for (Wrapper.Foo foo : foos) {
			sb.append(foo.toString());
		}

		return sb.toString();
	}

	private class GenericClassSample<T> {
		class InnerType {

			public void useInnerCollection(InnerType[] myInnerCList) {
				int size = 0;
				for (GenericClassSample<T>.InnerType innerCObje : myInnerCList) {
					size++;
				}
			}
		}
	}

	/**
	 * This collection is not subtype of {@code Iterable}.
	 */
	private class MyCollection<T> {
		private final int size = 5;
		private final int index = 0;

		public boolean hasNext() {
			return index < size;
		}

		public Iterator<T> iterator() {
			return new Iterator<T>() {

				@Override
				public boolean hasNext() {
					return false;
				}

				@Override
				public T next() {
					return null;
				}
			};
		}
	}

	class Boolean {
		public final Boolean FALSE = this.valueOf(false);
		public final Boolean TRUE = this.valueOf(true);
		// my boolean wrapper
		public boolean value;

		public Boolean(boolean val) {
			this.value = val;
		}

		@Override
		public String toString() {
			return java.lang.Boolean.toString(this.value);
		}

		public Boolean valueOf(boolean value) {
			return value ? TRUE : FALSE;
		}
	}
}

class Wrapper {
	public static Foo createFoo(String input) {
		return new Wrapper().new Foo(input);
	}

	class Foo {
		private final String foo;

		public Foo(String foo) {
			this.foo = foo;
		}

		@Override
		public String toString() {
			return this.foo;
		}

		public boolean isFoo() {
			return true;
		}
	}
}
