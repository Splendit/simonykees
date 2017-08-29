package at.splendit.simonykees.sample.preRule;

import java.math.RoundingMode;

@SuppressWarnings({ "nls" })
public class TestEnumsWithoutEqualsRule {

	public void replaceEqualsWithInfix(RoundingMode roundingMode) {
		if (roundingMode.equals(RoundingMode.UP)) {
			return;
		}
		if (RoundingMode.UP.equals(roundingMode)) {
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
}
