package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.impl.CollapseIfStatementsASTVisitor;
import eu.jsparrow.core.visitor.impl.UseTernaryOperatorASTVisitor;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see CollapseIfStatementsASTVisitor
 * 
 * @since 4.18.0
 *
 */
public class UseTernaryOperatorRule extends RefactoringRuleImpl<UseTernaryOperatorASTVisitor> {

	public static final String RULE_ID = "UseTernaryOperator"; //$NON-NLS-1$

	public UseTernaryOperatorRule() {
		this.visitorClass = UseTernaryOperatorASTVisitor.class;
		this.id = RULE_ID;
		String name = "Use Ternary Operator"; //$NON-NLS-1$
		// TODO: externalize to messages.properties in
		// eu.jsparrow.i18n.Messages;
		String description = "Replaces simple if statements by statements containing the ternary operator where this is possible."; //$NON-NLS-1$
		// TODO: externalize to messages.properties in
		// eu.jsparrow.i18n.Messages;

		this.ruleDescription = new RuleDescription(name, description, Duration.ofMinutes(10),
				Arrays.asList(Tag.JAVA_1_1, Tag.CODING_CONVENTIONS, Tag.READABILITY));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_1;
	}

}
