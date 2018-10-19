package eu.jsparrow.sample.postRule.guardCondition;

import java.util.List;

public class GuardConditionRule {
	
	/*
	 * void methods
	 */
	
	public void voidMethod_methodInvocationCondition_shouldTransform() {
		if (!condition()) {
			return;
		}
		doSomething("Should create guard condition with negation");
		doSomething("Should transform");
	}
	
	public void voidMethod_prefixExpressionCondition_shouldTransform() {
		if (condition()) {
			return;
		}
		doSomething("Should create guard condition without negation");
		doSomething("Should transform");
	}
	
	public void voidMethod_infixExpressionCondition_shouldTransform() {
		if (numericCondition() != 0) {
			return;
		}
		doSomething("Should create guard condition with not equals operator");
		doSomething("Should transform");
	}
	
	public void voidMethod_infixExpressionCondition2_shouldTransform() {
		if (numericCondition() == 0) {
			return;
		}
		doSomething("Should create guard condition with equals operator");
		doSomething("Should transform");
	}
	
	public void voidMethod_infixExpressionConditionLessThan_shouldTransform() {
		if (numericCondition() >= 0) {
			return;
		}
		doSomething("Should create guard condition with greater equals");
		doSomething("Should transform");
	}
	
	public void voidMethod_infixExpressionConditionLessThanEquals_shouldTransform() {
		if (numericCondition() > 0) {
			return;
		}
		doSomething("Should create guard condition with greater");
		doSomething("Should transform");
	}
	
	public void voidMethod_infixExpressionConditionBiggerThan_shouldTransform() {
		if (numericCondition() <= 0) {
			return;
		}
		doSomething("Should create guard condition with less equals");
		doSomething("Should transform");
	}
	
	public void voidMethod_infixExpressionConditionBiggerThanEquals_shouldTransform() {
		if (numericCondition() <= 0) {
			return;
		}
		doSomething("Should create guard condition with less");
		doSomething("Should transform");
	}
	
	public void voidMethod_statementsBeforeIf_shouldTransform() {
		doSomething("what ever");
		if (numericCondition() <= 0) {
			return;
		}
		doSomething("Should create guard condition with less");
		doSomething("Should transform");
	}
	
	public void voidMethod_compoundCondition_shouldTransform() {
		boolean a = true;
		boolean b = condition();
		boolean c = numericCondition() == 1;
		if (!(a == b == c)) {
			return;
		}
		doSomething("Should create negated guard condition");
		doSomething("Should transform");
	}
	
	public void voidMethod_logicalExpression_shouldTransform() {
		boolean a = true;
		boolean b = condition();
		boolean c = numericCondition() == 1;
		if (!(a && b || c)) {
			return;
		}
		doSomething("Should create negated guard condition");
		doSomething("Should transform");
	}
	
	public void voidMethod_negatedLogicalExpression_shouldTransform() {
		boolean a = true;
		boolean b = condition();
		boolean c = numericCondition() == 1;
		if (a && b || c) {
			return;
		}
		doSomething("Should remove negation from expression");
		doSomething("Should transform");
	}
	
	public void voidMethod_instanceOfOperator_shouldTransform(Object object) {
		if (!(object instanceof List)) {
			return;
		}
		doSomething("Object is a list");
		doSomething("Should transform");
	}
	

	
	/*
	 * if-{ ... return} return 
	 */
	
	public int ifWithReturn_followedByReturn_shouldTransform() {
		doSomething("what ever");
		if (!condition()) {
			return 0;
		}
		doSomething("Should be moved out of the if");
		doSomething("should transform");
		return 1;
	}
	
	/*
	 * if-{ ...} else {return} return 
	 */
	
	public int ifWithoutReturn_followedByElseReturn_shouldTransform() {
		doSomething("what ever");
		
		if (!condition()) {
			return 0;
		}
		doSomething("Should be moved out of the if");
		doSomething("should transform");
		return 1;
	}
	
	/*
	 * if-{ ...return } else {return} 
	 */
	
	public int ifWithReturn_followedByElseWithReturn_shouldTransform() {
		doSomething("what ever");
		
		if (!condition()) {
			return 0;
		}
		doSomething("Should be moved out of the if");
		doSomething("should transform");
		return 1;
	}
	
	public int methodWithReturnType_multipleElseBranches_shouldNotTransform() {
		if(numericCondition() > 0) {
			doSomething("Should create guard condition with less");
			doSomething("Should not transform");
			return 1;
		} else if(numericCondition() == 0) {
			return -1;
		} else {
			doSomething("Else branch");
		}
		return -1;
	}
	
	public void voidMethod_multipleElseBranches_shouldNotTransform() {
		if(numericCondition() > 0) {
			doSomething("Should create guard condition with less");
			doSomething("Should not transform");
		} else if(numericCondition() == 0) {
			doSomething("on else-if branch");
		} else {
			doSomething("Else branch");
		}
	}
	
	
	private boolean condition() {
		return true;
	}
	
	private int numericCondition() {
		return 0;
	}
	
	private void doSomething(String value) {
		
	}

}
