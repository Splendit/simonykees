package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.optional.OptionalIfPresentASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see OptionalIfPresentASTVisitor 
 * 
 * @since 2.6
 */
public class OptionalIfPresentRule extends RefactoringRuleImpl<OptionalIfPresentASTVisitor> {

	public static final String RULE_ID = "OptionalIfPresent"; //$NON-NLS-1$
	public OptionalIfPresentRule() {
		this.visitorClass = OptionalIfPresentASTVisitor.class;
		this.id = RULE_ID;
		this.ruleDescription = new RuleDescription(Messages.OptionalIfPresentRule_name,
				Messages.OptionalIfPresentRule_description, Duration.ofMinutes(2),
				Arrays.asList(Tag.JAVA_1_8, Tag.OLD_LANGUAGE_CONSTRUCTS, Tag.LAMBDA));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_8;
	}

}
