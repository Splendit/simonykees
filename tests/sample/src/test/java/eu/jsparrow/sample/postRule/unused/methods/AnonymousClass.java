package eu.jsparrow.sample.postRule.unused.methods;

public class AnonymousClass {
	void foo() {
		AnonymousParentClass anonymousParent = new AnonymousParentClass() {
			public void overridenInAnonymousClass() {
				System.out.println("Overriding overridenInAnonymousClass method");
			}
		};
		anonymousParent.toString();
	}
	
	public static void main(String []args) {
		AnonymousClass anonymousClass = new AnonymousClass();
		anonymousClass.foo();
	}
}
