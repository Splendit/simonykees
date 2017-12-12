package eu.jsparrow.sample.postRule.renaming.publicFieldRenaming;

@SuppressWarnings({"unused", "nls"})
public class VariousModifiers {
	/*
	 * Non final fields 
	 */
	public String publicField;
	protected String protectedField;
	String packagePrivateField;
	private String Private_field;
	
	/*
	 * Final fields 
	 */
	public final String publicFinalField;
	protected final String protectedFinalField;
	final String packagePrivateWithFinalModifier;
	private final String private_final_field;
	
	/*
	 * static fields should be renamed
	 */
	public static String publicStaticField = "";
	protected static String protectedStaticField = "";
	static String packageFinalStaticField = "";
	private static String PRIVATE_STATIC_FIELD = "";
	
	/*
	 * static final fields
	 */
	public static final String PUBLIC_STATIC_FINAL_FIELD = "";
	protected static final String PROTECTED_STATIC_FINAL_FIELD = "";
	static final String PACKAGE_FINAL_STATIC_FINAL_FIELD = "";
	private static final String PRIVATE_STATIC_FINAL_FIELD = "";
	
	public VariousModifiers() {
		publicField = "";
		protectedField = "";
		packagePrivateField = "";
		Private_field = "";
		
		publicFinalField = "";
		protectedFinalField = "";
		packagePrivateWithFinalModifier = "";
		private_final_field = "";
	}
}
