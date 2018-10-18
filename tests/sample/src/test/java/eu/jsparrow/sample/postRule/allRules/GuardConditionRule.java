package eu.jsparrow.sample.postRule.allRules;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GuardConditionRule {

	private static final Logger logger = LoggerFactory.getLogger(GuardConditionRule.class);

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

	private boolean condition() {
		return true;
	}

	private int numericCondition() {
		return 0;
	}

	private void doSomething(String value) {
		logger.info(value);
	}

}
