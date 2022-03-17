package eu.jsparrow.sample.preRule.unused.methods;

public class UnusedPublicMethodsSubclass extends UnusedPublicMethods {

	void consume() {
		super.usedInSuperMethodInvocation();
		Runnable r = super::usedInSuperMethodReference;
		r.run();
	}
	
	public static void main(String[]args) {
		UnusedPublicMethodsSubclass object = new UnusedPublicMethodsSubclass();
		object.consume();
	}
}
