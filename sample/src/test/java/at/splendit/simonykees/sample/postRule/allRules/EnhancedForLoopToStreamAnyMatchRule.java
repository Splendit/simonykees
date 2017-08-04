package at.splendit.simonykees.sample.postRule.allRules;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * 
 * @author Ardit Ymeri
 * @since 2.0.2
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
		boolean containsNonEmpty = true;
		for (String value : strings) {
			if (StringUtils.isEmpty(value)) {
				containsNonEmpty = false;
				break;
			}
		}
	}

	public void sameBooleanValues1(List<String> strings) {
		boolean containsNonEmpty = true;
		for (String value : strings) {
			if (StringUtils.isEmpty(value)) {
				containsNonEmpty = true;
				break;
			}
		}
	}

	public void sameBooleanValues2(List<String> strings) {
		boolean containsNonEmpty = false;
		for (String value : strings) {
			if (StringUtils.isEmpty(value)) {
				containsNonEmpty = false;
				break;
			}
		}
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
}
