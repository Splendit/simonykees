package eu.jsparrow.sample.preRule.renaming;

@SuppressWarnings({"unused", "nls"})
public class VariousModifiers {
	/*
	 * Non final fields 
	 */
	public String Public_field;
	protected String Protected_field;
	String package_private_field;
	private String Private_field;
	
	/*
	 * Final fields 
	 */
	public final String public_final_field;
	protected final String protected_final_field;
	final String package_private_with_final_modifier;
	private final String private_final_field;
	
	/*
	 * static fields should be renamed
	 */
	public static String PUBLIC_STATIC_FIELD = "";
	protected static String PROTECTED_STATIC_FIELD = "";
	static String PACKAGE_FINAL_STATIC_FIELD = "";
	private static String PRIVATE_STATIC_FIELD = "";
	
	/*
	 * static final fields
	 */
	public static final String PUBLIC_STATIC_FINAL_FIELD = "";
	protected static final String PROTECTED_STATIC_FINAL_FIELD = "";
	static final String PACKAGE_FINAL_STATIC_FINAL_FIELD = "";
	private static final String PRIVATE_STATIC_FINAL_FIELD = "";
	
	public VariousModifiers() {
		Public_field = "";
		Protected_field = "";
		package_private_field = "";
		Private_field = "";
		
		public_final_field = "";
		protected_final_field = "";
		package_private_with_final_modifier = "";
		private_final_field = "";
	}
}
