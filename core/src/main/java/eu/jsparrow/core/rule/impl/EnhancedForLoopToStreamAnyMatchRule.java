package eu.jsparrow.core.rule.impl;

import java.time.Duration;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.rule.RuleDescription;
import eu.jsparrow.core.util.TagUtil;
import eu.jsparrow.core.visitor.loop.stream.EnhancedForLoopToStreamAnyMatchASTVisitor;
import eu.jsparrow.i18n.Messages;

/**
 * @see EnhancedForLoopToStreamAnyMatchASTVisitor
 * 
 * @author Ardit Ymeri
 * @since 2.1.1
 *
 */
public class EnhancedForLoopToStreamAnyMatchRule extends RefactoringRule<EnhancedForLoopToStreamAnyMatchASTVisitor> {

	public EnhancedForLoopToStreamAnyMatchRule() {
		super();
		this.visitorClass = EnhancedForLoopToStreamAnyMatchASTVisitor.class;
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_8;
	}
	
	@Override
	public RuleDescription getRuleDescription() {
		return new RuleDescription(Messages.EnhancedForLoopToStreamAnyMatchRule_name, Messages.EnhancedForLoopToStreamAnyMatchRule_description,
				Duration.ofMinutes(2), TagUtil.getTagsForRule(this.getClass()));
	}

}
