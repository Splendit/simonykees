package eu.jsparrow.sample.preRule;

public class ReorderModifiersRule {
	
	/*
	 * Three modifiers
	 */
	final static public String FINAL_STATIC_PUBLIC = "";  
	static final public String STATIC_FINAL_PUBLIC = "";  
	final public static String FINAL_PUBLIC_STATIC = "";  
	public final static String PUBLIC_FINAL_STATIC = "";  
	static public final String STATIC_PUBLIC_FINAL = "";  
	public static final String PUBLIC_STATIC_FINAL = "";  
	
	/*
	 * Two modifiers
	 */
	final public String FINAL_PUBLIC = "";
	public final String PUBLIC_FINAL = "";
	
	/*
	 * One modifier
	 */
	public String PUBLIC = "";
	static String STATIC = "", STATIC2;
	final String FINAL = "", FINAL2 = "";
	
	/*
	 * No modifier
	 */
	String noModifier;
	
	static protected final synchronized void staticPublicFinalSynchronizedMethod() {
		/*
		 * should reorder modifiers
		 */
	}
	
	static private class PrivateStaticClass {
		/*
		 * Should reorder modifiers
		 */
	}
	
	/*
	 * Modifiers in interface methods
	 */
	interface ModifiersInInterfaceMethods {
		default public void defaultMethod () {
			/*
			 * Should reorder modifiers
			 */
		}
	}

}
