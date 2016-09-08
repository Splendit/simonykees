package at.splendit.simonykees.core.rule;

import at.splendit.simonykees.core.visitor.arithmetic.ArithmethicAssignmentASTVisitor;

public class ArithmethicAssignmentRule extends RefactoringRule<ArithmethicAssignmentASTVisitor> {

	public ArithmethicAssignmentRule(Class<ArithmethicAssignmentASTVisitor> visitor) {
		super(visitor);
		this.name = "Arithmetic Assignment";
	}

}
