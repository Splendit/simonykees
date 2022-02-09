package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

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

	public static final String RULE_ID = "ArithmethicAssignment"; //$NON-NLS-1$

	public ArithmethicAssignmentRule() {
		super();
		this.visitorClass = ArithmethicAssignmentASTVisitor.class;
		this.id = RULE_ID;
		this.ruleDescription = new RuleDescription(Messages.ArithmethicAssignmentRule_name,
				Messages.ArithmethicAssignmentRule_description, Duration.ofMinutes(2),
				Arrays.asList(Tag.JAVA_1_4, Tag.READABILITY, Tag.CODING_CONVENTIONS));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_4;
	}

}
