package at.splendit.simonykees.core.rule;

import at.splendit.simonykees.core.visitor.tryWithResource.MultiCatchASTVisitor;

public class MultiCatchRule extends RefactoringRule<MultiCatchASTVisitor> {

	public MultiCatchRule(Class<MultiCatchASTVisitor> visitor) {
		super(visitor);
		this.name = "MultiCatch";
	}

}
