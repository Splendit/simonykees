package at.splendit.simonykees.sample.preRule;

import java.util.Arrays;
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
	public List<List<String>> stringListList = Arrays.asList(stringList1, stringList2);

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
			length3 += s.length();
		}

		return "";
	}
}
