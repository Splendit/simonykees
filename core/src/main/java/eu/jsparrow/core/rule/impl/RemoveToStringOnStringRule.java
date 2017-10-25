package eu.jsparrow.core.rule.impl;

import java.time.Duration;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.rule.RuleDescription;
import eu.jsparrow.core.util.TagUtil;
import eu.jsparrow.core.visitor.impl.RemoveToStringOnStringASTVisitor;
import eu.jsparrow.i18n.Messages;

/**
 * @see RemoveToStringOnStringASTVisitor
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
public class RemoveToStringOnStringRule extends RefactoringRule<RemoveToStringOnStringASTVisitor> {

	public RemoveToStringOnStringRule() {
		super();
		this.visitorClass = RemoveToStringOnStringASTVisitor.class;
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_1;
	}
	
	@Override
	public RuleDescription getRuleDescription() {
		return new RuleDescription(Messages.RemoveToStringOnStringRule_name, Messages.RemoveToStringOnStringRule_description,
				Duration.ofMinutes(2), TagUtil.getTagsForRule(this.getClass()));
	}
}
