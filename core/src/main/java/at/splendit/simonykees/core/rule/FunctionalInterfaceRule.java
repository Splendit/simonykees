package at.splendit.simonykees.core.rule;

import at.splendit.simonykees.core.visitor.FunctionalInterfaceASTVisitor;

public class FunctionalInterfaceRule extends RefactoringRule<FunctionalInterfaceASTVisitor> {

	public FunctionalInterfaceRule(Class<FunctionalInterfaceASTVisitor> visitor) {
		super(visitor);
		this.name = "Working Title - FunctionalInterface";
		this.description = "Converts annonym inner classes to equivalent lambda expressions";
	}

}
