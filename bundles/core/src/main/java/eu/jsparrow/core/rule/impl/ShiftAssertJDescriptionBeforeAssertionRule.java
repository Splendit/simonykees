package eu.jsparrow.core.rule.impl;

import java.time.Duration;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.assertj.ShiftAssertJDescriptionBeforeAssertionASTVisitor;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

public class ShiftAssertJDescriptionBeforeAssertionRule extends RefactoringRuleImpl<ShiftAssertJDescriptionBeforeAssertionASTVisitor>{

	public ShiftAssertJDescriptionBeforeAssertionRule() {
		this.visitorClass = ShiftAssertJDescriptionBeforeAssertionASTVisitor.class;
		this.id = "ShiftAssertJDescriptionBeforeAssertion"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription("Shift AssertJ Description Before Assertion",
				"AssertJ Description only make senese to be invoked before the assertion itself. Otherwise it has no effect.", Duration.ofMinutes(5),
				Tag.JAVA_1_5, Tag.TESTING, Tag.ASSERTJ, Tag.CODING_CONVENTIONS);
	}
	
	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_5;
	}

}
