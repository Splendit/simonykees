package eu.jsparrow.sample.preRule.unused;

public class UnusedLocalVariables {
	
	void unusedWithReassignment() {
		int x;
		x = 1;
	}
	
	void usedInMethodInvocation() {
		int x = 1;
		System.out.println(x);
	}
}
