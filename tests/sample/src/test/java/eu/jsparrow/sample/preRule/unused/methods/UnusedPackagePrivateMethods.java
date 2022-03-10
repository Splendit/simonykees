package eu.jsparrow.sample.preRule.unused.methods;

public class UnusedPackagePrivateMethods {

	void unusedMethod() {
		// do nothing
	}

	void usedInternally() {
		// do nothing
	}

	void usedExternally() {
		// do nothing
	}
	
	public static void main(String[]args) {
		UnusedPackagePrivateMethods unused = new UnusedPackagePrivateMethods();
		unused.usedInternally();
	}
}
