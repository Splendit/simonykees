package eu.jsparrow.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.visitor.StringLiteralEqualityCheckASTVisitor;
import eu.jsparrow.i18n.Messages;

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
