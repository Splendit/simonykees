package at.splendit.simonykees.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;

import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.visitor.LambdaToMethodReferenceASTVisitor;

/**
 * @see LambdaToMethodReferenceASTVisitor
 * 
 * @author Matthias Webhofer
 * @since 1.2
 *
 */
public class LambdaToMethodReferenceRule extends RefactoringRule<LambdaToMethodReferenceASTVisitor> {
	public LambdaToMethodReferenceRule(Class<LambdaToMethodReferenceASTVisitor> visitor) {
		super(visitor);
		this.name = "LambdaToMethodReferenceRule";
		this.description = "LambdaToMethodReferenceRule";
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_8;
	}
}
