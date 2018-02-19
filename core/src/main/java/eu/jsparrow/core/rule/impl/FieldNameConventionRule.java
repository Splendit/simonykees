package eu.jsparrow.core.rule.impl;

import java.time.Duration;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.util.TagUtil;
import eu.jsparrow.core.visitor.renaming.FieldNameConventionASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.RuleDescription;

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
		this.id = "FieldNameConvention"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.FieldNameConventionRule_name,
				Messages.FieldNameConventionRule_description, Duration.ofMinutes(2),
				TagUtil.getTagsForRule(this.getClass()));
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_1;
	}

}
