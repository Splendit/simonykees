package eu.jsparrow.sample.preRule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings({"unused", "rawtypes"})
public class TestWhileToForEachListRule {
	
	private List<String> generateList(String input) {
		return Arrays.asList(input.split(";")); //$NON-NLS-1$
	}
	
	public String loopingOverLists(String input) {
		StringBuilder sb = new StringBuilder();
		List<String>list = generateList(input);
		int i = 0;
		while (i < list.size()) {
			String t = list.get(i);
			System.out.println(t);
			sb.append(t);
			i++;
		}
		return sb.toString();
	}
	
	public String nestedLoops(String input) {
		StringBuilder sb = new StringBuilder();
		List<String>list = generateList(input);
		int i = 0;
		while (i < list.size()) {
			String t = list.get(i);
			System.out.println(t);
			sb.append(t);
			int j = 0;
			while (j < list.size()) {
				sb.append(list.get(j));
				j++;
			}
			i++;
		}
		return sb.toString();
	}
	
	public String tripleNestedLoops(String input) {
		StringBuilder sb = new StringBuilder();
		List<String>list = generateList(input);
		int i = 0;
		while (i < list.size()) {
			String t = list.get(i);
			System.out.println(t);
			sb.append(t);
			int j = 0;
			while (j < list.size()) {
				sb.append(list.get(j));
				int k = 0;
				while (k < list.size()) {
					sb.append(list.get(k));
					System.out.print(list.get(k));
					k++;
				}
				j++;
			}
			i++;
		}
		
		return sb.toString();
	}
	
	public String cascadedLoops(String input) {
		StringBuilder sb = new StringBuilder();
		List<String>list = generateList(input);
		List<String>list2 = generateList(input);
		int i = 0;
		while (i < list.size()) {
			String t = list.get(i);
			System.out.println(t);
			sb.append(t);
			i++;
		}
		
		int j = 0;
		while (j < list2.size()) {
			String s = list2.get(j);
			System.out.println(s);
			sb.append(s);
			j++;
		}
		
		return sb.toString();
	}
	
	public String indexAccessedBeforeLoop(String input) {
		StringBuilder sb = new StringBuilder();
		int i = 0;
		i = 1;
		List<String>list = generateList(input);
		i = 0;
		while (i < list.size()) {
			String t = list.get(i);
			System.out.println(t);
			sb.append(t);
			i++;
		}
		return sb.toString();
	}
	
	public String indexAccessedInsideLoop(String input) {
		StringBuilder sb = new StringBuilder();
		List<String>list = generateList(input);
		int i = 0;
		while (i < list.size()) {
			String t = list.get(i);
			System.out.println(t);
			sb.append(t + i);
			i++;
		}
		return sb.toString();
	}
	
	public String indexAccessedAfterLoop(String input) {
		StringBuilder sb = new StringBuilder();
		List<String>list = generateList(input);
		int i = 0;
		while (i < list.size()) {
			String t = list.get(i);
			System.out.println(t);
			sb.append(t);
			i++;
		}
		sb.append(i);
		return sb.toString();
	}
	
	public String prefixIndexUpdate(String input) {
		StringBuilder sb = new StringBuilder();
		List<String>list = generateList(input);
		int i = 0;
		while (i < list.size()) {
			String t = list.get(i);
			System.out.println(t);
			sb.append(t);
			++i;
		}
		return sb.toString();
	}
	
	public String infixIndexUpdate(String input) {
		StringBuilder sb = new StringBuilder();
		List<String>list = generateList(input);
		int i = 0;
		while (i < list.size()) {
			String t = list.get(i);
			System.out.println(t);
			sb.append(t);
			i = i + 1;
		}
		return sb.toString();
	}
	
	public String assignmentIndexUpdate(String input) {
		StringBuilder sb = new StringBuilder();
		List<String>list = generateList(input);
		int i = 0;
		while (i < list.size()) {
			String t = list.get(i);
			System.out.println(t);
			sb.append(t);
			i += 1;
		}
		return sb.toString();
	}
	
