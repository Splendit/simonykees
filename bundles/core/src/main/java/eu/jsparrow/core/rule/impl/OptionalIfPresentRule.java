package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.visitor.optional.OptionalIfPresentASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

public class OptionalIfPresentRule extends RefactoringRuleImpl<OptionalIfPresentASTVisitor> {

	public OptionalIfPresentRule() {
		super();
		this.visitorClass = OptionalIfPresentASTVisitor.class;
		this.id = "OptionalIfPresent"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.OptionalIfPresentRule_name,
				Messages.OptionalIfPresentRule_description, Duration.ofMinutes(2),
				Arrays.asList(Tag.JAVA_1_8));
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_8;
	}

}
