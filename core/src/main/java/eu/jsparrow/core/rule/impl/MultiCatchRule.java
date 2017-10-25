package eu.jsparrow.core.rule.impl;

import java.time.Duration;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.rule.RuleDescription;
import eu.jsparrow.core.util.TagUtil;
import eu.jsparrow.core.visitor.trycatch.MultiCatchASTVisitor;
import eu.jsparrow.i18n.Messages;

/**
 * @see MultiCatchASTVisitor
 * 
 * @author Martin Huter
 * @since 0.9
 *
 */
public class MultiCatchRule extends RefactoringRule<MultiCatchASTVisitor> {

	public MultiCatchRule() {
		super();
		this.visitorClass = MultiCatchASTVisitor.class;
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_7;
	}
	
	@Override
	public RuleDescription getRuleDescription() {
		return new RuleDescription(Messages.MultiCatchRule_name, Messages.MultiCatchRule_description,
				Duration.ofMinutes(5), TagUtil.getTagsForRule(this.getClass()));
	}
}
