package at.splendit.simonykees.core.rule;

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
		this.name = "TODO name";
		this.description = "TODO description";
	}

}
