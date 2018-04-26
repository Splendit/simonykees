package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.visitor.renaming.FieldNameConventionASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see FieldNameConventionASTVisitor
 * 
 * @author Ardit Ymeri
 * @since 1.2
 *
 */
public class FieldNameConventionRule extends RefactoringRuleImpl<FieldNameConventionASTVisitor> {

	public FieldNameConventionRule() {
		super();
		this.visitorClass = FieldNameConventionASTVisitor.class;
		this.id = "FieldNameConvention"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.FieldNameConventionRule_name,
				Messages.FieldNameConventionRule_description, Duration.ofMinutes(2),
				Arrays.asList(Tag.JAVA_0_9, Tag.READABILITY, Tag.CODING_CONVENTIONS));
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_1;
	}

}
