package eu.jsparrow.core.rule.impl;

import java.time.Duration;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.impl.ReorderModifiersASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see ReorderModifiersASTVisitor
 * 
 * @since 3.6.0
 *
 */
public class ReorderModifiersRule extends RefactoringRuleImpl<ReorderModifiersASTVisitor> {

	public static final String RULE_ID = "ReorderModifiers"; //$NON-NLS-1$

	public ReorderModifiersRule() {
		this.visitorClass = ReorderModifiersASTVisitor.class;
		this.id = RULE_ID;
		this.ruleDescription = new RuleDescription(Messages.ReorderModifiersRule_name,
				Messages.ReorderModifiersRule_description, Duration.ofMinutes(2), Tag.READABILITY,
				Tag.CODING_CONVENTIONS);
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_1;
	}

}