	public String loopInIfBody(String input) {
		StringBuilder sb = new StringBuilder();
		List<String>list = generateList(input);
		int i = 0;
		if(list.size() > 0)
		while (i < list.size()) {
			String t = list.get(i);
			System.out.println(t);
			sb.append(t);
			i += 1;
		}
		return sb.toString();
	}
	
	public String confusingIndexUpdate(String input) {
		StringBuilder sb = new StringBuilder();
		List<String>list = generateList(input);
		int i = 0;
		int j = 0;
		while (i < list.size()) {
			String t = list.get(i);
			System.out.println(t);
			sb.append(t);
			i += 1;
			j++;
		}
		return sb.toString();
	}
	
	public String incorrectIndexInitialization(String input) {
		StringBuilder sb = new StringBuilder();
		List<String>list = generateList(input);
		int i = 1;
		int j = 0;
		while (i < list.size()) {
			String t = list.get(i);
			System.out.println(t);
			sb.append(t);
			j++;
			i += 1;
		}
		return sb.toString();
	}
	
	public String incorrectIndexUpdate(String input) {
		StringBuilder sb = new StringBuilder();
		List<String>list = generateList(input);
		int i = 0;
		int j = 0;
		while (i < list.size()) {
			String t = list.get(i);
			System.out.println(t);
			sb.append(t);
			j++;
			i += 2;
		}
		return sb.toString();
	}
	
	public String incorrectIndexInfixUpdate(String input) {
		StringBuilder sb = new StringBuilder();
		List<String>list = generateList(input);
		int i = 0;
		while (i < list.size()) {
			String t = list.get(i);
			System.out.println(t);
			sb.append(t);
			i = i + 2;
		}
		return sb.toString();
	}
	
	public String confusingIteratorName(String iterator) {
		StringBuilder sb = new StringBuilder();
		List<String>list = generateList(iterator);
		int i = 0;
		while (i < list.size()) {
			System.out.println(list.get(i));
			sb.append(list.get(i));
			i = i + 1;
		}
		return sb.toString();
	}
	
	public String avoidEmptyStatement(String input) {
		StringBuilder sb = new StringBuilder();
		List<String> list = generateList(input);
		int i = 0;
		while(i < list.size()) {
			sb.append(list.get(i));
			list.get(i);
			i++;
		}
		
		return sb.toString();
	}
	
	public String rawIteratingObject(String input) {
		List<List<String>> listOfLists = new ArrayList<>();
		StringBuilder sb = new StringBuilder();
		
		int i = 0;
		while( i < listOfLists.size()) {
			List rawIterator = listOfLists.get(i);
			// Incorrect casting to double
			Double d = (Double) rawIterator.get(0);
			sb.append(d);
			i++;
		}
		
		return sb.toString();
	}
	
	public <T extends Foo> void listOfTypeArguments() {
		List<T> elements = new ArrayList<>();
		int i = 0;
		while (i<elements.size()) {
			T foo = elements.get(i);
			foo.toString();
			foo.isFoo();
			i++;
		}
	}
	
	public String qualifiedNameType() {
		List<java.lang.Boolean> javaLangBooleans = new ArrayList<>();
		StringBuilder sb = new StringBuilder();
		int i = 0;
		while( i<javaLangBooleans.size()) {
			sb.append(javaLangBooleans.get(i));
			i++;
		}
		return sb.toString();
	}
	
	public String unQualifiedNameType() {
		List<Boolean> myBooleans = new ArrayList<>();
		StringBuilder sb = new StringBuilder();
		int i = 0;
		while( i<myBooleans.size()) {
			sb.append(myBooleans.get(i));
			i++;
		}
		return sb.toString();
	}
	
	public String intKeyWord(String input) {
		List<Double> ints = new ArrayList<>();
		StringBuilder sb = new StringBuilder();
		int i = 0;
		while(i< ints.size()) {
			sb.append(ints.get(i));
			i++;
		}
		return sb.toString();
	}
	
	class Foo {
		@Override
		public String toString() {
			return "foo"; //$NON-NLS-1$
		}
		
		public boolean isFoo() {
			return true;
		}
	}
	
	class Boolean {
		boolean val = false;
	}
}
