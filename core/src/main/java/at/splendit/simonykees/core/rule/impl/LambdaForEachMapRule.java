package at.splendit.simonykees.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;

import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.visitor.lambdaForEach.LambdaForEachMapASTVisitor;
import at.splendit.simonykees.i18n.Messages;

/**
 * @see LambdaForEachMapASTVisitor
 * 
 * @author Ardit Ymeri
 * @since 1.2
 *
 */
public class LambdaForEachMapRule extends RefactoringRule<LambdaForEachMapASTVisitor> {

	public LambdaForEachMapRule(Class<LambdaForEachMapASTVisitor> visitor) {
		super(visitor);
		this.name = Messages.LambdaForEachMapRule_name;
		this.description = Messages.LambdaForEachMapRule_description;
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_8;
	}

}
