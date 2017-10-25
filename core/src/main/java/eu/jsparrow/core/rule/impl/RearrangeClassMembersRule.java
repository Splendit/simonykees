package eu.jsparrow.core.rule.impl;

import java.time.Duration;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.rule.RuleDescription;
import eu.jsparrow.core.util.TagUtil;
import eu.jsparrow.core.visitor.impl.RearrangeClassMembersASTVisitor;
import eu.jsparrow.i18n.Messages;

/**
 * @see RearrangeClassMembersASTVisitor
 * 
 * @author Ardit Ymeri
 * @since 1.1
 */
public class RearrangeClassMembersRule extends RefactoringRule<RearrangeClassMembersASTVisitor> {

	public RearrangeClassMembersRule() {
		super();
		this.visitorClass = RearrangeClassMembersASTVisitor.class;
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_1;
	}
	
	@Override
	public RuleDescription getRuleDescription() {
		return new RuleDescription(Messages.RearrangeClassMembersRule_name, Messages.RearrangeClassMembersRule_description,
				Duration.ofMinutes(5), TagUtil.getTagsForRule(this.getClass()));
	}
}
