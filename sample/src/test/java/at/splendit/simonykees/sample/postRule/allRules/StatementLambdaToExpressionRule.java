package at.splendit.simonykees.sample.postRule.allRules;

import java.util.List;
import java.util.function.Function;

/**
 * 
 * @author Matthias Webhofer
 * @since 1.2
 *
 */
@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
public class StatementLambdaToExpressionRule {
	public Function<Function, Function> f = (Function function) -> function.compose(function);

	private Function<Function, Function> g = (Function function) -> function.compose(function);

	private void testMethod(List<Integer> list) {
		list.stream().map(element -> element * 2);
		list.stream().map(element -> {
			element *= 2;
			element += 1;
			return element;
		});
		list.stream().map(element -> element * 2);
	}
}
