package eu.jsparrow.sample.postRule.hideDefaultConstructor;

public class HideDefaultConstructorInUtilityClassRule {

	private HideDefaultConstructorInUtilityClassRule() {
		throw new IllegalStateException("Utility class");
	}

	public static void sampleMethod() {}

	public static void foo() {}
}
