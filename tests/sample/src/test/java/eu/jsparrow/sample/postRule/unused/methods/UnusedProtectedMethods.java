package eu.jsparrow.sample.postRule.unused.methods;

public class UnusedProtectedMethods {

	protected void unusedOverriden() {
		// do nothing
	}

	protected void usedInternally() {
		// do nothing
	}

	protected void usedExternally() {
		// do nothing
	}

	public static void main(String[] args) {
		UnusedProtectedMethods object = new UnusedProtectedMethods();
		object.usedInternally();
	}
}
