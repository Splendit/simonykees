package eu.jsparrow.core.rule.impl;

import java.time.Duration;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.rule.RuleDescription;
import eu.jsparrow.core.util.TagUtil;
import eu.jsparrow.core.visitor.lambdaforeach.LambdaForEachIfWrapperToFilterASTVisitor;
import eu.jsparrow.i18n.Messages;

/**
 * @see LambdaForEachIfWrapperToFilterASTVisitor
 * 
 * @author Matthias Webhofer
 * @since 1.2
 */
public class LambdaForEachIfWrapperToFilterRule extends RefactoringRule<LambdaForEachIfWrapperToFilterASTVisitor> {

	public LambdaForEachIfWrapperToFilterRule() {
		super();
		this.visitorClass = LambdaForEachIfWrapperToFilterASTVisitor.class;
		this.name = Messages.LambdaForEachIfWrapperToFilterRule_name;
		this.description = Messages.LambdaForEachIfWrapperToFilterRule_description;
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_8;
	}
	
	@Override
	public RuleDescription getRuleDescription() {
		return new RuleDescription(Messages.LambdaForEachIfWrapperToFilterRule_name, Messages.LambdaForEachIfWrapperToFilterRule_description,
				Duration.ofMinutes(5), TagUtil.getTagsForRule(this.getClass()));
	}

}
