package eu.jsparrow.core.rule.impl;

import java.time.Duration;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.rule.RuleDescription;
import eu.jsparrow.core.util.TagUtil;
import eu.jsparrow.core.visitor.loop.fortoforeach.ForToForEachASTVisitor;
import eu.jsparrow.i18n.Messages;

/**
 * @see ForToForEachASTVisitor
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
public class ForToForEachRule extends RefactoringRule<ForToForEachASTVisitor> {

	public ForToForEachRule() {
		super();
		this.visitorClass = ForToForEachASTVisitor.class;
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_5;
	}
	
	@Override
	public RuleDescription getRuleDescription() {
		return new RuleDescription(Messages.ForToForEachRule_name, Messages.ForToForEachRule_description,
				Duration.ofMinutes(5), TagUtil.getTagsForRule(this.getClass()));
	}
}
