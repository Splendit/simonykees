package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.impl.AvoidEvaluationOfParametersInLoggingMessagesASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see AvoidEvaluationOfParametersInLoggingMessagesASTVisitor
 * @since 3.18.0
 */
public class AvoidEvaluationOfParametersInLoggingMessagesRule
		extends RefactoringRuleImpl<AvoidEvaluationOfParametersInLoggingMessagesASTVisitor> {

	public AvoidEvaluationOfParametersInLoggingMessagesRule() {
		this.visitorClass = AvoidEvaluationOfParametersInLoggingMessagesASTVisitor.class;
		this.id = "AvoidEvaluationOfParametersInLoggingMessages"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.AvoidEvaluationOfParametersInLoggingMessagesRule_name,
				Messages.AvoidEvaluationOfParametersInLoggingMessagesRule_description, Duration.ofMinutes(5),
				Arrays.asList(Tag.JAVA_1_1, Tag.PERFORMANCE, Tag.CODING_CONVENTIONS, Tag.READABILITY));

	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_1;
	}

}
