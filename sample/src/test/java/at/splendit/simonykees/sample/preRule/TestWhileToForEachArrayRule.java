package at.splendit.simonykees.sample.preRule;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unused")
public class TestWhileToForEachArrayRule {
	
	private String[] generateList(String input) {
		return input.split(";"); //$NON-NLS-1$
	}
	
	public String loopingOverLists(String input) {
		StringBuilder sb = new StringBuilder();
		String[] array = generateList(input);
		int i = 0;
		while (i < array.length) {
			String t = array[i];
			System.out.println(t);
			sb.append(t);
			i++;
		}
		return sb.toString();
	}
	
	public String nestedLoops(String input) {
		StringBuilder sb = new StringBuilder();
		String[] array = generateList(input);
		int i = 0;
		while (i < array.length) {
			String t = array[i];
			System.out.println(t);
			sb.append(t);
			int j = 0;
			while (j < array.length) {
				sb.append(array[j]);
				j++;
			}
			i++;
		}
		return sb.toString();
	}
	
	public String tripleNestedLoops(String input) {
		StringBuilder sb = new StringBuilder();
		String[] array = generateList(input);
		int i = 0;
		while (i < array.length) {
			String t = array[i];
			System.out.println(t);
			sb.append(t);
			int j = 0;
			while (j < array.length) {
				sb.append(array[j]);
				int k = 0;
				while (k < array.length) {
					sb.append(array[k]);
					System.out.print(array[k]);
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
		String[] array = generateList(input);
		String[] array2 = generateList(input);
		int i = 0;
		while (i < array.length) {
			String t = array[i];
			System.out.println(t);
			sb.append(t);
			i++;
		}
		
		int j = 0;
		while (j < array2.length) {
			String s = array2[j];
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
		String [] array = generateList(input);
		i = 0;
		while (i < array.length) {
			String t = array[i];
			System.out.println(t);
			sb.append(t);
			i++;
		}
		return sb.toString();
	}
	
	public String indexAccessedInsideLoop(String input) {
		StringBuilder sb = new StringBuilder();
		String[] array = generateList(input);
		int i = 0;
		while (i < array.length) {
			String t = array[i];
			System.out.println(t);
			sb.append(t + i);
			i++;
		}
		return sb.toString();
	}
	
	public String indexAccessedAfterLoop(String input) {
		StringBuilder sb = new StringBuilder();
		String [] array = generateList(input);
		int i = 0;
		while (i < array.length) {
			String t = array[i];
			System.out.println(t);
			sb.append(t);
			i++;
		}
		sb.append(i);
		return sb.toString();
	}
	
	public String prefixIndexUpdate(String input) {
		StringBuilder sb = new StringBuilder();
		String[] array = generateList(input);
		int i = 0;
		while (i < array.length) {
			String t = array[i];
			System.out.println(t);
			sb.append(t);
			++i;
		}
		return sb.toString();
	}
	
	public String infixIndexUpdate(String input) {
		StringBuilder sb = new StringBuilder();
		String [] array = generateList(input);
		int i = 0;
		while (i < array.length) {
			String t = array[i];
			System.out.println(t);
			sb.append(t);
			i = i + 1;
		}
		return sb.toString();
	}
	
	public String assignmentIndexUpdate(String input) {
		StringBuilder sb = new StringBuilder();
		String [] array = generateList(input);
		int i = 0;
		while (i < array.length) {
			String t = array[i];
			System.out.println(t);
			sb.append(t);
			i += 1;
		}
		return sb.toString();
	}
	
	public String loopInIfBody(String input) {
		StringBuilder sb = new StringBuilder();
		String [] array = generateList(input);
		int i = 0;
		if(array.length > 0)
		while (i < array.length) {
			String t = array[i];
			System.out.println(t);
			sb.append(t);
			i += 1;
		}
		return sb.toString();
	}
	
	public String confusingIndexUpdate(String input) {
		StringBuilder sb = new StringBuilder();
		String [] array = generateList(input);
		int i = 0;
		int j = 0;
		while (i < array.length) {
			String t = array[i];
			System.out.println(t);
			sb.append(t);
			i += 1;
			j++;
		}
		return sb.toString();
	}
	
	public String incorrectIndexInitialization(String input) {
		StringBuilder sb = new StringBuilder();
		String [] array = generateList(input);
		int i = 1;
		int j = 0;
		while (i < array.length) {
			String t = array[i];
			System.out.println(t);
			sb.append(t);
			j++;
			i += 1;
		}
		return sb.toString();
	}
	
	public String incorrectIndexUpdate(String input) {
		StringBuilder sb = new StringBuilder();
		String [] array = generateList(input);
		int i = 0;
		int j = 0;
		while (i < array.length) {
			String t = array[i];
			System.out.println(t);
			sb.append(t);
			j++;
			i += 2;
		}
		return sb.toString();
	}
	
	public String incorrectIndexInfixUpdate(String input) {
		StringBuilder sb = new StringBuilder();
		String [] array = generateList(input);
		int i = 0;
		while (i < array.length) {
			String t = array[i];
			System.out.println(t);
			sb.append(t);
			i = i + 2;
		}
		return sb.toString();
	}
	
	public String confusingIteratorName(String iterator) {
		StringBuilder sb = new StringBuilder();
		String [] array = generateList(iterator);
		int i = 0;
		while (i < array.length) {
			System.out.println(array[i]);
			sb.append(array[i]);
			i = i + 1;
		}
		return sb.toString();
	}
}
