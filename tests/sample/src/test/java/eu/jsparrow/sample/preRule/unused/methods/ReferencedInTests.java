package eu.jsparrow.sample.preRule.unused.methods;

public class ReferencedInTests {

	public String foo() {
		return "foo";
	}
	
	public void foo2() {}
	
	public String multipleReferencesInTheSameTestCase() {
		return "multiple invocations in the same test";
	}
}
