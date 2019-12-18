package eu.jsparrow.sample.postRule.allRules;

import java.util.List;

public class GuardConditionRule {

	/*
	 * void methods
	 */

	public void voidMethod_testComments_shouldTransform() {
		// 1
		doSomething("Whatever"); // 2

		// 3

		// 4
		/* 5 */
		/* 9 */
		/* 10 */
		/* 12 */
		/* 15 */
		if (!/* 6 */condition/* 7 */()/* 8 */) {
			return;
		}
		/* 11 */
		doSomething("Should create guard condition with negation");
		/* 13 */
		doSomething("Should transform");
		/* 14 */

		/* 16 */
	}

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
		if (numericCondition() < 0) {
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
		final boolean a = true;
		final boolean b = condition();
		final boolean c = numericCondition() == 1;
		if (!(a == b == c)) {
			return;
		}
		doSomething("Should create negated guard condition");
		doSomething("Should transform");
	}

	public void voidMethod_logicalExpression_shouldTransform() {
		final boolean a = true;
		final boolean b = condition();
		final boolean c = numericCondition() == 1;
		if (!(a && b || c)) {
			return;
		}
		doSomething("Should create negated guard condition");
		doSomething("Should transform");
	}

	public void voidMethod_negatedLogicalExpression_shouldTransform() {
		final boolean a = true;
		final boolean b = condition();
		final boolean c = numericCondition() == 1;
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

	public void voidMethod_booleanLiteralTrue_shouldTransform(Object object) {
		if (false) {
			return;
		}
		doSomething("Object is a list");
		doSomething("Should transform");
	}

	public void voidMethod_booleanLiteralFalse_shouldTransform(Object object) {
		if (true) {
			return;
		}
		doSomething("Object is a list");
		doSomething("Should transform");
	}

	public void voidMethod_infixBooleanExpression_shouldTransform(Object object) {
		final boolean a = false;
		if (!(condition() && a)) {
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

	public int ifWithReturn_testComments_shouldTransform() {
		/* 1 */
		doSomething("what ever"); /* 2 */
		/* 3 */

		/* 4 */

		/* 5 */
		/* 6 */
		/* 10 */
		/* 11 */
		/* 13 */
		/* 16 */
		if (!/* 7 */condition(/* 8 */)/* 9 */) {
			/* 17 */
			return 0;
		}
		/* 12 */
		doSomething("Should be moved out of the if");
		/* 14 */
		doSomething("should transform");
		/* 15 */
		return 1;

		/* 18 */
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
		if (numericCondition() > 0) {
			doSomething("Should create guard condition with less");
			doSomething("Should not transform");
			return 1;
		} else if (numericCondition() == 0) {
			return -1;
		} else {
			doSomething("Else branch");
		}
		return -1;
	}

	public void voidMethod_multipleElseBranches_shouldNotTransform() {
		if (numericCondition() > 0) {
			doSomething("Should create guard condition with less");
			doSomething("Should not transform");
		} else if (numericCondition() == 0) {
			doSomething("on else-if branch");
		} else {
			doSomething("Else branch");
		}
	}

	public int ifWithoutReturn_followedByElseReturn_shouldNotTransform() {
		doSomething("what ever");

		if (condition()) {
			doSomething("Should be moved out of the if");
			doSomething("should transform");
		} else {
			return 0;
		}
		{
			return 1;
		}
	}

	public void voidMethod_trivialBody_shouldNotTransform() {
		doSomething("Whatever");
		if (numericCondition() > 0) {
			doSomething("Trivial, should not transform");
		}
	}

	public void voidMethod_lastIsNotIf_shouldNotTransform() {
		if (numericCondition() > 0) {
			doSomething("Not trivial");
			doSomething("There are statements following the if");
		}
		doSomething("Whatever");
	}

	public int ifReturnElseReturn_notExplicitIfReturn_shouldNotTransform() {
		if (condition()) {
			doSomething("Not trivial");
			doSomething("else branch does not return explicitly");
			{
				return 0;
			}
		} else {
			return 1;
		}
	}

	public int ifReturnElseReturn_elseIsReturnStatement_shouldTransform() {
		if (!condition()) {
			return 1;
		}
		doSomething("Not trivial");
		doSomething("else branch does not return explicitly");
		return 0;
	}

	public int ifReturnElseReturn_elseWithMultipleStatements_shouldTransform() {
		if (condition()) {
			doSomething("Not trivial");
			doSomething("else branch does not return explicitly");
			return 0;
		} else {
			doSomething("Here as well");
			return 1;
		}
	}

	public int ifReturn_missingifReturnStatement_shouldNotTransform() {
		if (condition()) {
			doSomething("Not trivial");
			doSomething("Next statement is not return");
			doSomething("Should not transform");

		}
		return 1;
	}

	public int ifReturn_thenSingleReturnStatement_shouldNotTransform() {
		doSomething("Whatever ");
		if (condition()) {
			return 0;
		}
		return 1;
	}

	public int ifReturnElseReturn_trivialBody_shouldNotTransform() {
		if (condition()) {
			return 0;
		} else {
			return 1;
		}
	}

	public int ifWithoutReturn_followedElseWithoutReturn_shouldNotTransform() {
		doSomething("what ever");

		if (condition()) {
			doSomething("Should be moved out of the if");
			doSomething("should transform");
		} else {
			doSomething("what ever else");
		}
		return 1;
	}

	public int ifWithReturn_multipleElseWithReturn_shouldNotTransform() {
		doSomething("what ever");

		if (condition()) {
			doSomething("Should be moved out of the if");
			doSomething("should transform");
			return 1;
		} else if (numericCondition() == 1) {
			doSomething("what ever else");
			return 0;
		} else {
			return -1;
		}
	}

	public int noIf_shouldNotTransform() {
		doSomething("what ever");

		if (condition()) {
			doSomething("Should be moved out of the if");
			doSomething("should transform");
			return 1;
		}
		doSomething("Do something before returning");
		return 0;
	}

	public int trivialIfStatement_shouldNotTransform() {
		doSomething("what ever");

		if (condition()) {
			return 0;
		}
		return 1;
	}

	public void emptyMethod() {

	}

	public void emptyIfStatementMethod_shouldNotTransform() {
		doSomething("what ever");
		if (condition()) {

		}
	}

	private boolean condition() {
		return true;
	}

	private int numericCondition() {
		return 0;
	}

	private void doSomething(String value) {
		value.chars();
	}

	abstract class AbstractClass {
		public AbstractClass() {
			/*
			 * constructor
			 */
			if (numericCondition() == 0) {
				doSomething("Should create guard condition with not equals operator");
				doSomething("Should transform");
			}
		}

		abstract void abstractMethod();
	}

}
