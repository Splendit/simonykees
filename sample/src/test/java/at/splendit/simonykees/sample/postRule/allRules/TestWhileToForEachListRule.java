package at.splendit.simonykees.sample.postRule.allRules;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unused")
public class TestWhileToForEachListRule {

	private static final Logger logger = LoggerFactory.getLogger(TestWhileToForEachListRule.class);

	private List<String> generateList(String input) {
		return Arrays.asList(input.split(";")); //$NON-NLS-1$
	}

	public String loopingOverLists(String input) {
		StringBuilder sb = new StringBuilder();
		List<String> list = generateList(input);
		list.stream().forEach((t) -> {
			logger.info(t);
			sb.append(t);
		});
		return sb.toString();
	}

	public String nestedLoops(String input) {
		StringBuilder sb = new StringBuilder();
		List<String> list = generateList(input);
		list.stream().forEach((t) -> {
			logger.info(t);
			sb.append(t);
			list.stream().forEach(sb::append);
		});
		return sb.toString();
	}

	public String tripleNestedLoops(String input) {
		StringBuilder sb = new StringBuilder();
		List<String> list = generateList(input);
		list.stream().forEach((t) -> {
			logger.info(t);
			sb.append(t);
			list.stream().forEach((iterator) -> {
				sb.append(iterator);
				list.stream().forEach((iterator1) -> {
					sb.append(iterator1);
					logger.info(iterator1);
				});
			});
		});

		return sb.toString();
	}

	public String cascadedLoops(String input) {
		StringBuilder sb = new StringBuilder();
		List<String> list = generateList(input);
		List<String> list2 = generateList(input);
		list.stream().forEach((t) -> {
			logger.info(t);
			sb.append(t);
		});

		list2.stream().forEach((s) -> {
			logger.info(s);
			sb.append(s);
		});

		return sb.toString();
	}

	public String indexAccessedBeforeLoop(String input) {
		StringBuilder sb = new StringBuilder();
		int i = 0;
		i = 1;
		List<String> list = generateList(input);
		i = 0;
		while (i < list.size()) {
			String t = list.get(i);
			logger.info(t);
			sb.append(t);
			i++;
		}
		return sb.toString();
	}

	public String indexAccessedInsideLoop(String input) {
		StringBuilder sb = new StringBuilder();
		List<String> list = generateList(input);
		int i = 0;
		while (i < list.size()) {
			String t = list.get(i);
			logger.info(t);
			sb.append(t + i);
			i++;
		}
		return sb.toString();
	}

	public String indexAccessedAfterLoop(String input) {
		StringBuilder sb = new StringBuilder();
		List<String> list = generateList(input);
		int i = 0;
		while (i < list.size()) {
			String t = list.get(i);
			logger.info(t);
			sb.append(t);
			i++;
		}
		sb.append(i);
		return sb.toString();
	}

	public String prefixIndexUpdate(String input) {
		StringBuilder sb = new StringBuilder();
		List<String> list = generateList(input);
		list.stream().forEach((t) -> {
			logger.info(t);
			sb.append(t);
		});
		return sb.toString();
	}

	public String infixIndexUpdate(String input) {
		StringBuilder sb = new StringBuilder();
		List<String> list = generateList(input);
		list.stream().forEach((t) -> {
			logger.info(t);
			sb.append(t);
		});
		return sb.toString();
	}

	public String assignmentIndexUpdate(String input) {
		StringBuilder sb = new StringBuilder();
		List<String> list = generateList(input);
		list.stream().forEach((t) -> {
			logger.info(t);
			sb.append(t);
		});
		return sb.toString();
	}

	public String loopInIfBody(String input) {
		StringBuilder sb = new StringBuilder();
		List<String> list = generateList(input);
		int i = 0;
		if (list.size() > 0) {
			while (i < list.size()) {
				String t = list.get(i);
				logger.info(t);
				sb.append(t);
				i += 1;
			}
		}
		return sb.toString();
	}

	public String confusingIndexUpdate(String input) {
		StringBuilder sb = new StringBuilder();
		List<String> list = generateList(input);
		int i = 0;
		int j = 0;
		while (i < list.size()) {
			String t = list.get(i);
			logger.info(t);
			sb.append(t);
			i += 1;
			j++;
		}
		return sb.toString();
	}

	public String incorrectIndexInitialization(String input) {
		StringBuilder sb = new StringBuilder();
		List<String> list = generateList(input);
		int i = 1;
		int j = 0;
		while (i < list.size()) {
			String t = list.get(i);
			logger.info(t);
			sb.append(t);
			j++;
			i += 1;
		}
		return sb.toString();
	}

	public String incorrectIndexUpdate(String input) {
		StringBuilder sb = new StringBuilder();
		List<String> list = generateList(input);
		int i = 0;
		int j = 0;
		while (i < list.size()) {
			String t = list.get(i);
			logger.info(t);
			sb.append(t);
			j++;
			i += 2;
		}
		return sb.toString();
	}

	public String incorrectIndexInfixUpdate(String input) {
		StringBuilder sb = new StringBuilder();
		List<String> list = generateList(input);
		int i = 0;
		while (i < list.size()) {
			String t = list.get(i);
			logger.info(t);
			sb.append(t);
			i += 2;
		}
		return sb.toString();
	}

	public String confusingIteratorName(String iterator) {
		StringBuilder sb = new StringBuilder();
		List<String> list = generateList(iterator);
		list.stream().forEach((iterator1) -> {
			logger.info(iterator1);
			sb.append(iterator1);
		});
		return sb.toString();
	}

	public String avoidEmptyStatement(String input) {
		StringBuilder sb = new StringBuilder();
		List<String> list = generateList(input);
		int i = 0;
		while (i < list.size()) {
			sb.append(list.get(i));
			list.get(i);
			i++;
		}

		return sb.toString();
	}
}
