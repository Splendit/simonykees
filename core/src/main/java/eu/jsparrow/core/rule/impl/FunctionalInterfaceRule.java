package eu.jsparrow.core.rule.impl;

import java.time.Duration;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.rule.RuleDescription;
import eu.jsparrow.core.util.TagUtil;
import eu.jsparrow.core.visitor.functionalinterface.FunctionalInterfaceASTVisitor;
import eu.jsparrow.i18n.Messages;

/**
 * @see FunctionalInterfaceASTVisitor
 * 
 * @author Martin Huter
 * @since 0.9
 *
 */
public class FunctionalInterfaceRule extends RefactoringRule<FunctionalInterfaceASTVisitor> {

	public FunctionalInterfaceRule() {
		super();
		this.visitorClass = FunctionalInterfaceASTVisitor.class;
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_8;
	}
	
	@Override
	public RuleDescription getRuleDescription() {
		return new RuleDescription(Messages.FunctionalInterfaceRule_name, Messages.FunctionalInterfaceRule_description,
				Duration.ofMinutes(5), TagUtil.getTagsForRule(this.getClass()));
	}
}
