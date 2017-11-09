package eu.jsparrow.core.rule.impl;

import java.time.Duration;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.rule.RuleDescription;
import eu.jsparrow.core.util.TagUtil;
import eu.jsparrow.core.visitor.impl.EnumsWithoutEqualsASTVisitor;
import eu.jsparrow.i18n.Messages;

/**
 * @see EnumsWithoutEqualsRuleASTVisitor
 * 
 * @author Hans-Jörg Schrödl
 * @since 2.1.1
 */
public class EnumsWithoutEqualsRule extends RefactoringRule<EnumsWithoutEqualsASTVisitor> {

	public EnumsWithoutEqualsRule() {
		super();
		this.visitorClass = EnumsWithoutEqualsASTVisitor.class;
		this.id = "EnumsWithoutEquals"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.EnumsWithoutEqualsRule_name,
				Messages.EnumsWithoutEqualsRule_description, Duration.ofMinutes(2),
				TagUtil.getTagsForRule(this.getClass()));
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		// Enums exist since 1.5
		return JavaVersion.JAVA_1_5;
	}

}
