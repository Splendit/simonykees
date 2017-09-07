package eu.jsparrow.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.visitor.lambdaForEach.LambdaForEachMapASTVisitor;
import eu.jsparrow.i18n.Messages;

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
