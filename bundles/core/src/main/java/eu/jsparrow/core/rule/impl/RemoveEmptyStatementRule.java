package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.impl.RemoveEmptyStatementASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see RemoveEmptyStatementASTVisitor
 * 
 * @since 2.7.0
 *
 */
public class RemoveEmptyStatementRule extends RefactoringRuleImpl<RemoveEmptyStatementASTVisitor> {

	public static final String REMOVE_EMPTY_STATEMENT_RULE_ID = "RemoveEmptyStatement"; //$NON-NLS-1$

	public RemoveEmptyStatementRule() {
		this.visitorClass = RemoveEmptyStatementASTVisitor.class;
		this.id = REMOVE_EMPTY_STATEMENT_RULE_ID;
		this.ruleDescription = new RuleDescription(Messages.RemoveEmptyStatementRule_name,
				Messages.RemoveEmptyStatementRule_description, Duration.ofMinutes(1),
				Arrays.asList(Tag.JAVA_1_1, Tag.READABILITY, Tag.FREE));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_1;
	}
}
