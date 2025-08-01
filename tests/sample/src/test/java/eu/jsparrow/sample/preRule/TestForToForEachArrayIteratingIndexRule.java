package eu.jsparrow.sample.preRule;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings({"nls", "unused", "rawtypes"})
public class TestForToForEachArrayIteratingIndexRule {
	
	private List<String> generateList(String input) {
		return Arrays.asList(input.split(";"));
	}
	
	public String multiDimensionArrayUnderIf(String input) {
		
		String[][] ms = {{"3", "1", "2"}, {"5", "6", "7"}, {"8", "9", "4"}};
		StringBuilder sb = new StringBuilder();
		int i = 0;
		if(sb != null)
		for (i = 0; i < ms.length; i = i + 1) {
			String []inner = ms[i];
			String []another = ms[i];
			for(int j = 0; j<inner // comment after inner
					.length; j++) {
				sb.append(inner[j]);
			}
		}

		return sb.toString();
	}
	
	public String testForToForEachWithArray(String input) {

		List<String> foo = generateList(input);
		String[] ms = (String[]) foo.toArray();
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < ms.length; i++) {
			String s = ms[i];
			sb.append(s);
		}

		return sb.toString();
	}
	
	public String testingComments(String input) {

		List<String> foo = generateList(input);
		String[] ms = (String[]) foo.toArray();
		StringBuilder sb = new StringBuilder();

		/* leading comment */
		for ( /* initialization */ int i = 0; /* condition */ i < ms.length; /* incrementing */ i++) {
			// iterator assignment
			String s = ms[i];
			sb.append(s);
		}

		return sb.toString();
	}
	
	public String emptyInitStatement(String input) {

		List<String> foo = generateList(input);
		String[] ms = (String[]) foo.toArray();
		StringBuilder sb = new StringBuilder();
		int i = 0;
		for (; i < ms.length; i++) {
			String s = ms[i];
			sb.append(s);
		}

		return sb.toString();
	}
	
	public String doubleInitialization(String input) {

		List<String> foo = generateList(input);
		String[] ms = (String[]) foo.toArray();
		StringBuilder sb = new StringBuilder();
		int i = 0;
		for (i = 0; i < ms.length; i++) {
			String s = ms[i];
			sb.append(s);
		}

		return sb.toString();
	}
	
	public String emptyUdater(String input) {

		List<String> foo = generateList(input);
		String[] ms = (String[]) foo.toArray();
		StringBuilder sb = new StringBuilder();
		int i = 0;
		for (i = 0; i < ms.length;) {
			String s = ms[i];
			sb.append(s);
			++i;
		}

		return sb.toString();
	}
	
	public String emptyInfixUpdater(String input) {

		List<String> foo = generateList(input);
		String[] ms = (String[]) foo.toArray();
		StringBuilder sb = new StringBuilder();
		int i = 0;
		for (i = 0; i < ms.length; i = i + 1) {
			String s = ms[i];
			sb.append(s);
		}

		return sb.toString();
	}
	
	public String referencedIndex(String input) {

		List<String> foo = generateList(input);
		String[] ms = (String[]) foo.toArray();
		StringBuilder sb = new StringBuilder();
		int i = 0;
		for (i = 0; i < ms.length; i = i + 1) {
			String s = ms[i%2];
			sb.append(s);
		}

		return sb.toString();
	}
	
	public String modifiedIndex(String input) {

		List<String> foo = generateList(input);
		String[] ms = (String[]) foo.toArray();
		StringBuilder sb = new StringBuilder();
		int i = 0;
		for (i = 0; i < ms.length; i = i + 1) {
			String s = ms[i];
			i++;
			sb.append(s);
		}

		return sb.toString();
	}
	
	public String referencedIndexOutsideLoop(String input) {

		List<String> foo = generateList(input);
		String[] ms = (String[]) foo.toArray();
		StringBuilder sb = new StringBuilder();
		int i = 0;
		for (i = 0; i < ms.length; i = i + 1) {
			String s = ms[i];
			sb.append(s);
		}
		
		int c = i;

		return sb.toString();
	}
	
	public String nestedLoops(String input) {

		List<String> foo = generateList(input);
		String[] ms = (String[]) foo.toArray();
		StringBuilder sb = new StringBuilder();
		int i = 0;
		for (i = 0; i < ms.length; i = i + 1) {
			String s = ms[i];
			for(int j = 0; j<ms.length; j++) {
				sb.append(ms[j]);
				sb.append(s);
			}
		}

		return sb.toString();
	}
	
	public String singleBodyStatementNestedLoops(String input) {

		List<String> foo = generateList(input);
		String[] ms = (String[]) foo.toArray();
		StringBuilder sb = new StringBuilder();
		int i = 0;
		for (i = 0; i < ms.length; i = i + 1)
			for(int j = 0; j<ms.length; j++)
				sb.append(ms[j] + ms[i]);

		return sb.toString();
	}
	
	public String singleBodyStatementNestedLoops2(String input) {

		List<String> foo = generateList(input);
		String[] ms = (String[]) foo.toArray();
		StringBuilder sb = new StringBuilder();
		
		for (String iterator : ms)
			for(int j = 0; j<ms.length; j++)
				sb.append(ms[j] + iterator);

		return sb.toString();
	}
	
	public String customArrayType(String input) {

		List<String> foo = generateList(input);
		MyCollection[] ms = {new MyCollection<>(), new MyCollection<>()};
		StringBuilder sb = new StringBuilder();
		int i = 0;
		for (i = 0; i < ms.length; i = i + 1) {
			String s = ms[i].toString();
			sb.append(s);
		}

		return sb.toString();
	}
	
	public String confusingDeclaringIndex(String input) {

		List<String> foo = generateList(input);
		MyCollection[] ms = {new MyCollection<>(), new MyCollection<>()};
		StringBuilder sb = new StringBuilder();

		if(foo.isEmpty()) {
			int i = 1;
			i++;
		} else {
			int i = 2;
			i++;
		}
		int i = 0;
		
		Runnable r = new Runnable() {
			@Override
			public void run() {
				int i = 0;
			}
		};
		for (i = 0; i < ms.length; i++) {
			String s = ms[i].toString();
			sb.append(s);
		}

		return sb.toString();
	}
	
	public String clashingWithMethodParamter(String iterator) {
		
		String[] ms = {"3", "1", "2"};
		StringBuilder sb = new StringBuilder();
		String iterator1;
		int i = 0;
		for (i = 0; i < ms.length; i = i + 1) {
			sb.append(ms[i]);
		}

		return sb.toString();
	}

	public String multiDimensionArray(String input) {
		
		String[][] ms = {{"3", "1", "2"}, {"5", "6", "7"}, {"8", "9", "4"}};
		StringBuilder sb = new StringBuilder();
		int i = 0;
		for (i = 0; i < ms.length; i = i + 1) {
			String []inner = ms[i];
			String []another = ms[i];
			for(int j = 0; j<inner.length; j++) {
				sb.append(inner[j]);
			}
		}

		return sb.toString();
	}
	
	public String assigningArrayAccess(String input) {
		
		String[] ms = {"", "", ""};
		StringBuilder sb = new StringBuilder();
		
		for (int i = 0; i < ms.length; i = i + 1) {
			ms[i] = "-";
		}
		
		for (int j = 0; j < ms.length; j = j + 1) {
			sb.append(ms[j]);
		}

		return sb.toString();
	}
	
	public String cascadedLoopsSameIndexName(String input) {
		
		String[] ms = {"1", "2", "3"};
		String[] ms2 = {"4", "5", "6"};
		StringBuilder sb = new StringBuilder();
		
		for (int i = 0; i < ms.length; i = i + 1) {
			sb.append(ms[i]);
		}
		
		for (int i = 0; i < ms2.length; i = i + 1) {
			sb.append(ms2[i]);
		}

		return sb.toString();
	}
	
	public String confusingIteratingIndex(String input) {
		
		String[] ms = {"1", "2", "3"};
		StringBuilder sb = new StringBuilder();
		int i = 1;
		for (int j = 0; i < ms.length; i = i + 1) {
			sb.append(ms[i]);
		}
		
		return sb.toString();
	}
	
	public String confusingIteratingIndex2(String input) {
		
		String[] ms = {};
		StringBuilder sb = new StringBuilder();
		int j = 0;
		if(ms.length == 0) {
			for (int i = 0; i < ms.length; j++) {
				sb.append(ms[i]);
			}
		}
		
		return sb.toString();
	}
	
	public String confusingIteratingIndex3(String input) {
		
		String[] ms = {"1", "2", "3"};
		StringBuilder sb = new StringBuilder();
		int i = 0;
		for (int j = 0; i < ms.length; i = i + 1) {
			sb.append(ms[i]);
			sb.append(ms[j]);
		}
		
		return sb.toString();
	}
	
	public String confusingIteratingIndex4(String input) {
		
		String[] ms = {"1", "2", "3"};
		StringBuilder sb = new StringBuilder();
		int j = 0;
		for (int i = 0; i < ms.length; j++) {
			sb.append(ms[i]);
			sb.append(ms[j]);
			i++;
		}
		
		return sb.toString();
	}
	
	public String compoundCondition(String input) {
		
		String[] ms = {};
		StringBuilder sb = new StringBuilder();
		int j = 0;
		if(ms.length == 0) {
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
            	Object o1 = array[i];
                Object o2 = array[j];
                if (o1 != null && o2 != null) {
                    if (o1.equals(o2)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
	public String qualifiedNameType() {
		java.lang.Boolean[] javaLangBooleans = {true, true, false};
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i<javaLangBooleans.length; i++) {
			sb.append(javaLangBooleans[i]);
		}
		return sb.toString();
	}
	
	public String unQualifiedNameType() {
		Boolean[] javaLangBooleans = {};
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i<javaLangBooleans.length; i++) {
			sb.append(javaLangBooleans[i]);
		}
		return sb.toString();
	}
	
	public String arrayOfInnerTypesFromOtherClasses(String input) {
		Wrapper.Foo[] foos = {Wrapper.createFoo(input)};
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < foos.length; i++) {
			Wrapper.Foo foo = foos[i];
			sb.append(foo.toString());
		}
		
		return sb.toString();
	}
	
	private class GenericClassSample<T> {
		class InnerType {
			
			public void useInnerCollection(InnerType[] myInnerCList) {
				int size = 0;
				for(int i = 0; i < myInnerCList.length; i++) {
					InnerType innerCObje = myInnerCList[i];
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
		private int index = 0;
		
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
			if (value) {
				return TRUE;
			} else {
				return FALSE;
			}
		}
	}
}

class Wrapper {
	class Foo {
		private String foo;
		
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

	public static Foo createFoo(String input) {
		return new Wrapper().new Foo(input);
	}
}
