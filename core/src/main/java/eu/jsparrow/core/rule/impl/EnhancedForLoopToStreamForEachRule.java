package eu.jsparrow.core.rule.impl;

import java.time.Duration;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.rule.RuleDescription;
import eu.jsparrow.core.util.TagUtil;
import eu.jsparrow.core.visitor.loop.stream.EnhancedForLoopToStreamForEachASTVisitor;
import eu.jsparrow.i18n.Messages;

/**
 * @see EnhancedForLoopToStreamForEachASTVisitor
 * 
 * @author Matthias Webhofer
 * @since 1.2
 */
public class EnhancedForLoopToStreamForEachRule extends RefactoringRule<EnhancedForLoopToStreamForEachASTVisitor> {

	public EnhancedForLoopToStreamForEachRule() {
		super();
		this.visitorClass = EnhancedForLoopToStreamForEachASTVisitor.class;
		this.name = Messages.EnhancedForLoopToStreamForEachRule_name;
		this.description = Messages.EnhancedForLoopToStreamForEachRule_description;
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_8;
	}
	
	
	@Override
	public RuleDescription getRuleDescription() {
		return new RuleDescription(Messages.EnhancedForLoopToStreamForEachRule_name, Messages.EnhancedForLoopToStreamForEachRule_description,
				Duration.ofMinutes(15), TagUtil.getTagsForRule(this.getClass()));
	}

}
