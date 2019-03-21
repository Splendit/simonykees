package eu.jsparrow.sample.postRule.removeModifiersInInterfaceProperties;

public interface RemoveModifiersInInterfacePropertiesRule {
	String PUBLIC_FIELD = "";
	
	String PUBLIC_STATIC_FIELD = "";
	
	String PUBLIC_STATIC_FINAL_FIELD = "";
	
	
	void method();
	
	void publicMethod();
	
	static void publicStaticMethod() {
		
	}
	
	default void publicDefaultVoidMethod() {
		
	}
}
