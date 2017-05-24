package at.splendit.simonykees.sample.postRule.enhancedForLoopToStreamForEach;

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

		stringList1.stream().forEach((String s)->{
			System.out.println(s);
		});

		stringList1.stream().forEach((String s)->System.out.println(s));

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

		stringList1.stream().forEach((String s)-> {
			stringList2.stream().forEach((String t)->{
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

		stringListList.stream().forEach((List<String> list)->{
			stringList1.add(list.get(0));
			list.stream().forEach((String s)->{
				stringList2.stream().forEach((String t)->{
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

		stringList1.stream().forEach((String s)->{
			try {
				Class.forName(s);
			} catch (ClassNotFoundException cnfe) {
				System.out.println(s);
			}
		});

		for (String s : stringList1) {
			int length = 0;
			if (s.length() < 2) {
				length /= s.length();
			}
		}
		stringList1.stream().forEach((String s)->{
			int length = 0;
			if (s.length() < 2) {
				length /= s.length();
			}
		});

		int length = 0;
		for (String s : stringList1) {
			length += s.length();
		}

		int length2 = 0;
		stringList1.stream().forEach((String s)->{
			if (length2 > 0) {
				System.out.println(length2);
			}
		});

		int length3 = 0;
		for (String s : stringList1) {
			length3 += s.length();
		}

		return "";
	}
}
