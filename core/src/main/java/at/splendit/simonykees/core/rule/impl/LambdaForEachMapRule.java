package at.splendit.simonykees.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;

import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.visitor.LambdaForEachMapASTVisitor;

public class LambdaForEachMapRule extends RefactoringRule<LambdaForEachMapASTVisitor> {

	public LambdaForEachMapRule(Class<LambdaForEachMapASTVisitor> visitor) {
		super(visitor);
		this.name = "Lambda forEach unwrap map";
		this.description = "Unwraps a map from the body of the forEach";
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_8;
	}

}
