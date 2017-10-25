package eu.jsparrow.core.rule.impl;

import java.time.Duration;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.rule.RuleDescription;
import eu.jsparrow.core.util.TagUtil;
import eu.jsparrow.core.visitor.renaming.FieldNameConventionASTVisitor;
import eu.jsparrow.i18n.Messages;

/**
 * @see FieldNameConventionASTVisitor
 * 
 * @author Ardit Ymeri
 * @since 1.2
 *
 */
public class FieldNameConventionRule extends RefactoringRule<FieldNameConventionASTVisitor> {

	public FieldNameConventionRule() {
		super();
		this.visitorClass = FieldNameConventionASTVisitor.class;
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_1;
	}
	
	@Override
	public RuleDescription getRuleDescription() {
		return new RuleDescription(Messages.FieldNameConventionRule_name, Messages.FieldNameConventionRule_description,
				Duration.ofMinutes(2), TagUtil.getTagsForRule(this.getClass()));
	}
}
