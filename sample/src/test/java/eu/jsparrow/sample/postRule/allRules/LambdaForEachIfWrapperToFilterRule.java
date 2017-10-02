package eu.jsparrow.sample.postRule.allRules;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Matthias Webhofer
 * @since 1.2
 */
@SuppressWarnings({ "nls" })
public class LambdaForEachIfWrapperToFilterRule {

	private static final Logger logger = LoggerFactory.getLogger(LambdaForEachIfWrapperToFilterRule.class);
	public List<String> list = Arrays.asList("asdf", "jkl", "yxcv", "bnm,");
	public List<Integer> intList = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 0);
	public List<Boolean> booleanList = Arrays.asList(true, false, true, true, false);

	public void doSomething() {
		list.stream().filter(s -> s.length() > 3).forEach(s -> {
			logger.info(s);
			logger.info(s + s);
		});

		list.parallelStream().filter(s -> "asdf".equals(s)).forEach(logger::info);

		list.parallelStream().filter(s -> "asdf".equals(s)).forEach(logger::info);

		list.stream().filter(s -> s.length() > 3).forEach(logger::info);

		intList.stream().filter(i -> i < 5).forEach(i -> {
			System.out.println(i);
			i++;
			System.out.println(i);
		});

		intList.stream().filter(i -> i == 5).forEach(System.out::println);

		booleanList.stream().filter(b -> b).forEach(System.out::println);

		booleanList.stream().filter(b -> !b).forEach(System.out::println);

		intList.stream().filter(i -> i == 5).forEach(System.out::println);

		intList.stream().forEach(i -> {
			if (getRandomNuber() > 0) {
				System.out.println(i);
			}
		});

		intList.stream().forEach(i -> {
			int j;
			if ((j = getRandomNuber()) > 0) {
				System.out.println(i + j);
			}
		});

		intList.stream().forEach(i -> {
			if (i < 0) {
				System.out.println(i);
			} else {
				System.out.println(i + 1);
			}
		});

		intList.stream().filter(i -> i < 0).forEach(System.out::println);

		intList.stream().filter(i -> i < 0).forEach(System.out::println);

		intList.stream().filter(i -> i < 0).forEach(System.out::println);

		intList.stream().filter(i -> i < 0).forEach(System.out::println);
	}

	public void forEachOnCollection() {
		list.stream().filter(s -> s.length() > 3).forEach(s -> {
			logger.info(s);
			logger.info(s + s);
		});
	}

	public void ifWithExpressionStatementBody(String input) {
		StringBuilder sb = new StringBuilder();
		list.stream().filter(s -> !StringUtils.isEmpty(s)).forEach(sb::append);
	}

	private int getRandomNuber() {
		return 0;
	}
}
