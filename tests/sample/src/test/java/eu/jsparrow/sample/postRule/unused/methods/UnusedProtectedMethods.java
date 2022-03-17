package eu.jsparrow.sample.postRule.unused.methods;

import java.util.List;

public class UnusedProtectedMethods<T> {

	protected void unusedOverriden(String value) {
		// do nothing
	}

	protected void unusedOverridenOverloaded(String stringValue, List<String> values, T value, int intValue) {
		// do nothing
	}

	protected void usedInternally() {
		// do nothing
	}

	protected void usedExternally() {
		// do nothing
	}

	public static void main(String[] args) {
		UnusedProtectedMethods<String> object = new UnusedProtectedMethods<String>();
		object.usedInternally();
	}
}
