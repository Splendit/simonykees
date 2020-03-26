package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.security.ReplaceDynamicQueryByPreparedStatementASTVisitor;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

public class ReplaceDynamicQueryByPreparedStatementRule extends RefactoringRuleImpl<ReplaceDynamicQueryByPreparedStatementASTVisitor> {
	

	public ReplaceDynamicQueryByPreparedStatementRule() {
		this.visitorClass = ReplaceDynamicQueryByPreparedStatementASTVisitor.class;
		this.id = "ReplaceDynamicQueryByPreparedStatement"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription("Replace Dynamic Query by PreparedStatement", "Replace Dynamic Query by PreparedStatement. Avoid SQL injections. ",
				Duration.ofMinutes(5), Arrays.asList(Tag.JAVA_1_1, Tag.SECURITY));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_1;
	}
}
