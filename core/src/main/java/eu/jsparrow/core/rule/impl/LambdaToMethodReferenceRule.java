package eu.jsparrow.core.rule.impl;

import java.time.Duration;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.rule.RuleDescription;
import eu.jsparrow.core.util.TagUtil;
import eu.jsparrow.core.visitor.impl.LambdaToMethodReferenceASTVisitor;
import eu.jsparrow.i18n.Messages;

/**
 * @see LambdaToMethodReferenceASTVisitor
 * 
 * @author Matthias Webhofer
 * @since 1.2
 *
 */
public class LambdaToMethodReferenceRule extends RefactoringRule<LambdaToMethodReferenceASTVisitor> {
	public LambdaToMethodReferenceRule() {
		super();
		this.visitorClass = LambdaToMethodReferenceASTVisitor.class;
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_8;
	}
	
	@Override
	public RuleDescription getRuleDescription() {
		return new RuleDescription(Messages.LambdaToMethodReferenceRule_name, Messages.LambdaToMethodReferenceRule_description,
				Duration.ofMinutes(2), TagUtil.getTagsForRule(this.getClass()));
	}
}
