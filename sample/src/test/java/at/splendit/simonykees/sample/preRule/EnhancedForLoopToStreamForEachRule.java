package at.splendit.simonykees.sample.preRule;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

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

	static {
		stringList3 = new LinkedList<>();
		for(String s : stringList1) {
			for(String t : stringList2) {
				stringList3.add(s + t);
			}
		}
	}

	public String doSomething() throws ClassNotFoundException {

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

		int length2 = 0;
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

		String u = "asdf";
		for(String s : stringList1) {
			if(s.equals(u)) {
				System.out.println(u.length());
			}
		}

		for (String s : stringList1) {
			if(s.length() > u.length()) {
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
		for (String s : stringList1) {
			testClassLocal.testIntField++;
		}

		for (String s : stringList1) {
			--testClassLocal.testIntField;
		}

		for (String s : stringList1) {
			testClassLocal.testIntField += s.length();
		}

		return "";
	}

	private class TestClass {
		public int testIntField = 0;
	}
}
