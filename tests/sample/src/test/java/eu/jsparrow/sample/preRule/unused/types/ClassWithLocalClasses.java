package eu.jsparrow.sample.preRule.unused.types;

public class ClassWithLocalClasses {
	
	void methodWithLocalClasses() {
		class UsedLocalClass {
		}

		class UnusedLocalClass {
		}
		UsedLocalClass usedLocalClass;
	}

	private void unusedPrivateMethodWithLocalClasses() {
		class UsedLocalClassInPrivateUnusedMethod {
		}
	}
}
