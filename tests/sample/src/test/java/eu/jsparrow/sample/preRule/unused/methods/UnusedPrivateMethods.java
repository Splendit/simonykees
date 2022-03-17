package eu.jsparrow.sample.preRule.unused.methods;

public class UnusedPrivateMethods {

	private void unusedPrivateMethod() {
		
	}
	
	private void usedInMethodReference() {
		// Do nothing
	}
	
	private void usedWithMethodInvocation() {
		// Do nothing
	}
	
	void blackHole() {
		Runnable r = this::usedInMethodReference;
		r.run();
		usedWithMethodInvocation();
	}
	
	public static void main(String[]args) {
		UnusedPrivateMethods unusedPrivateMethods = new UnusedPrivateMethods();
		unusedPrivateMethods.blackHole();
	}
}
