package eu.jsparrow.rules.java16;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

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

	public ReplaceStringFormatByFormattedRule() {
		this.visitorClass = ReplaceStringFormatByFormattedASTVisitor.class;
		this.id = "ReplaceStringFormatByFormatted"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.ReplaceStringFormatByFormattedRule_name,
				Messages.ReplaceStringFormatByFormattedRule_description,
				Duration.ofMinutes(2),
				Arrays.asList(Tag.JAVA_15, Tag.OLD_LANGUAGE_CONSTRUCTS, Tag.READABILITY));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_15;
	}
}