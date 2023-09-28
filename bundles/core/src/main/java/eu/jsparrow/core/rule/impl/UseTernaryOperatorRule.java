package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.impl.ternary.UseTernaryOperatorASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see UseTernaryOperatorASTVisitor
 * 
 * @since 4.18.0
 *
 */
public class UseTernaryOperatorRule extends RefactoringRuleImpl<UseTernaryOperatorASTVisitor> {

	public static final String RULE_ID = "UseTernaryOperator"; //$NON-NLS-1$

	public UseTernaryOperatorRule() {
		this.visitorClass = UseTernaryOperatorASTVisitor.class;
		this.id = RULE_ID;
		this.ruleDescription = new RuleDescription(Messages.UseTernaryOperatorRule_name,
				Messages.UseTernaryOperatorRule_description, Duration.ofMinutes(5),
				Arrays.asList(Tag.JAVA_1_1, Tag.READABILITY));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_1;
	}

}
