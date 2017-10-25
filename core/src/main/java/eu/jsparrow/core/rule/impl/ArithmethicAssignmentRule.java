package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.rule.RuleDescription;
import eu.jsparrow.core.util.TagUtil;
import eu.jsparrow.core.visitor.arithmetic.ArithmethicAssignmentASTVisitor;
import eu.jsparrow.i18n.Messages;

/**
 * @see ArithmethicAssignmentASTVisitor
 * 
 * @author Martin Huter
 * @since 0.9
 *
 */
public class ArithmethicAssignmentRule extends RefactoringRule<ArithmethicAssignmentASTVisitor> {

	public ArithmethicAssignmentRule() {
		super();
		this.visitorClass = ArithmethicAssignmentASTVisitor.class;
		this.name = Messages.ArithmethicAssignmentRule_name;
		this.description = Messages.ArithmethicAssignmentRule_description;
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_4;
	}

	@Override
	public RuleDescription getRuleDescription() {
		return new RuleDescription(Messages.ArithmethicAssignmentRule_name,
				Messages.ArithmethicAssignmentRule_description, Duration.ofMinutes(2),
				TagUtil.getTagsForRule(this.getClass()));
	}
}
