package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.impl.AvoidConcatenationInLoggingStatementsASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see AvoidConcatenationInLoggingStatementsASTVisitor
 * @since 3.18.0
 */
public class AvoidConcatenationInLoggingStatementsRule
		extends RefactoringRuleImpl<AvoidConcatenationInLoggingStatementsASTVisitor> {

	public AvoidConcatenationInLoggingStatementsRule() {
		this.visitorClass = AvoidConcatenationInLoggingStatementsASTVisitor.class;
		this.id = "AvoidConcatenationInLoggingStatements"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.AvoidConcatenationInLoggingStatementsRule_name,
				Messages.AvoidConcatenationInLoggingStatementsRule_description, Duration.ofMinutes(5),
				Arrays.asList(Tag.JAVA_1_1, Tag.PERFORMANCE, Tag.CODING_CONVENTIONS, Tag.READABILITY, Tag.LOGGING));

	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_1;
	}

}
