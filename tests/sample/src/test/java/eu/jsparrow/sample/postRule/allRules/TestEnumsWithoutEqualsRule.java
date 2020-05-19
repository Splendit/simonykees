package eu.jsparrow.sample.postRule.allRules;

import java.math.RoundingMode;

public class TestEnumsWithoutEqualsRule {

	public void replaceEqualsWithInfix(RoundingMode roundingMode) {
		if (roundingMode == RoundingMode.UP) {
			return;
		}
		if (RoundingMode.UP == roundingMode) {
			return;
		}
		if (roundingMode != RoundingMode.UP) {
			return;
		}
		if (RoundingMode.UP != roundingMode) {
			return;
		}
		if (roundingMode == RoundingMode.UP && true) {
			return;
		}
		if (RoundingMode.UP == roundingMode && true) {
			return;
		}
		/* saving comments */
		if (RoundingMode.UP == roundingMode) {
			return;
		}
	}

	public void noEnumsShouldNotBeReplaced(String item) {
		if (item.equals(RoundingMode.UP)) {
			return;
		}
		if (RoundingMode.UP.equals(item)) {
			return;
		}
	}

	public void otherMethodsShouldNotBeReplaced(RoundingMode roundingMode) {
		if (roundingMode.compareTo(RoundingMode.UP) > 0) {
			return;
		}
		if (RoundingMode.UP.compareTo(roundingMode) < 0) {
			return;
		}
	}

	public void savingComments(RoundingMode roundingMode) {

		/* saving comments */
		if (RoundingMode.UP == roundingMode) {
			return;
		}

		if (RoundingMode.UP // I don't want to break anything
				== roundingMode) {
			return;
		}

		/* invocation comment */
		/* leading comment */
		/* trailing comments */
		if (RoundingMode.UP /* expression comment */ == /* param comment */ roundingMode) {
			return;
		}
	}
}
