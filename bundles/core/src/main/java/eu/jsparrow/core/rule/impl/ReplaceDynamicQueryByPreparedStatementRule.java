package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.security.ReplaceDynamicQueryByPreparedStatementASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see ReplaceDynamicQueryByPreparedStatementASTVisitor
 * 
 * @since 3.16.0
 *
 */
public class ReplaceDynamicQueryByPreparedStatementRule
		extends RefactoringRuleImpl<ReplaceDynamicQueryByPreparedStatementASTVisitor> {

	public ReplaceDynamicQueryByPreparedStatementRule() {
		this.visitorClass = ReplaceDynamicQueryByPreparedStatementASTVisitor.class;
		this.id = "ReplaceDynamicQueryByPreparedStatement"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.ReplaceDynamicQueryByPreparedStatementRule_name,
				Messages.ReplaceDynamicQueryByPreparedStatementRule_description, Duration.ofMinutes(10),
				Arrays.asList(Tag.JAVA_1_1, Tag.SECURITY));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_1;
	}
}
