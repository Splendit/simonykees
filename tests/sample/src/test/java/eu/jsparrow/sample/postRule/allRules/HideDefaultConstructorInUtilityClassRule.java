package eu.jsparrow.sample.postRule.allRules;

public class HideDefaultConstructorInUtilityClassRule {

	private HideDefaultConstructorInUtilityClassRule() {
		throw new IllegalStateException("Utility class");
	}

	public static void sampleMethod() {
	}

	public static void foo() {
	}
}

abstract class AbstractClass {

	public static void foo() {
	}

}
