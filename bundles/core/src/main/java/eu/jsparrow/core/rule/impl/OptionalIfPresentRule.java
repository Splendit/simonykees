package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.apache.commons.lang3.JavaVersion;

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

	public OptionalIfPresentRule() {
		this.visitorClass = OptionalIfPresentASTVisitor.class;
		this.id = "OptionalIfPresent"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.OptionalIfPresentRule_name,
				Messages.OptionalIfPresentRule_description, Duration.ofMinutes(2),
				Arrays.asList(Tag.JAVA_1_8, Tag.OLD_LANGUAGE_CONSTRUCTS, Tag.LAMBDA));
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_8;
	}

}
