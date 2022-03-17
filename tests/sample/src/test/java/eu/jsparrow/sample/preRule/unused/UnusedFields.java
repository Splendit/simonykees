package eu.jsparrow.sample.preRule.unused;

import java.io.Serializable;

public class UnusedFields implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	
	public String publicUnusedField = "";//1
	public String publicFieldReassignedInternally = "";//2
	public String publicFieldReassignedExternally = "";//3
	public String publicFieldReassignedInternallyAndExternally = "";//4
	
	public String publicFieldUsedExternally = "";//5
	public String publicFieldUsedInternally = "";//6
	public String publicFieldUsedInternallyAndExternally = "";//7
	
	
	protected String protectedUnusedField = "";//8
	protected String protectedReassignedField = "";//9
	
	String packageProtectedUnusedField = "";//10
	


	private String privateUnusedField = "";//11
	private String privateFieldReassignedInternally = "";//12
	private String privateFieldUsedInternally = "";
	
	@Deprecated
	private String unusedDeprecatedAnnoation = "";
	
	@SuppressWarnings("unused")
	private String unusedAnnotation = "";
	
	void reassignemnts() {
		publicFieldReassignedInternally = "value"; //13
		publicFieldUsedInternallyAndExternally = "new value"; //14
		publicFieldReassignedInternallyAndExternally = ""; //15
		protectedReassignedField = ""; //16
	}
	
	void blackHole() {
		System.out.println(publicFieldUsedInternally);
		System.out.println(publicFieldUsedInternallyAndExternally);
		System.out.println(privateFieldUsedInternally);
	}
}
