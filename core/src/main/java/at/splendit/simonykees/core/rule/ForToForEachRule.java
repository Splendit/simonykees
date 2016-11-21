package at.splendit.simonykees.core.rule;

import at.splendit.simonykees.core.visitor.loop.ForToForEachASTVisitor;

/**
 * @see ForToForEachRule
 * 
 * @author Martin Huter
 * @since 0.9.2
 *
 */
public class ForToForEachRule extends RefactoringRule<ForToForEachASTVisitor> {

	public ForToForEachRule(Class<ForToForEachASTVisitor> visitor) {
		super(visitor);
		this.name = "TODO NAME";
		this.description = "TODO DESCRIPTION";
	}

}
