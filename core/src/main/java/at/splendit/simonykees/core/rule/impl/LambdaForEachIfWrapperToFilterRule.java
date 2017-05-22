package at.splendit.simonykees.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;

import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.visitor.LambdaForEachIfWrapperToFilterASTVisitor;

public class LambdaForEachIfWrapperToFilterRule extends RefactoringRule<LambdaForEachIfWrapperToFilterASTVisitor> {

	public LambdaForEachIfWrapperToFilterRule(Class<LambdaForEachIfWrapperToFilterASTVisitor> visitor) {
		super(visitor);
		this.name = "LambdaForEachIfWrapperToFilterRule";
		this.description = "LambdaForEachIfWrapperToFilterRule";
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_8;
	}

}
