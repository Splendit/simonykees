package eu.jsparrow.sample.preRule;

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
	 * 
	 * @param unusedParameter to be removed
	 */
	private void visit_privateMethod_shouldTransform(String unusedParameter) {
		
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
	 * @param first
	 *            should be removed
	 * @param second
	 *            should remain
	 * @param third
	 *            should be removed
	 * 
	 * @return empty string
	 */
	private String visit_multipleUnusedParameters_shouldTransform(String first, String second, String third) {
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
	
	private void invokeTransformedMethods() {
		String first = "first";
		visit_multipleUnusedParameters_shouldTransform(first, "second", "third");
		visit_privateMethod_shouldTransform(first);
		visit_sameSignatureWithInnerClassMethod_shouldTransformInInnerClass(first);
		
		/*
		 * Save comments
		 */
		visit_multipleUnusedParameters_shouldTransform(/*1*/first/*2*/, /*3*/"second"/*4*/, /*5*/"third"/*6*/);
	}
	
	private void secondaryInvocations() {
		String first = "first";
		visit_multipleUnusedParameters_shouldTransform(first, "second", "third");
		visit_privateMethod_shouldTransform(first);
		visit_sameSignatureWithInnerClassMethod_shouldTransformInInnerClass(first);
	}
	
	class InnerClass {
		
		private void visit_sameSignatureWithInnerClassMethod_shouldTransformInInnerClass(String value) {
			
		}
		
		public void invokeInInnerClass() {
			String first = "first";
			visit_sameSignatureWithInnerClassMethod_shouldTransformInInnerClass(first);
			visit_overloadedInInnerClass_shouldNotTransform(first);
			visit_overloadedInInnerClass_shouldNotTransform(1);
		}
		
		private void visit_overloadedInInnerClass_shouldNotTransform(String unusedParameter) {
			
		}
		
		private void visit_overloadedInInnerClass_shouldNotTransform(int unusedParameter) {
			
		}
	}
}
