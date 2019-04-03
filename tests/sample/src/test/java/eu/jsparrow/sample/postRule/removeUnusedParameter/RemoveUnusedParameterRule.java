package eu.jsparrow.sample.postRule.removeUnusedParameter;

@SuppressWarnings("nls")
public class RemoveUnusedParameterRule {
	
	public void visit_publicMethod_shouldNotTransform(String unusedParameter) {
		
	}
	
	protected void visit_protectedMethod_shouldNotTransform(String unusedParameter) {
		
	}
	
	void visit_packagePrivateMethod_shouldNotTransform(String unusedParameter) {
		
	}
	
	private void visit_privateMethod_shouldTransform() {
		
	}
	
	private void visit_overloadedMethod_shouldNotTransform(String unusedParameter) {
		
	}
	
	private void visit_overloadedMethod_shouldNotTransform(String unusedParameter, String unused) {
		
	}
	
	private native void visit_nativeOverloadedMethod_shouldNotTransform(String unusedParameter);
	
	private void visit_methodWithoutParameters_shouldNotTranform() {
		
	}
	
	private void visit_multipleUnusedParameters_shouldTranform(String second) {
		/*
		 * Should remove first and second
		 */
		consume(second);
	}
	
	
	private void consume(String value) {
		value.chars();
	}
	
	private void visit_sameSignatureWithInnerclassMethod_shouldTransformInInnerClass(String value) {
		consume(value);
	}
	
	private void invokeTransformedMethods() {
		String first = "first";
		visit_multipleUnusedParameters_shouldTranform("second");
		visit_privateMethod_shouldTransform();
		visit_sameSignatureWithInnerclassMethod_shouldTransformInInnerClass(first);
	}
	
	private void secondaryInvocations() {
		String first = "first";
		visit_multipleUnusedParameters_shouldTranform("second");
		visit_privateMethod_shouldTransform();
		visit_sameSignatureWithInnerclassMethod_shouldTransformInInnerClass(first);
	}
	
	class InnerClass {
		
		private void visit_sameSignatureWithInnerclassMethod_shouldTransformInInnerClass() {
			
		}
		
		public void invokeInInnerClass() {
			String first = "first";
			visit_sameSignatureWithInnerclassMethod_shouldTransformInInnerClass();
			visit_overloadedInInnerClass_shouldNotTransform(first);
			visit_overloadedInInnerClass_shouldNotTransform(1);
		}
		
		private void visit_overloadedInInnerClass_shouldNotTransform(String unusedParameter) {
			
		}
		
		private void visit_overloadedInInnerClass_shouldNotTransform(int unusedParameter) {
			
		}
	}
}
