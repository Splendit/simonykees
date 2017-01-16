package at.splendit.simonykees.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;

import at.splendit.simonykees.core.i18n.Messages;
import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.visitor.StringConcatToPlusASTVisitor;

/**
 * @see StringConcatToPlusASTVisitor
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
public class StringConcatToPlusRule extends RefactoringRule<StringConcatToPlusASTVisitor> {

	public StringConcatToPlusRule(Class<StringConcatToPlusASTVisitor> visitor) {
		super(visitor);
		this.name = Messages.StringConcatToPlusRule_name;
		this.description = Messages.StringConcatToPlusRule_description;
		this.requiredJavaVersion = JavaVersion.JAVA_1_1;
	}

}
