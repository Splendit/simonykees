package eu.jsparrow.sample.postRule.allRules;

public interface RemoveModifiersInInterfacePropertiesRule {
	String PUBLIC_STATIC_FIELD = "";

	String PUBLIC_STATIC_FINAL_FIELD = "";

	String PUBLIC_FIELD = "";

	void method();

	void publicMethod();

	static void publicStaticMethod() {

	}

	default void publicDefaultVoidMethod() {

	}
}
