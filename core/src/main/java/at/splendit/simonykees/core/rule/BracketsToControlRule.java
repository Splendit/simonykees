package at.splendit.simonykees.core.rule;

import at.splendit.simonykees.core.i18n.Messages;
import at.splendit.simonykees.core.visitor.BracketsToControlASTVisitor;

public class BracketsToControlRule extends RefactoringRule<BracketsToControlASTVisitor> {

	public BracketsToControlRule(Class<BracketsToControlASTVisitor> visitor) {
		super(visitor);
		this.name = Messages.BracketsToControlRule_name;
		this.description = Messages.BracketsToControlRule_description;
	}

}
