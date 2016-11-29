package at.splendit.simonykees.core.rule;

import at.splendit.simonykees.core.i18n.Messages;
import at.splendit.simonykees.core.visitor.StringFormatLineSeperatorASTVisitor;

/**
 * @see StringFormatLineSeperatorRule
 * 
 * @author Martin Huter
 * @since 0.9.2
 *
 */
public class StringFormatLineSeperatorRule extends RefactoringRule<StringFormatLineSeperatorASTVisitor> {

	public StringFormatLineSeperatorRule(Class<StringFormatLineSeperatorASTVisitor> visitor) {
		super(visitor);
		this.name = Messages.StringFormatLineSeperatorRule_name;
		this.description = Messages.StringFormatLineSeperatorRule_description;
	}

}
