package eu.jsparrow.core.rule.impl;

import java.time.Duration;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.rule.RuleDescription;
import eu.jsparrow.core.util.TagUtil;
import eu.jsparrow.core.visitor.impl.BracketsToControlASTVisitor;
import eu.jsparrow.i18n.Messages;

/**
 * @see BracketsToControlASTVisitor
 * 
 * @author Martin Huter
 * @since 0.9
 *
 */
public class BracketsToControlRule extends RefactoringRule<BracketsToControlASTVisitor> {

	public BracketsToControlRule() {
		super();
		this.visitorClass = BracketsToControlASTVisitor.class;
		this.name = Messages.BracketsToControlRule_name;
		this.description = Messages.BracketsToControlRule_description;
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_1;
	}

	public RuleDescription getRuleDescription() {
		return new RuleDescription(Messages.BracketsToControlRule_name, Messages.BracketsToControlRule_description,
				Duration.ofMinutes(2), TagUtil.getTagsForRule(this.getClass()));
	}
}
