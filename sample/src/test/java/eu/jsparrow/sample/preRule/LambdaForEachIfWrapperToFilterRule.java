package eu.jsparrow.sample.preRule;

import java.util.Arrays;
import java.util.List;

/**
 * 
 * @author Matthias Webhofer
 * @since 1.2
 */
@SuppressWarnings({ "nls" })
public class LambdaForEachIfWrapperToFilterRule {

	public List<String> list = Arrays.asList("asdf", "jkl", "yxcv", "bnm,");
	public List<Integer> intList = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 0);
	public List<Boolean> booleanList = Arrays.asList(true, false, true, true, false);

	public void doSomething() {
		list.stream().forEach(s -> {
			if (s.length() > 3) {
				System.out.println(s);
				System.out.println(s + s);
			}
		});

		list.parallelStream().forEach(s -> {
			if (s.equals("asdf")) {
				System.out.println(s);
			}
		});

		list.parallelStream().forEach(s -> {
			if ("asdf".equals(s)) {
				System.out.println(s);
			}
		});

		list.forEach(s -> {
			if (s.length() > 3) {
				System.out.println(s);
			}
		});

		intList.stream().forEach(i -> {
			if (i < 5) {
				System.out.println(i);
				i++;
				System.out.println(i);
			}
		});

		intList.stream().forEach(i -> {
			if (i == 5) {
				System.out.println(i);
			}
		});

		booleanList.stream().forEach(b -> {
			if(b) {
				System.out.println(b);
			}
		});

		booleanList.stream().forEach(b -> {
			if(!b) {
				System.out.println(b);
			}
		});

		intList.stream().filter(i -> i == 5).forEach(i -> {
			System.out.println(i);
		});

		intList.stream().forEach(i -> {
			if(getRandomNuber() > 0) {
				System.out.println(i);
			}
		});

		intList.stream().forEach(i -> {
			int j;
			if((j = getRandomNuber()) > 0) {
				System.out.println(i + j);
			}
		});

		intList.stream().forEach(i -> {
			if(i < 0) {
				System.out.println(i);
			} else {
				System.out.println(i + 1);
			}
		});

		intList.stream().forEach(i -> {
			if(i < 0) {
				System.out.println(i);
			} else {
				
			}
		});

		intList.stream().forEach(i -> {
			if(i < 0) {
				System.out.println(i);
			} else {
				;
			}
		});

		intList.stream().forEach(i -> {
			if(i < 0) {
				System.out.println(i);
			} else ;
		});

		intList.stream().forEach(i -> {
			if(i < 0) {
				System.out.println(i);
			} else {
				;
				;
				;
			}
		});
	}
	
	public void forEachOnCollection() {
		list.forEach(s -> {
			if (s.length() > 3) {
				System.out.println(s);
				System.out.println(s + s);
			}
		});
	}
	
	public void ifWithExpressionStatementBody(String input) {
		StringBuilder sb = new StringBuilder();
		list.stream().forEach(s -> {
			if(!s.isEmpty()) 
				sb.append(s);
		});
	}

	private int getRandomNuber() {
		return 0;
	}
}
