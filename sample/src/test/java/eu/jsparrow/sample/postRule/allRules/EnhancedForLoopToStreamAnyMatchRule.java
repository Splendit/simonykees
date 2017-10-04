package eu.jsparrow.sample.postRule.allRules;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * 
 * @author Ardit Ymeri
 * @since 2.1.1
 *
 */
@SuppressWarnings({ "unused", "nls" })
public class EnhancedForLoopToStreamAnyMatchRule {

	boolean missingBoolDecl = false;

	public void usingAnyMatch(List<String> strings) {
		boolean containsEmpty = strings.stream().anyMatch(StringUtils::isEmpty);
	}

	public void statementsInBetween(List<String> strings) {
		String b = "b";
		if (strings.contains("a")) {
			strings.add(b);
		}
		boolean containsEmpty = strings.stream().anyMatch(StringUtils::isEmpty);
	}

	public void statementsBefore(List<String> strings) {
		String b = "b";
		if (strings.contains("a")) {
			strings.add(b);
		}
		boolean containsEmpty = strings.stream().anyMatch(StringUtils::isEmpty);
	}

	public void statementsAfter(List<String> strings) {
		boolean containsEmpty = strings.stream().anyMatch(StringUtils::isEmpty);

		String b = "b";
		if (strings.contains("a")) {
			strings.add(b);
		}
	}

	public void modifiedBoolVariable(List<String> strings) {
		boolean containsEmpty = false;
		if (strings.contains("")) {
			containsEmpty = true;
			return;
		}

		for (String value : strings) {
			if (StringUtils.isEmpty(value)) {
				containsEmpty = true;
				break;
			}
		}
	}

	public void multipleDeclarationFragments(List<String> strings) {
		boolean containsA = false;
		if (strings.contains("a")) {
			containsA = true;
		}

		boolean containsEmpty = strings.stream().anyMatch(StringUtils::isEmpty);
	}

	public void missingBreakStatement(List<String> strings) {
		boolean containsEmpty = false;
		for (String value : strings) {
			if (StringUtils.isEmpty(value)) {
				containsEmpty = true;
			}
		}
	}

	public void existingElseBranch(List<String> strings) {
		boolean containsEmpty = false;
		for (String value : strings) {
			if (StringUtils.isEmpty(value)) {
				containsEmpty = true;
				break;
			} else {
				value.split("\\.");
			}
		}
	}

	public void multipleLoopBodyStatements(List<String> strings) {
		boolean containsEmpty = false;
		for (String value : strings) {
			if (StringUtils.isEmpty(value)) {
				containsEmpty = true;
				break;
			}
			String[] parts = value.split("\\.");
			if (parts.length == 0) {
				return;
			}
		}
	}

	public void missingIfStatement(List<String> strings) {
		boolean containsEmpty = false;
		for (String value : strings) {
			containsEmpty = true;
			break;
		}
	}

	public void missingBooleanDeclaration(List<String> strings) {
		for (String value : strings) {
			if (StringUtils.isEmpty(value)) {
				missingBoolDecl = true;
				break;
			}
		}
	}

	public void swappedBooleanValues(List<String> strings) {
		boolean containsNonEmpty = strings.stream().filter(StringUtils::isEmpty).findFirst().map(value -> false)
				.orElse(true);
	}

	public void sameBooleanValues1(List<String> strings) {
		boolean containsNonEmpty = strings.stream().filter(StringUtils::isEmpty).findFirst().map(value -> true)
				.orElse(true);
	}

	public void sameBooleanValues2(List<String> strings) {
		boolean containsNonEmpty = strings.stream().filter(StringUtils::isEmpty).findFirst().map(value -> false)
				.orElse(false);
	}

	public void compoundCondition(List<String> strings) {
		String emptyString = "";
		boolean containsEmpty = strings.stream().anyMatch(emptyString::equals);
	}

	public void nonEffectivelyFinalCondition(List<String> strings) {
		boolean containsEmpty = false;
		String emptyString = "";
		emptyString = "";
		for (String value : strings) {
			if (emptyString.equals(value)) {
				containsEmpty = true;
				break;
			}
		}
	}

	public void loopWithSingleBodyStatement(List<String> strings) {
		String emptyString = "";
		boolean containsEmpty = strings.stream().anyMatch(emptyString::equals);
	}

	/*
	 * Testing loops with return statement
	 */

	public boolean loopWithReturnStatement(List<String> strings) {
		String emptyString = "";
		return strings.stream().anyMatch(emptyString::equals);
	}

	public boolean nonEffectivelyFinalReturnStatement(List<String> strings) {
		String emptyString = "";
		emptyString = "";
		for (String value : strings) {
			if (emptyString.equals(value)) {
				return true;
			}
		}
		return false;
	}

	public boolean statementsBetweenLoopAndReturnStatement(List<String> strings) {
		String emptyString = "";
		for (String value : strings) {
			if (emptyString.equals(value)) {
				return true;
			}
		}
		String nonEmptyString = "I dont let you convert to anyMatch";
		return false;
	}

	public boolean mixedReturnValues(List<String> strings) {
		String emptyString = "";
		return strings.stream().filter(emptyString::equals).findFirst().map(value -> false).orElse(false);
	}

	public boolean mixedReturnValues2(List<String> strings) {
		String emptyString = "";
		return strings.stream().filter(emptyString::equals).findFirst().map(value -> true).orElse(true);
	}

	public boolean mixedReturnValues3(List<String> strings) {
		String emptyString = "";
		return strings.stream().filter(emptyString::equals).findFirst().map(value -> false).orElse(true);
	}

	public boolean irrelevantStatementsBeforeLoop(List<String> strings) {
		String emptyString = "";
		String nonEmpty = "I dont stop you from converting to anyMatch";
		return strings.stream().anyMatch(emptyString::equals);
	}

	public boolean noIfWrapperAroundReturn(List<String> strings) {
		String emptyString = "";
		for (String value : strings) {
			return true;
		}
		return false;
	}

	public boolean noReturnStatementInsideIf(List<String> strings) {
		String emptyString = "";
		strings.stream().filter(value -> !emptyString.equals(value)).map(value -> StringUtils.substring(value, 0, 1))
				.forEach(prefix -> {
				});
		return false;
	}

	public boolean multipleStatementsInsideIf(List<String> strings) {
		String emptyString = "";
		for (String value : strings) {
			if (!emptyString.equals(value)) {
				String prefix = StringUtils.substring(value, 0, 1);
				return true;
			}
		}
		return false;
	}

	public boolean ifWithSingleBodyStatement(List<String> strings) {
		String emptyString = "";
		return strings.stream().anyMatch(emptyString::equals);
	}

	public boolean singleBodyStatementEverywhere(List<String> strings) {
		String emptyString = "";
		return strings.stream().anyMatch(emptyString::equals);
	}

	public void emptyReturnStatements(List<String> strings) {
		String emptyString = "";
		for (String value : strings) {
			if (emptyString.equals(value)) {
				return;
			}
		}

		return;
	}

	public boolean unhandledException(List<String> strings) throws Exception {
		String emptyString = "";
		for (String value : strings) {
			if (compareEquals(emptyString, value)) {
				return true;
			}
		}

		return false;
	}

	private boolean compareEquals(String value1, String value2) throws Exception {
		if (value1 == null || value2 == null) {
			throw new Exception();
		}

		return value1.equals(value2);
	}
}
