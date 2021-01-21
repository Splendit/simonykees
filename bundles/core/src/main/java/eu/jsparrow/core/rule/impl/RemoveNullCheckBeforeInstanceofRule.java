package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.impl.RemoveNullCheckBeforeInstanceofASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see RemoveNullCheckBeforeInstanceofASTVisitor
 * 
 * @since 3.8.0
 */
public class RemoveNullCheckBeforeInstanceofRule
		extends RefactoringRuleImpl<RemoveNullCheckBeforeInstanceofASTVisitor> {

	public static final String REMOVE_NULL_CHECKS_BEFORE_INSTANCE_OF_RULE_ID = "RemoveNullCheckBeforeInstanceof"; //$NON-NLS-1$

	public RemoveNullCheckBeforeInstanceofRule() {
		this.visitorClass = RemoveNullCheckBeforeInstanceofASTVisitor.class;
		this.id = REMOVE_NULL_CHECKS_BEFORE_INSTANCE_OF_RULE_ID;
		this.ruleDescription = new RuleDescription(Messages.RemoveNullCheckBeforeInstanceofRule_name,
				Messages.RemoveNullCheckBeforeInstanceofRule_description, Duration.ofMinutes(5),
				Arrays.asList(Tag.JAVA_1_1, Tag.READABILITY, Tag.CODING_CONVENTIONS, Tag.FREE));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_1;
	}

}
