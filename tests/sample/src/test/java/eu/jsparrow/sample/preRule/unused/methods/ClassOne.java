package eu.jsparrow.sample.preRule.unused.methods;

public class ClassOne implements InterfaceOne {
	
	public ClassOne() {
		usedMethod();
	}

	@Override
	public void interfaceMethod() {
		System.out.println("In ClassOne");		
	}
	
	
	public void usedMethod() {
		System.out.println("Used method");
	}
}
