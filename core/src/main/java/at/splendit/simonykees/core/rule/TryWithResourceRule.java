package at.splendit.simonykees.core.rule;

import at.splendit.simonykees.core.visitor.tryWithResource.TryWithResourceASTVisitor;

public class TryWithResourceRule extends RefactoringRule<TryWithResourceASTVisitor> {

	public TryWithResourceRule(Class<TryWithResourceASTVisitor> visitor) {
		super(visitor);
		this.name = "TryWithResource";
	}

}
