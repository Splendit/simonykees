package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.impl.RemoveDoubleNegationASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see RemoveDoubleNegationASTVisitor
 * 
 * @since 2.7
 */
public class RemoveDoubleNegationRule extends RefactoringRuleImpl<RemoveDoubleNegationASTVisitor> {

	public static final String REMOVE_DOUBLE_NEGATION_RULE_ID = "RemoveDoubleNegation"; //$NON-NLS-1$

	public RemoveDoubleNegationRule() {
		this.visitorClass = RemoveDoubleNegationASTVisitor.class;
		this.id = REMOVE_DOUBLE_NEGATION_RULE_ID;
		this.ruleDescription = new RuleDescription(Messages.RemoveDoubleNegationRule_name,
				Messages.RemoveDoubleNegationRule_description, Duration.ofMinutes(2),
				Arrays.asList(Tag.JAVA_1_1, Tag.CODING_CONVENTIONS, Tag.FREE));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_1;
	}
}