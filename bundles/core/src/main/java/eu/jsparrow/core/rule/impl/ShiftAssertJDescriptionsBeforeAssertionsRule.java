package eu.jsparrow.core.rule.impl;

import java.time.Duration;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.assertj.ShiftAssertJDescriptionsBeforeAssertionsASTVisitor;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

public class ShiftAssertJDescriptionsBeforeAssertionsRule extends RefactoringRuleImpl<ShiftAssertJDescriptionsBeforeAssertionsASTVisitor>{

	public ShiftAssertJDescriptionsBeforeAssertionsRule() {
		this.visitorClass = ShiftAssertJDescriptionsBeforeAssertionsASTVisitor.class;
		this.id = "ShiftAssertJDescriptionsBeforeAssertions"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription("Shift AssertJ Descriptions Before Assertions",
				"AssertJ Description only make senese to be invoked before the assertion itself. Otherwise it has no effect.", Duration.ofMinutes(5),
				Tag.TESTING, Tag.CODING_CONVENTIONS);
	}
	
	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_5;
	}

}
