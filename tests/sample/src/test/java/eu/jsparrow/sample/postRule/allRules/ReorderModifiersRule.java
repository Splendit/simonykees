package eu.jsparrow.sample.postRule.allRules;

public class ReorderModifiersRule {

	/*
	 * Three modifiers
	 */
	public static final String FINAL_STATIC_PUBLIC = "";
	public static final String STATIC_FINAL_PUBLIC = "";
	public static final String FINAL_PUBLIC_STATIC = "";
	public static final String PUBLIC_FINAL_STATIC = "";
	public static final String STATIC_PUBLIC_FINAL = "";
	public static final String PUBLIC_STATIC_FINAL = "";
	static String STATIC = "";
	static String STATIC2;
	/*
	 * Two modifiers
	 */
	public final String FINAL_PUBLIC = "";
	public final String PUBLIC_FINAL = "";
	/*
	 * One modifier
	 */
	public String PUBLIC = "";
	final String FINAL = "";
	final String FINAL2 = "";
	/*
	 * No modifier
	 */
	String noModifier;

	protected static final synchronized void staticPublicFinalSynchronizedMethod() {
		/*
		 * should reorder modifiers
		 */
	}

	private static class PrivateStaticClass {
		/*
		 * Should reorder modifiers
		 */
	}

	/*
	 * Modifiers in interface methods
	 */
	interface ModifiersInInterfaceMethods {
		default void defaultMethod() {
			/*
			 * Should reorder modifiers
			 */
		}
	}

}
