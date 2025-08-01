package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.impl.ReplaceStringFormatByFormattedASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * This rule replaces invocations of the method
 * {@link String#format(String, Object...)} by invocations of the Java 15 method
 * {@code String#formatted(Object...)}.
 * 
 * @see ReplaceStringFormatByFormattedASTVisitor
 * 
 * @since 4.3.0
 * 
 */
public class ReplaceStringFormatByFormattedRule
		extends RefactoringRuleImpl<ReplaceStringFormatByFormattedASTVisitor> {

	public static final String RULE_ID = "ReplaceStringFormatByFormatted"; //$NON-NLS-1$

	public ReplaceStringFormatByFormattedRule() {
		this.visitorClass = ReplaceStringFormatByFormattedASTVisitor.class;
		this.id = RULE_ID;
		this.ruleDescription = new RuleDescription(Messages.ReplaceStringFormatByFormattedRule_name,
				Messages.ReplaceStringFormatByFormattedRule_description,
				Duration.ofMinutes(2),
				Arrays.asList(Tag.JAVA_15, Tag.STRING_MANIPULATION, Tag.READABILITY));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_15;
	}
}