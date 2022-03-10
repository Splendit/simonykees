package eu.jsparrow.sample.preRule.unused.methods;

public class UnusedProtectedMethodsSubclass extends UnusedProtectedMethods {
	void consume() {
		usedExternally();
	}

	@Override
	protected void unusedOverriden() {
		// do nothing
	}

	public static void main(String[] args) {
		UnusedProtectedMethodsSubclass object = new UnusedProtectedMethodsSubclass();
		object.consume();
	}
}
