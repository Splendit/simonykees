package eu.jsparrow.core.rule.impl;

import java.time.Duration;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.rule.RuleDescription;
import eu.jsparrow.core.util.TagUtil;
import eu.jsparrow.core.visitor.lambdaforeach.LambdaForEachMapASTVisitor;
import eu.jsparrow.i18n.Messages;

/**
 * @see LambdaForEachMapASTVisitor
 * 
 * @author Ardit Ymeri
 * @since 1.2
 *
 */
public class LambdaForEachMapRule extends RefactoringRule<LambdaForEachMapASTVisitor> {

	public LambdaForEachMapRule() {
		super();
		this.visitorClass = LambdaForEachMapASTVisitor.class;
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_8;
	}

	@Override
	public RuleDescription getRuleDescription() {
		return new RuleDescription(Messages.LambdaForEachMapRule_name, Messages.LambdaForEachMapRule_description,
				Duration.ofMinutes(15), TagUtil.getTagsForRule(this.getClass()));
	}
}
