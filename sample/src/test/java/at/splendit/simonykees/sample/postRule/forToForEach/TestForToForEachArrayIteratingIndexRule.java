package at.splendit.simonykees.sample.postRule.forToForEach;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings({"nls", "unused", "rawtypes"})
public class TestForToForEachArrayIteratingIndexRule {
	
	private List<String> generateList(String input) {
		return Arrays.asList(input.split(";"));
	}
	
	public String testForToForEachWithArray(String input) {

		List<String> foo = generateList(input);
		String[] ms = (String[]) foo.toArray();
		StringBuilder sb = new StringBuilder();

		for (String s:ms){
			sb.append(s);
		}

		return sb.toString();
	}
	
	public String emptyInitStatement(String input) {

		List<String> foo = generateList(input);
		String[] ms = (String[]) foo.toArray();
		StringBuilder sb = new StringBuilder();
		for (String s:ms){
			sb.append(s);
		}

		return sb.toString();
	}
	
	public String doubleInitialization(String input) {

		List<String> foo = generateList(input);
		String[] ms = (String[]) foo.toArray();
		StringBuilder sb = new StringBuilder();
		for (String s:ms){
			sb.append(s);
		}

		return sb.toString();
	}
	
	public String emptyUdater(String input) {

		List<String> foo = generateList(input);
		String[] ms = (String[]) foo.toArray();
		StringBuilder sb = new StringBuilder();
		for (String s:ms){
			sb.append(s);
		}

		return sb.toString();
	}
	
	public String emptyInfixUpdater(String input) {

		List<String> foo = generateList(input);
		String[] ms = (String[]) foo.toArray();
		StringBuilder sb = new StringBuilder();
		for (String s:ms){
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
		for (String s:ms){
			for (String iterator:ms){
				sb.append(iterator);
				sb.append(s);
			}
		}

		return sb.toString();
	}
	
	public String customArrayType(String input) {

		List<String> foo = generateList(input);
		MyCollection[] ms = {new MyCollection<>(), new MyCollection<>()};
		StringBuilder sb = new StringBuilder();
		for (MyCollection iterator:ms){
			String s = iterator.toString();
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
		Runnable r = new Runnable() {
			@Override
			public void run() {
				int i = 0;
			}
		};
		for (MyCollection iterator:ms){
			String s = iterator.toString();
			sb.append(s);
		}

		return sb.toString();
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
}
