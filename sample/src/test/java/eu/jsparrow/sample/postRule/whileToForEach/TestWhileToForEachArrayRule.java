package eu.jsparrow.sample.postRule.whileToForEach;

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
		for (String t : array) {
			System.out.println(t);
			sb.append(t);
		}
		return sb.toString();
	}
	
	public String nestedLoops(String input) {
		StringBuilder sb = new StringBuilder();
		String[] array = generateList(input);
		for (String t : array) {
			System.out.println(t);
			sb.append(t);
			for (String anArray : array) {
				sb.append(anArray);
			}
		}
		return sb.toString();
	}
	
	public String tripleNestedLoops(String input) {
		StringBuilder sb = new StringBuilder();
		String[] array = generateList(input);
		for (String t : array) {
			System.out.println(t);
			sb.append(t);
			for (String anArray : array) {
				sb.append(anArray);
				for (String anArray1 : array) {
					sb.append(anArray1);
					System.out.print(anArray1);
				}
			}
		}
		
		return sb.toString();
	}
	
	public String cascadedLoops(String input) {
		StringBuilder sb = new StringBuilder();
		String[] array = generateList(input);
		String[] array2 = generateList(input);
		for (String t : array) {
			System.out.println(t);
			sb.append(t);
		}
		
		for (String s : array2) {
			System.out.println(s);
			sb.append(s);
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
		for (String t : array) {
			System.out.println(t);
			sb.append(t);
		}
		return sb.toString();
	}
	
	public String infixIndexUpdate(String input) {
		StringBuilder sb = new StringBuilder();
		String [] array = generateList(input);
		for (String t : array) {
			System.out.println(t);
			sb.append(t);
		}
		return sb.toString();
	}
	
	public String assignmentIndexUpdate(String input) {
		StringBuilder sb = new StringBuilder();
		String [] array = generateList(input);
		for (String t : array) {
			System.out.println(t);
			sb.append(t);
		}
		return sb.toString();
	}
	
	public String loopInIfBody(String input) {
		StringBuilder sb = new StringBuilder();
		String [] array = generateList(input);
		if(array.length > 0)
			for (String t : array) {
				System.out.println(t);
				sb.append(t);
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
		for (String anArray : array) {
			System.out.println(anArray);
			sb.append(anArray);
		}
		return sb.toString();
	}
	
    public static boolean hasDuplicateItems(Object[] array) {
    	// jFreeChart corner case
    	int i = 0;
        while ( i < array.length) {
        	int j = 0;
        	while ( j < i) {
            	Object o1 = array[i];
                Object o2 = array[j];
                if (o1 != null && o2 != null) {
                    if (o1.equals(o2)) {
                        return true;
                    }
                }
                j++;
            }
            i++;
        }
        return false;
    }
    
	public String qualifiedNameType() {
		java.lang.Boolean[] javaLangBooleans = {true, true, false};
		StringBuilder sb = new StringBuilder();
		for (java.lang.Boolean javaLangBoolean : javaLangBooleans) {
			sb.append(javaLangBoolean);
		}
		return sb.toString();
	}
	
	public String unQualifiedNameType() {
		Boolean[] myBooleans = {};
		StringBuilder sb = new StringBuilder();
		for (Boolean myBoolean : myBooleans) {
			sb.append(myBoolean);
		}
		return sb.toString();
	}
	
	class Boolean {
		boolean val = false;
	}
}
