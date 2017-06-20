package at.splendit.simonykees.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;

import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.visitor.StringLiteralEqualityCheckASTVisitor;
import at.splendit.simonykees.i18n.Messages;

/**
 * @see StringLiteralEqualityCheckASTVisitor
 * 
 * @author Ardit Ymeri
 * @since 1.2
 *
 */
public class StringLiteralEqualityCheckRule extends RefactoringRule<StringLiteralEqualityCheckASTVisitor> {

	public StringLiteralEqualityCheckRule(Class<StringLiteralEqualityCheckASTVisitor> visitor) {
		super(visitor);
		this.name = Messages.StringLiteralEqualityCheckRule_name;
		this.description = Messages.StringLiteralEqualityCheckRule_description;
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_1;
	}
}
