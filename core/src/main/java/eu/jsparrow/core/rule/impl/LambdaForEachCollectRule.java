package eu.jsparrow.core.rule.impl;

import java.time.Duration;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.util.TagUtil;
import eu.jsparrow.core.visitor.lambdaforeach.LambdaForEachCollectASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.RuleDescription;

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
		this.id = "LambdaForEachCollect"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.LambdaForEachCollectRule_name,
				Messages.LambdaForEachCollectRule_description, Duration.ofMinutes(15),
				TagUtil.getTagsForRule(this.getClass()));
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_8;
	}

}
