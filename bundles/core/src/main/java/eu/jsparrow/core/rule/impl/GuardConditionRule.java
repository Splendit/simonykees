package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.impl.GuardConditionASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see GuardConditionASTVisitor
 * 
 * @since 2.7.0
 *
 */
public class GuardConditionRule extends RefactoringRuleImpl<GuardConditionASTVisitor> {

	public static final String RULE_ID = "GuardCondition"; //$NON-NLS-1$

	public GuardConditionRule () {
		this.visitorClass = GuardConditionASTVisitor.class;
		this.id = RULE_ID;
		this.ruleDescription = new RuleDescription(Messages.GuardConditionRule_name,
				Messages.GuardConditionRule_description, Duration.ofMinutes(5),
				Arrays.asList(Tag.JAVA_1_1, Tag.READABILITY, Tag.CODING_CONVENTIONS));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_1;
	}

}
