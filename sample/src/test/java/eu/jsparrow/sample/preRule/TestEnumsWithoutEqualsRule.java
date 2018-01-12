package eu.jsparrow.sample.preRule;

import java.math.RoundingMode;

public class TestEnumsWithoutEqualsRule {

	public void replaceEqualsWithInfix(RoundingMode roundingMode) {
		if (roundingMode.equals(RoundingMode.UP)) {
			return;
		}
		if (RoundingMode.UP.equals(roundingMode)) {
			return;
		}
		if (!roundingMode.equals(RoundingMode.UP)) {
			return;
		}
		if (!RoundingMode.UP.equals(roundingMode)) {
			return;
		}
		if (roundingMode.equals(RoundingMode.UP) && true) {
			return;
		}
		if (RoundingMode.UP.equals(roundingMode) && true) {
			return;
		}
		if (RoundingMode.UP. /* saving comments */ equals(roundingMode)) {
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
