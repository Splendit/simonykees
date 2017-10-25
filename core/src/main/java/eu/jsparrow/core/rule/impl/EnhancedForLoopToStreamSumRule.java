package eu.jsparrow.core.rule.impl;

import java.time.Duration;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.rule.RuleDescription;
import eu.jsparrow.core.util.TagUtil;
import eu.jsparrow.core.visitor.loop.stream.EnhancedForLoopToStreamSumASTVisitor;
import eu.jsparrow.i18n.Messages;

/**
 * @see EnhancedForLoopToStreamSumASTVisitor
 * 
 * @author Ardit Ymeri
 * @since 2.1.1
 */
public class EnhancedForLoopToStreamSumRule extends RefactoringRule<EnhancedForLoopToStreamSumASTVisitor> {

	public EnhancedForLoopToStreamSumRule() {
		super();
		this.visitorClass = EnhancedForLoopToStreamSumASTVisitor.class;
		this.name = Messages.EnhancedForLoopToStreamSumRule_name;
		this.description = Messages.EnhancedForLoopToStreamSumRule_description;
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_8;
	}
	
	@Override
	public RuleDescription getRuleDescription() {
		return new RuleDescription(Messages.EnhancedForLoopToStreamSumRule_name, Messages.EnhancedForLoopToStreamSumRule_name,
				Duration.ofMinutes(10), TagUtil.getTagsForRule(this.getClass()));
	}


}
