package eu.jsparrow.sample.preRule.unused.methods;

import java.util.List;

public class UnusedProtectedMethodsSubclass extends UnusedProtectedMethods<String> {
	void consume() {
		usedExternally();
	}

	@Override
	protected void unusedOverriden(String value) {
		// do nothing
	}

	protected void unusedOverridenOverloaded() {
		// do nothing
	}

	protected void unusedOverridenOverloaded(String stringValue, List<String> values, String value) {
		// do nothing
	}
	
	protected void unusedOverridenOverloaded(String stringValue, List<String> values, String value, String intValue) {
		// do nothing
	}
	
	protected void unusedOverridenOverloaded(String stringValue, List<String> values, List<String> values2, int intValue) {
		// do nothing
	}
	
	protected void unusedOverridenOverloaded(Integer boxedIntValue, List<String> values, List<String> values2, int intValue) {
		// do nothing
	}
	
	protected <E> void unusedOverridenOverloaded(String stringValue, List<String> values, String value, E intValue) {
		// do nothing
	}
	
	protected void unusedOverridenOverloaded(String stringValue, List<String> values, String value, int intValue) {
		// do nothing
	}

	public static void main(String[] args) {
		UnusedProtectedMethodsSubclass object = new UnusedProtectedMethodsSubclass();
		object.consume();
	}
}
