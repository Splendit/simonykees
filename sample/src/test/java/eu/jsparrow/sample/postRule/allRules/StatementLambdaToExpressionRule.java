package eu.jsparrow.sample.postRule.allRules;

import java.util.List;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Matthias Webhofer
 * @since 1.2
 *
 */
@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
public class StatementLambdaToExpressionRule {
	private static final Logger logger = LoggerFactory.getLogger(StatementLambdaToExpressionRule.class);

	public Function<Function, Function> f = (Function function) -> function.compose(function);

	private Function<Function, Function> g = (Function function) -> function.compose(function);

	private String elementString;

	private void testMethod(List<Integer> list) {
		list.stream().map(element -> element * 2);
		list.stream().map(element -> {
			element *= 2;
			element += 1;
			return element;
		});
		list.stream().map(element -> element * 2);
		list.forEach(this::doSomething);
		list.forEach(element -> elementString = element.toString());
		list.forEach(element -> Integer.valueOf(1));
		list.forEach(this::doSomething);
	}

	private void doSomething(int element) {
		logger.info(String.valueOf(element));
	}
}
