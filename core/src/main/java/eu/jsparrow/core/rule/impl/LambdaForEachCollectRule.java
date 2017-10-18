package eu.jsparrow.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.visitor.lambdaforeach.LambdaForEachCollectASTVisitor;
import eu.jsparrow.i18n.Messages;

/**
 * @see LambdaForEachCollectASTVisitor
 * 
 * @author Ardit Ymeri
 * @since 1.2
 *
 */
public class LambdaForEachCollectRule extends RefactoringRule<LambdaForEachCollectASTVisitor> {

	public LambdaForEachCollectRule() {
		super();
		this.visitorClass = LambdaForEachCollectASTVisitor.class;
		this.name = Messages.LambdaForEachCollectRule_name;
		this.description = Messages.LambdaForEachCollectRule_description;
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_8;
	}

}
