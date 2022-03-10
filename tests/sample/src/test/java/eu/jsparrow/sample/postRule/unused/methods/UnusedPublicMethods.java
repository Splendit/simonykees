package eu.jsparrow.sample.postRule.unused.methods;

public class UnusedPublicMethods {
	
	public UnusedPublicMethods() {
		/*
		 * Default constructor - should not be removed. 
		 */
	}
	
	public UnusedPublicMethods(String value) {
		/*
		 * Initially, the constructors are not removed. 
		 */
	}

	public void usedInExpressionMethodReference() {
		// do nothing
	}
	
	public void usedInTypeMethodReference() {
		// do nothing
	}
	
	public void usedInSuperMethodReference() {
		// do nothing
	}
	
	public void usedInMethodInvocationInternally() {
		// do nothing
	}
	
	public void usedInMethodInvocationExternally() {
		// do nothing
	}
	
	public void usedInSuperMethodInvocation() {
		// do nothing
	}
	
	void blackHole() {
		usedInMethodInvocationInternally();
		Runnable r = this::usedInExpressionMethodReference;
		r.run();
	}
	
	public static void main(String[]args) {
		UnusedPublicMethods unusedPublicMethod = new UnusedPublicMethods();
		unusedPublicMethod.blackHole();
	}
	
	public boolean equals(Object other) {
		return false;
	}
}
