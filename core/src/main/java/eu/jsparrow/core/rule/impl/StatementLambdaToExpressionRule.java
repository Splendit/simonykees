package eu.jsparrow.core.rule.impl;

import java.time.Duration;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.util.TagUtil;
import eu.jsparrow.core.visitor.impl.StatementLambdaToExpressionASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.RuleDescription;

/**
 * @see StatementLambdaToExpressionASTVisitor
 * 
 * @author Matthias Webhofer
 * @since 1.2
 *
 */
public class StatementLambdaToExpressionRule extends RefactoringRule<StatementLambdaToExpressionASTVisitor> {

	public StatementLambdaToExpressionRule() {
		super();
		this.visitorClass = StatementLambdaToExpressionASTVisitor.class;
		this.id = "StatementLambdaToExpression"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.StatementLambdaToExpressionRule_name,
				Messages.StatementLambdaToExpressionRule_description, Duration.ofMinutes(5),
				TagUtil.getTagsForRule(this.getClass()));
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_8;
	}

}
