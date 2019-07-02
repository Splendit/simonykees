package eu.jsparrow.sample.preRule;

public class LocalVariableTypeInferenceRule {
	
	
	public void visit_initializedWithSubtypeAndUsedInOverloadedMethod_shouldNotTransform() {
		Number number = new Integer(4);
		overloadedMethod(number);
	}
	
	
	
	public void overloadedMethod() {
		
	}
	
	public void overloadedMethod(String value) {
		
	}
	
	public void overloadedMethod(Integer integer) {
		
	}
	
	public void overloadedMethod(Number number) {
		
	}

}
