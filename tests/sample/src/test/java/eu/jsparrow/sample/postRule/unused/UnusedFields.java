package eu.jsparrow.sample.postRule.unused;

import java.io.Serializable;

public class UnusedFields implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	
	public String publicFieldUsedExternally = "";//5
	public String publicFieldUsedInternally = "";//6
	public String publicFieldUsedInternallyAndExternally = "";//7
	
	
	private String privateFieldUsedInternally = "";
	
	void reassignemnts() {
		publicFieldUsedInternallyAndExternally = "new value"; //14
	}
	
	void blackHole() {
		System.out.println(publicFieldUsedInternally);
		System.out.println(publicFieldUsedInternallyAndExternally);
		System.out.println(privateFieldUsedInternally);
	}
}
