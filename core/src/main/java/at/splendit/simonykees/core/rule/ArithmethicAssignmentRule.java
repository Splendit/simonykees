package at.splendit.simonykees.core.rule;

import at.splendit.simonykees.core.i18n.Messages;
import at.splendit.simonykees.core.visitor.arithmetic.ArithmethicAssignmentASTVisitor;

public class ArithmethicAssignmentRule extends RefactoringRule<ArithmethicAssignmentASTVisitor> {

	public ArithmethicAssignmentRule(Class<ArithmethicAssignmentASTVisitor> visitor) {
		super(visitor);
		this.name = Messages.ArithmethicAssignmentRule_name;
		this.description = Messages.ArithmethicAssignmentRule_description;
	}

}
