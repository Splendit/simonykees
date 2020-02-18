package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.optional.OptionalFilterASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see OptionalFilterASTVisitor
 * 
 * @since 3.14.0
 *
 */
public class OptionalFilterRule extends RefactoringRuleImpl<OptionalFilterASTVisitor> {

	public OptionalFilterRule() {
		this.visitorClass = OptionalFilterASTVisitor.class;
		this.id = "OptionalFilter"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.OptionalFilterRule_name,
				Messages.OptionalFilterRule_description, Duration.ofMinutes(2),
				Arrays.asList(Tag.JAVA_1_8, Tag.CODING_CONVENTIONS, Tag.LAMBDA, Tag.READABILITY));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_8;
	}
}
