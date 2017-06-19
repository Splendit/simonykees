package at.splendit.simonykees.sample.preRule;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author Matthias Webhofer
 * @since 1.2
 */
@SuppressWarnings({ "nls", "unused" })
public class EnhancedForLoopToStreamForEachRule {
	public static List<String> stringList1 = Arrays.asList("str1", "str2", "str3", "str4");
	public static List<String> stringList2 = Arrays.asList("str1", "str2", "str3", "str4");
	public static List<String> stringList3;
	public List<List<String>> stringListList = Arrays.asList(stringList1, stringList2);
	private TestClass testClassField = new TestClass();
	private int intField = 0;
	protected Map<String, Map<String, String>> validationConfigurations = new HashMap<String, Map<String,String>>();

	static {
		stringList3 = new LinkedList<>();
		for (String s : stringList1) {
			for (String t : stringList2) {
				stringList3.add(s + t);
			}
		}
	}

	public String doSomething() throws ClassNotFoundException, FileNotFoundException {

		for (String s : stringList1) {
			System.out.println(s);
		}

		for (String s : stringList1)
			System.out.println(s);

		for (String s : stringList1) {
			if (s.length() > 5) {
				continue;
			} else if (s.length() > 3) {
				break;
			} else {
				return s;
			}
		}

		for (String s : stringList1) {
			if (s.length() > 5) {
				continue;
			}
			System.out.println(s);
		}

		for (String s : stringList1) {
			if (s.length() > 5) {
				break;
			}
			System.out.println(s);
		}

		for (String s : stringList1) {
			if (s.length() > 5) {
				return s;
			}
			System.out.println(s);
		}

		for (String s : stringList1) {
			for (String t : stringList2) {
				System.out.println(s + t);
			}
		}

		for (List<String> list : stringListList) {
			for (String s : list) {
				for (String t : stringList2) {
					if (t.equals(s)) {
						break;
					}
					if (t.length() > s.length()) {
						continue;
					}
				}
			}
		}

		for (List<String> list : stringListList) {
			stringList1.add(list.get(0));
			for (String s : list) {
				for (String t : stringList2) {
					if (t.equals(s)) {
						System.out.println(t);
					}
					if (t.length() > s.length()) {
						System.out.println(s + t);
					}
				}
			}
		}

		for (String s : stringList1) {
			Class.forName(s);
		}

		for (String s : stringList1) {
			try {
				Class.forName(s);
			} catch (ClassNotFoundException cnfe) {
				System.out.println(s);
			}
		}

		for (String s : stringList1) {
			int length = 0;
			if (s.length() < 2) {
				length /= s.length();
			}
		}

		int length = 0;
		for (String s : stringList1) {
			length += s.length();
		}

		final int length2 = 0;
		for (String s : stringList1) {
			if (length2 > 0) {
				System.out.println(length2);
			}
		}

		int length3 = 0;
		for (String s : stringList1) {
			length3++;
			--length3;
			this.intField++;
			--this.intField;
		}

		int length4 = 0;
		for (String s : stringList1) {
			length4++;
		}

		int length5 = 0;
		for (String s : stringList1) {
			--length5;
		}

		String u = "asdf";
		for (String s : stringList1) {
			if (s.equals(u)) {
				System.out.println(u.length());
			}
		}

		for (String s : stringList1) {
			if (s.length() > u.length()) {
				u = s;
			}
		}

		for (String s : stringList1) {
			this.intField++;
		}

		for (String s : stringList1) {
			intField++;
		}

		for (String s : stringList1) {
			testClassField.testIntField++;
		}

		for (String s : stringList1) {
			--testClassField.testIntField;
		}

		for (String s : stringList1) {
			testClassField.testIntField += s.length();
		}

		TestClass testClassLocal = new TestClass();
		EnhancedForLoopToStreamForEachRule rule = new EnhancedForLoopToStreamForEachRule();

		for (String s : stringList1) {
			testClassLocal.testIntField++;
		}

		for (String s : stringList1) {
			--testClassLocal.testIntField;
		}

		for (String s : stringList1) {
			testClassLocal.testIntField += s.length();
		}

		rule.intField = 12;
		rule.testClassField.testIntField = 1;
		for(Map.Entry<String, Map<String, String>> entry : validationConfigurations.entrySet()) {
            Map<String, String> clone = new HashMap<String, String>(entry.getValue().size());
            for (Map.Entry<String, String> entry2 : entry.getValue().entrySet()) {
                clone.put(entry2.getKey(), entry2.getValue());
            }
            rule.validationConfigurations.put(entry.getKey(), clone);
        }
		rule.intField = 12;
		rule.testClassField.testIntField = 1;
		rule = null;

		StringBuffer sb = new StringBuffer();
		for(String s : stringList1) {
			sb.append(s);
			for (String n : stringList2) {
				sb.append(n + ",");
				for (String r : stringList3) {
					String t = s;
					sb.append(r + t);
				}
			}
		}

		for(String s : stringList1) {
			s += ";";
			sb.append(s);
			for (String n : stringList2) {
				sb.append(n + ",");
				for (String r : stringList3) {
					String t = s;
					sb.append(r + t);
				}
			}
		}

		int testClassStringListSize = testClassLocal.stringList.size();
		LinkedList<String> stringListLocal = new LinkedList<>();
		for (int i = 0; i < testClassStringListSize; i++) {
			for (String s : stringListLocal) {
				if (testClassLocal.stringList.get(i).contains(s)) {
					testClassLocal.stringList.get(i);
					testClassLocal.stringList.get(i);
				}
			}
		}

		/*
		 * SIM-472 bugfix
		 */
		for (String s : stringList1) {
			TestClass tc = new TestClass();
		}

		for (String s : stringList1) {
			TestClass tc = new TestClass(1);
		}

		for (Object o : (List) stringList1) {
			System.out.println(o);
		}

		return "";
	}

	private class TestClass {
		public int testIntField = 0;
		public List<String> stringList = Arrays.asList("asdf", "jkl");

		public TestClass() {

		}

		public TestClass(int testIntField) throws FileNotFoundException {
			throw new FileNotFoundException();
		}
	}
}
