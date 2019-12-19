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
		/* save me 5 - leading lambda */
		// save me 7 - trailing lambda 2
		/* save me 6 - leading lambda body */
		// save me - leading if statement
		// save me 4 - trailing lambda 1
		list.stream()
			.filter(s -> s.length() > 3 // comment after if condition
			)
			.forEach(s -> {
				// save me 2
				logger.info(s);
				// save me 3
				logger.info(s + s);
			});

		// i'm last but not least
		list.parallelStream()
			.filter(s -> "asdf".equals(s))
			.forEach(logger::info);

		// i'm last but not least
		list.parallelStream()
			.filter(s -> "asdf".equals(s))
			.forEach(logger::info);

		list.stream()
			.filter(s -> s.length() > 3)
			.forEach(logger::info);

		intList.stream()
			.filter(i -> i < 5)
			.forEach(i -> {
				logger.info(String.valueOf(i));
				i++;
				logger.info(String.valueOf(i));
			});

		intList.stream()
			.filter(i -> i == 5)
			.forEach(i -> logger.info(String.valueOf(i)));

		booleanList.stream()
			.filter(b -> b)
			.forEach(b -> logger.info(String.valueOf(b)));

		booleanList.stream()
			.filter(b -> !b)
			.forEach(b -> logger.info(String.valueOf(b)));

		intList.stream()
			.filter(i -> i == 5)
			.forEach(i -> logger.info(String.valueOf(i)));

		intList.stream()
			.forEach(i -> {
				if (getRandomNuber() > 0) {
					logger.info(String.valueOf(i));
				}
			});

		intList.stream()
			.forEach(i -> {
				final int j;
				if ((j = getRandomNuber()) > 0) {
					logger.info(String.valueOf(i + j));
				}
			});

		intList.stream()
			.forEach(i -> {
				if (i < 0) {
					logger.info(String.valueOf(i));
				} else {
					logger.info(String.valueOf(i + 1));
				}
			});

		intList.stream()
			.filter(i -> i < 0)
			.forEach(i -> logger.info(String.valueOf(i)));

		intList.stream()
			.filter(i -> i < 0)
			.forEach(i -> logger.info(String.valueOf(i)));

		intList.stream()
			.filter(i -> i < 0)
			.forEach(i -> logger.info(String.valueOf(i)));

		intList.stream()
			.filter(i -> i < 0)
			.forEach(i -> logger.info(String.valueOf(i)));
	}

	public void forEachOnCollection() {
		list.stream()
			.filter(s -> s.length() > 3)
			.forEach(s -> {
				logger.info(s);
				logger.info(s + s);
			});
	}

	public void ifWithExpressionStatementBody(String input) {
		final StringBuilder sb = new StringBuilder();
		list.stream()
			.filter(s -> !StringUtils.isEmpty(s))
			.forEach(sb::append);
	}

	private int getRandomNuber() {
		return 0;
	}
}
