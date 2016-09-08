package at.splendit.simonykees.core.rule;

import at.splendit.simonykees.core.visitor.StringUtilsASTVisitor;

public class StringUtilsRule extends RefactoringRule<StringUtilsASTVisitor> {

	public StringUtilsRule(Class<StringUtilsASTVisitor> visitor) {
		super(visitor);
		this.name = "StringUtils";
	}

}
