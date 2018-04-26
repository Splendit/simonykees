package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.visitor.arithmetic.ArithmethicAssignmentASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see ArithmethicAssignmentASTVisitor
 * 
 * @author Martin Huter
 * @since 0.9
 *
 */
public class ArithmethicAssignmentRule extends RefactoringRuleImpl<ArithmethicAssignmentASTVisitor> {

	public ArithmethicAssignmentRule() {
		super();
		this.visitorClass = ArithmethicAssignmentASTVisitor.class;
		this.id = "ArithmethicAssignment"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.ArithmethicAssignmentRule_name,
				Messages.ArithmethicAssignmentRule_description, Duration.ofMinutes(2),
				Arrays.asList(Tag.JAVA_1_4, Tag.READABILITY, Tag.CODING_CONVENTIONS));
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_4;
	}

}
