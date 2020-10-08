package eu.jsparrow.sample.postRule.removeUnusedParameter;

@SuppressWarnings("nls")
public class RemoveUnusedParameterRule {
	
	private RemoveUnusedParameterRule(String unusedParameter) {
		
	}
	
	public void visit_publicMethod_shouldNotTransform(String unusedParameter) {
		
	}
	
	protected void visit_protectedMethod_shouldNotTransform(String unusedParameter) {
		
	}
	
	void visit_packagePrivateMethod_shouldNotTransform(String unusedParameter) {
		
	}
	
	/**
	 * Main text 
	 */
	private void visit_privateMethod_shouldTransform() {
		
	}
	
	private void visit_overloadedMethod_shouldNotTransform(String unusedParameter) {
		
	}
	
	private void visit_overloadedMethod_shouldNotTransform(String unusedParameter, String unused) {
		
	}
	
	private native void visit_nativeOverloadedMethod_shouldNotTransform(String unusedParameter);
	
	private void visit_methodWithoutParameters_shouldNotTransform() {
		
	}
	
	/**
	 * Main content
	 * 
	 * @param second
	 *            should remain
	 * @return empty string
	 */
	private String visit_multipleUnusedParameters_shouldTransform(String second) {
		/*
		 * Should remove first and second
		 */
		consume(second);
		return "";
	}
	
	
	private void consume(String value) {
		value.chars();
	}
	
	private void visit_sameSignatureWithInnerClassMethod_shouldTransformInInnerClass(String value) {
		consume(value);
	}

	private void unusedFinalParameter_shouldTransform() {}

	private void unusedAnnotatedParameter_shouldNotTransform(@SuppressWarnings("unused") String unusedParameter) {}
	
	private void invokeTransformedMethods() {
		String first = "first";
		visit_multipleUnusedParameters_shouldTransform("second");
		visit_privateMethod_shouldTransform();
		visit_sameSignatureWithInnerClassMethod_shouldTransformInInnerClass(first);
		
		/*1*/
		/*2*/
		/*5*/
		/*6*/
		/*
		 * Save comments
		 */
		visit_multipleUnusedParameters_shouldTransform(/*3*/"second"/*4*/);
	}
	
	private void secondaryInvocations() {
		String first = "first";
		visit_multipleUnusedParameters_shouldTransform("second");
		visit_privateMethod_shouldTransform();
		visit_sameSignatureWithInnerClassMethod_shouldTransformInInnerClass(first);
	}
	
	class InnerClass {
		
		private void visit_sameSignatureWithInnerClassMethod_shouldTransformInInnerClass() {
			
		}
		
		public void invokeInInnerClass() {
			String first = "first";
			visit_sameSignatureWithInnerClassMethod_shouldTransformInInnerClass();
			visit_overloadedInInnerClass_shouldNotTransform(first);
			visit_overloadedInInnerClass_shouldNotTransform(1);
		}
		
		private void visit_overloadedInInnerClass_shouldNotTransform(String unusedParameter) {
			
		}
		
		private void visit_overloadedInInnerClass_shouldNotTransform(int unusedParameter) {
			
		}
	}
}
