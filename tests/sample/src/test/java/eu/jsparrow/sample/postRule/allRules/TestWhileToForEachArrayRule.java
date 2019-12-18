package eu.jsparrow.sample.postRule.allRules;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unused")
public class TestWhileToForEachArrayRule {

	private static final Logger logger = LoggerFactory.getLogger(TestWhileToForEachArrayRule.class);

	private String[] generateList(String input) {
		return input.split(";"); //$NON-NLS-1$
	}

	public String loopingOverLists(String input) {
		final StringBuilder sb = new StringBuilder();
		final String[] array = generateList(input);
		for (String t : array) {
			logger.info(t);
			sb.append(t);
		}
		return sb.toString();
	}

	public String nestedLoops(String input) {
		final StringBuilder sb = new StringBuilder();
		final String[] array = generateList(input);
		for (String t : array) {
			logger.info(t);
			sb.append(t);
			for (String anArray : array) {
				sb.append(anArray);
			}
		}
		return sb.toString();
	}

	public String tripleNestedLoops(String input) {
		final StringBuilder sb = new StringBuilder();
		final String[] array = generateList(input);
		for (String t : array) {
			logger.info(t);
			sb.append(t);
			for (String anArray : array) {
				sb.append(anArray);
				for (String anArray1 : array) {
					sb.append(anArray1);
					logger.info(anArray1);
				}
			}
		}

		return sb.toString();
	}

	public String cascadedLoops(String input) {
		final StringBuilder sb = new StringBuilder();
		final String[] array = generateList(input);
		final String[] array2 = generateList(input);
		for (String t : array) {
			logger.info(t);
			sb.append(t);
		}

		for (String s : array2) {
			logger.info(s);
			sb.append(s);
		}

		return sb.toString();
	}

	public String indexAccessedBeforeLoop(String input) {
		final StringBuilder sb = new StringBuilder();
		int i = 0;
		i = 1;
		final String[] array = generateList(input);
		i = 0;
		while (i < array.length) {
			final String t = array[i];
			logger.info(t);
			sb.append(t);
			i++;
		}
		return sb.toString();
	}

	public String indexAccessedInsideLoop(String input) {
		final StringBuilder sb = new StringBuilder();
		final String[] array = generateList(input);
		int i = 0;
		while (i < array.length) {
			final String t = array[i];
			logger.info(t);
			sb.append(t + i);
			i++;
		}
		return sb.toString();
	}

	public String indexAccessedAfterLoop(String input) {
		final StringBuilder sb = new StringBuilder();
		final String[] array = generateList(input);
		int i = 0;
		while (i < array.length) {
			final String t = array[i];
			logger.info(t);
			sb.append(t);
			i++;
		}
		sb.append(i);
		return sb.toString();
	}

	public String prefixIndexUpdate(String input) {
		final StringBuilder sb = new StringBuilder();
		final String[] array = generateList(input);
		for (String t : array) {
			logger.info(t);
			sb.append(t);
		}
		return sb.toString();
	}

	public String infixIndexUpdate(String input) {
		final StringBuilder sb = new StringBuilder();
		final String[] array = generateList(input);
		for (String t : array) {
			logger.info(t);
			sb.append(t);
		}
		return sb.toString();
	}

	public String assignmentIndexUpdate(String input) {
		final StringBuilder sb = new StringBuilder();
		final String[] array = generateList(input);
		for (String t : array) {
			logger.info(t);
			sb.append(t);
		}
		return sb.toString();
	}

	public String loopInIfBody(String input) {
		final StringBuilder sb = new StringBuilder();
		final String[] array = generateList(input);
		int i = 0;
		if (array.length > 0) {
			while (i < array.length) {
				final String t = array[i];
				logger.info(t);
				sb.append(t);
				i += 1;
			}
		}
		return sb.toString();
	}

	public String confusingIndexUpdate(String input) {
		final StringBuilder sb = new StringBuilder();
		final String[] array = generateList(input);
		int i = 0;
		int j = 0;
		while (i < array.length) {
			final String t = array[i];
			logger.info(t);
			sb.append(t);
			i += 1;
			j++;
		}
		return sb.toString();
	}

	public String incorrectIndexInitialization(String input) {
		final StringBuilder sb = new StringBuilder();
		final String[] array = generateList(input);
		int i = 1;
		int j = 0;
		while (i < array.length) {
			final String t = array[i];
			logger.info(t);
			sb.append(t);
			j++;
			i += 1;
		}
		return sb.toString();
	}

	public String incorrectIndexUpdate(String input) {
		final StringBuilder sb = new StringBuilder();
		final String[] array = generateList(input);
		int i = 0;
		int j = 0;
		while (i < array.length) {
			final String t = array[i];
			logger.info(t);
			sb.append(t);
			j++;
			i += 2;
		}
		return sb.toString();
	}

	public String incorrectIndexInfixUpdate(String input) {
		final StringBuilder sb = new StringBuilder();
		final String[] array = generateList(input);
		int i = 0;
		while (i < array.length) {
			final String t = array[i];
			logger.info(t);
			sb.append(t);
			i += 2;
		}
		return sb.toString();
	}

	public String confusingIteratorName(String iterator) {
		final StringBuilder sb = new StringBuilder();
		final String[] array = generateList(iterator);
		for (String anArray : array) {
			logger.info(anArray);
			sb.append(anArray);
		}
		return sb.toString();
	}

	public static boolean hasDuplicateItems(Object[] array) {
		// jFreeChart corner case
		int i = 0;
		while (i < array.length) {
			int j = 0;
			while (j < i) {
				final Object o1 = array[i];
				final Object o2 = array[j];
				final boolean condition = o1 != null && o2 != null && o1.equals(o2);
				if (condition) {
					return true;
				}
				j++;
			}
			i++;
		}
		return false;
	}

	public String qualifiedNameType() {
		final java.lang.Boolean[] javaLangBooleans = { true, true, false };
		final StringBuilder sb = new StringBuilder();
		for (java.lang.Boolean javaLangBoolean : javaLangBooleans) {
			sb.append(javaLangBoolean);
		}
		return sb.toString();
	}

	public String unQualifiedNameType() {
		final Boolean[] myBooleans = {};
		final StringBuilder sb = new StringBuilder();
		for (Boolean myBoolean : myBooleans) {
			sb.append(myBoolean);
		}
		return sb.toString();
	}

	class Boolean {
		boolean val = false;
	}
}
