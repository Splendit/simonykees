package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.impl.GuardConditionASTVisitor;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

public class GuardConditionRule extends RefactoringRuleImpl<GuardConditionASTVisitor> {
	
	public GuardConditionRule () {
		
		this.visitorClass = GuardConditionASTVisitor.class;
		this.id = "GuardCondition"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription("Use Guard Condition", //$NON-NLS-1$
				"Use a guard condition instead of embedding the whole body of a method inside an if statement", Duration.ofMinutes(2), //$NON-NLS-1$
				Arrays.asList(Tag.JAVA_1_1, Tag.READABILITY, Tag.CODING_CONVENTIONS));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_1;
	}

}
