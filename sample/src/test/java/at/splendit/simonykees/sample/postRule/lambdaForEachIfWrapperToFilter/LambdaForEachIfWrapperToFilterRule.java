package at.splendit.simonykees.sample.postRule.lambdaForEachIfWrapperToFilter;

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
		list.stream().filter((s) -> s.length() > 3).forEach((s) -> {
			System.out.println(s);
			System.out.println(s + s);
		});

		list.parallelStream().filter((s) -> s.equals("asdf")).forEach((s) -> {
			System.out.println(s);
		});

		list.parallelStream().filter((s) -> "asdf".equals(s)).forEach((s) -> {
			System.out.println(s);
		});

		list.forEach(s -> {
			if (s.length() > 3) {
				System.out.println(s);
			}
		});

		intList.stream().filter((i) -> i < 5).forEach((i) -> {
			System.out.println(i);
			i++;
			System.out.println(i);
		});

		intList.stream().filter((i) -> i == 5).forEach((i) -> {
			System.out.println(i);
		});

		booleanList.stream().filter((b) -> b).forEach((b) -> {
			System.out.println(b);
		});

		booleanList.stream().filter((b) -> !b).forEach((b) -> {
			System.out.println(b);
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

		intList.stream().filter((i) -> i < 0).forEach((i) -> {
			System.out.println(i);
		});

		intList.stream().filter((i) -> i < 0).forEach((i) -> {
			System.out.println(i);
		});

		intList.stream().filter((i) -> i < 0).forEach((i) -> {
			System.out.println(i);
		});

		intList.stream().filter((i) -> i < 0).forEach((i) -> {
			System.out.println(i);
		});
	}
	
	public void ifWithExpressionStatementBody(String input) {
		StringBuilder sb = new StringBuilder();
		list.stream().filter((s) -> !s.isEmpty()).forEach((s) -> sb.append(s));
	}

	private int getRandomNuber() {
		return 0;
	}
}
