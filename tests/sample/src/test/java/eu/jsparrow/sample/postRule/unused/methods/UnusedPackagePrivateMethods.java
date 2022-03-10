package eu.jsparrow.sample.postRule.unused.methods;

public class UnusedPackagePrivateMethods {

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
