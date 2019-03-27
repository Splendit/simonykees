package eu.jsparrow.sample.preRule;

public interface RemoveModifiersInInterfacePropertiesRule {
	public String PUBLIC_FIELD = "";
	
	public static String PUBLIC_STATIC_FIELD = "";
	
	public static final String PUBLIC_STATIC_FINAL_FIELD = "";
	
	
	void method();
	
	public void publicMethod();
	
	public static void publicStaticMethod() {
		
	}
	
	public default void publicDefaultVoidMethod() {
		
	}
}
