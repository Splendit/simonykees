package eu.jsparrow.sample.postRule.allRules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({ "unused", "rawtypes" })
public class TestWhileToForEachListRule {

	private static final Logger logger = LoggerFactory.getLogger(TestWhileToForEachListRule.class);

	private List<String> generateList(String input) {
		return Arrays.asList(input.split(";")); //$NON-NLS-1$
	}

	public String loopingOverLists(String input) {
		final StringBuilder sb = new StringBuilder();
		final List<String> list = generateList(input);
		// save leading comments
		/* expression comment */
		list.forEach(t -> {
			// internal comment
			logger.info(t);
			sb.append(t);
		});
		return sb.toString();
	}

	public String nestedLoops(String input) {
		final StringBuilder sb = new StringBuilder();
		final List<String> list = generateList(input);
		list.forEach(t -> {
			logger.info(t);
			sb.append(t);
			list.forEach(sb::append);
		});
		return sb.toString();
	}

	public String tripleNestedLoops(String input) {
		final StringBuilder sb = new StringBuilder();
		final List<String> list = generateList(input);
		list.forEach(t -> {
			logger.info(t);
			sb.append(t);
			list.forEach(aList -> {
				sb.append(aList);
				list.forEach(aList1 -> {
					sb.append(aList1);
					logger.info(aList1);
				});
			});
		});

		return sb.toString();
	}

	public String cascadedLoops(String input) {
		final StringBuilder sb = new StringBuilder();
		final List<String> list = generateList(input);
		final List<String> list2 = generateList(input);
		list.forEach(t -> {
			logger.info(t);
			sb.append(t);
		});

		list2.forEach(s -> {
			logger.info(s);
			sb.append(s);
		});

		return sb.toString();
	}

	public String indexAccessedBeforeLoop(String input) {
		final StringBuilder sb = new StringBuilder();
		int i = 0;
		i = 1;
		final List<String> list = generateList(input);
		i = 0;
		while (i < list.size()) {
			final String t = list.get(i);
			logger.info(t);
			sb.append(t);
			i++;
		}
		return sb.toString();
	}

	public String indexAccessedInsideLoop(String input) {
		final StringBuilder sb = new StringBuilder();
		final List<String> list = generateList(input);
		int i = 0;
		while (i < list.size()) {
			final String t = list.get(i);
			logger.info(t);
			sb.append(t + i);
			i++;
		}
		return sb.toString();
	}

	public String indexAccessedAfterLoop(String input) {
		final StringBuilder sb = new StringBuilder();
		final List<String> list = generateList(input);
		int i = 0;
		while (i < list.size()) {
			final String t = list.get(i);
			logger.info(t);
			sb.append(t);
			i++;
		}
		sb.append(i);
		return sb.toString();
	}

	public String prefixIndexUpdate(String input) {
		final StringBuilder sb = new StringBuilder();
		final List<String> list = generateList(input);
		list.forEach(t -> {
			logger.info(t);
			sb.append(t);
		});
		return sb.toString();
	}

	public String infixIndexUpdate(String input) {
		final StringBuilder sb = new StringBuilder();
		final List<String> list = generateList(input);
		list.forEach(t -> {
			logger.info(t);
			sb.append(t);
		});
		return sb.toString();
	}

	public String assignmentIndexUpdate(String input) {
		final StringBuilder sb = new StringBuilder();
		final List<String> list = generateList(input);
		list.forEach(t -> {
			logger.info(t);
			sb.append(t);
		});
		return sb.toString();
	}

	public String loopInIfBody(String input) {
		final StringBuilder sb = new StringBuilder();
		final List<String> list = generateList(input);
		int i = 0;
		if (list.size() > 0) {
			while (i < list.size()) {
				final String t = list.get(i);
				logger.info(t);
				sb.append(t);
				i += 1;
			}
		}
		return sb.toString();
	}

	public String confusingIndexUpdate(String input) {
		final StringBuilder sb = new StringBuilder();
		final List<String> list = generateList(input);
		int i = 0;
		int j = 0;
		while (i < list.size()) {
			final String t = list.get(i);
			logger.info(t);
			sb.append(t);
			i += 1;
			j++;
		}
		return sb.toString();
	}

	public String incorrectIndexInitialization(String input) {
		final StringBuilder sb = new StringBuilder();
		final List<String> list = generateList(input);
		int i = 1;
		int j = 0;
		while (i < list.size()) {
			final String t = list.get(i);
			logger.info(t);
			sb.append(t);
			j++;
			i += 1;
		}
		return sb.toString();
	}

	public String incorrectIndexUpdate(String input) {
		final StringBuilder sb = new StringBuilder();
		final List<String> list = generateList(input);
		int i = 0;
		int j = 0;
		while (i < list.size()) {
			final String t = list.get(i);
			logger.info(t);
			sb.append(t);
			j++;
			i += 2;
		}
		return sb.toString();
	}

	public String incorrectIndexInfixUpdate(String input) {
		final StringBuilder sb = new StringBuilder();
		final List<String> list = generateList(input);
		int i = 0;
		while (i < list.size()) {
			final String t = list.get(i);
			logger.info(t);
			sb.append(t);
			i += 2;
		}
		return sb.toString();
	}

	public String confusingIteratorName(String iterator) {
		final StringBuilder sb = new StringBuilder();
		final List<String> list = generateList(iterator);
		list.forEach(aList -> {
			logger.info(aList);
			sb.append(aList);
		});
		return sb.toString();
	}

	public String avoidEmptyStatement(String input) {
		final StringBuilder sb = new StringBuilder();
		final List<String> list = generateList(input);
		int i = 0;
		while (i < list.size()) {
			sb.append(list.get(i));
			list.get(i);
			i++;
		}

		return sb.toString();
	}

	public String rawIteratingObject(String input) {
		final List<List<String>> listOfLists = new ArrayList<>();
		final StringBuilder sb = new StringBuilder();

		int i = 0;
		while (i < listOfLists.size()) {
			final List rawIterator = listOfLists.get(i);
			// Incorrect casting to double
			final Double d = (Double) rawIterator.get(0);
			sb.append(d);
			i++;
		}

		return sb.toString();
	}

	public <T extends Foo> void listOfTypeArguments() {
		final List<T> elements = new ArrayList<>();
		elements.forEach(foo -> {
			foo.toString();
			foo.isFoo();
		});
	}

	public String qualifiedNameType() {
		final List<java.lang.Boolean> javaLangBooleans = new ArrayList<>();
		final StringBuilder sb = new StringBuilder();
		javaLangBooleans.forEach(sb::append);
		return sb.toString();
	}

	public String unQualifiedNameType() {
		final List<Boolean> myBooleans = new ArrayList<>();
		final StringBuilder sb = new StringBuilder();
		myBooleans.forEach(sb::append);
		return sb.toString();
	}

	public String intKeyWord(String input) {
		final List<Double> ints = new ArrayList<>();
		final StringBuilder sb = new StringBuilder();
		ints.forEach(sb::append);
		return sb.toString();
	}

	class Foo {
		@Override
		public String toString() {
			return "foo"; //$NON-NLS-1$
		}

		public boolean isFoo() {
			return true;
		}
	}

	class Boolean {
		boolean val = false;
	}
}
