package eu.jsparrow.sample.postRule.unused.methods;

public class UnusedPublicMethods {
	
	private Runnable r = () -> useInInitializer();
	
	public UnusedPublicMethods() {
		/*
		 * Default constructor - should not be removed. 
		 */
		r.run();
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
	
	public void useInInitializer() {
		// do nothing
	}

	public boolean equals(Object other) {
		return false;
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
}
