package at.splendit.simonykees.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;

import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.visitor.StatementLambdaToExpressionASTVisitor;
import at.splendit.simonykees.i18n.Messages;

/**
 * @see StatementLambdaToExpressionASTVisitor
 * 
 * @author Matthias Webhofer
 * @since 1.2
 *
 */
public class StatementLambdaToExpressionRule extends RefactoringRule<StatementLambdaToExpressionASTVisitor> {

	public StatementLambdaToExpressionRule(Class<StatementLambdaToExpressionASTVisitor> visitor) {
		super(visitor);
		// TODO i18n
		this.name = Messages.StatementLambdaToExpressionRule_name;
		this.description = Messages.StatementLambdaToExpressionRule_description;
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_8;
	}

}
