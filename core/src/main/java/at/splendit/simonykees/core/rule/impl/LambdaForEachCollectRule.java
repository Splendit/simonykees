package at.splendit.simonykees.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;

import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.visitor.LambdaForEachCollectASTVisitor;
import at.splendit.simonykees.i18n.Messages;

/**
 * @see LambdaForEachCollectASTVisitor
 * 
 * @author Ardit Ymeri
 * @since 1.2
 *
 */
public class LambdaForEachCollectRule extends RefactoringRule<LambdaForEachCollectASTVisitor> {

	public LambdaForEachCollectRule(Class<LambdaForEachCollectASTVisitor> visitor) {
		super(visitor);
		this.name = Messages.LambdaForEachCollectRule_name;
		this.description = Messages.LambdaForEachCollectRule_description;
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_8;
	}

}
