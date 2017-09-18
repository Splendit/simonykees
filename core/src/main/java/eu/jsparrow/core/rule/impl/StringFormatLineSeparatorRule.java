package eu.jsparrow.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.visitor.StringFormatLineSeparatorASTVisitor;
import eu.jsparrow.i18n.Messages;

/**
 * @see StringFormatLineSeparatorRule
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
public class StringFormatLineSeparatorRule extends RefactoringRule<StringFormatLineSeparatorASTVisitor> {

	public StringFormatLineSeparatorRule(Class<StringFormatLineSeparatorASTVisitor> visitor) {
		super(visitor);
		this.name = Messages.StringFormatLineSeparatorRule_name;
		this.description = Messages.StringFormatLineSeparatorRule_description;
	}
	
	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_5;
	}
}
