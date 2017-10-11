package eu.jsparrow.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.visitor.impl.StatementLambdaToExpressionASTVisitor;
import eu.jsparrow.i18n.Messages;

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
		this.visitor = StatementLambdaToExpressionASTVisitor.class;
		this.name = Messages.StatementLambdaToExpressionRule_name;
		this.description = Messages.StatementLambdaToExpressionRule_description;
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_8;
	}

}
