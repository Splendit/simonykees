package at.splendit.simonykees.sample.postRule.enhancedForLoopToStreamForEach;

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
		stringList1.stream().forEach((s) -> {
			stringList2.stream().forEach((t) -> {
				stringList3.add(s + t);
			});
		});
	}

	public String doSomething() throws ClassNotFoundException {

		stringList1.stream().forEach((s) -> {
			System.out.println(s);
		});

		stringList1.stream().forEach((s) -> System.out.println(s));

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

		stringList1.stream().forEach((s) -> {
			stringList2.stream().forEach((t) -> {
				System.out.println(s + t);
			});
		});

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

		stringListList.stream().forEach((list) -> {
			stringList1.add(list.get(0));
			list.stream().forEach((s) -> {
				stringList2.stream().forEach((t) -> {
					if (t.equals(s)) {
						System.out.println(t);
					}
					if (t.length() > s.length()) {
						System.out.println(s + t);
					}
				});
			});
		});

		for (String s : stringList1) {
			Class.forName(s);
		}

		stringList1.stream().forEach((s) -> {
			try {
				Class.forName(s);
			} catch (ClassNotFoundException cnfe) {
				System.out.println(s);
			}
		});

		stringList1.stream().forEach((s) -> {
			int length = 0;
			if (s.length() < 2) {
				length /= s.length();
			}
		});

		int length = 0;
		for (String s : stringList1) {
			length += s.length();
		}

		final int length2 = 0;
		stringList1.stream().forEach((s) -> {
			if (length2 > 0) {
				System.out.println(length2);
			}
		});

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

		stringList1.stream().forEach((s) -> {
			this.intField++;
		});

		stringList1.stream().forEach((s) -> {
			intField++;
		});

		stringList1.stream().forEach((s) -> {
			testClassField.testIntField++;
		});

		stringList1.stream().forEach((s) -> {
			--testClassField.testIntField;
		});

		stringList1.stream().forEach((s) -> {
			testClassField.testIntField += s.length();
		});

		TestClass testClassLocal = new TestClass();
		EnhancedForLoopToStreamForEachRule rule = new EnhancedForLoopToStreamForEachRule();

		stringList1.stream().forEach((s) -> {
			testClassLocal.testIntField++;
		});

		stringList1.stream().forEach((s) -> {
			--testClassLocal.testIntField;
		});

		stringList1.stream().forEach((s) -> {
			testClassLocal.testIntField += s.length();
		});

		rule.intField = 12;
		rule.testClassField.testIntField = 1;
		for(Map.Entry<String, Map<String, String>> entry : validationConfigurations.entrySet()) {
            Map<String, String> clone = new HashMap<String, String>(entry.getValue().size());
            entry.getValue().entrySet().stream().forEach((entry2) -> {
                clone.put(entry2.getKey(), entry2.getValue());
            });
            rule.validationConfigurations.put(entry.getKey(), clone);
        }
		rule.intField = 12;
		rule.testClassField.testIntField = 1;
		rule = null;

		StringBuffer sb = new StringBuffer();
		stringList1.stream().forEach((s) -> {
			sb.append(s);
			stringList2.stream().forEach((n) -> {
				sb.append(n + ",");
				stringList3.stream().forEach((r) -> {
					String t = s;
					sb.append(r + t);
				});
			});
		});

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

		return "";
	}

	private class TestClass {
		public int testIntField = 0;
		public List<String> stringList = Arrays.asList("asdf", "jkl");
	}
}
