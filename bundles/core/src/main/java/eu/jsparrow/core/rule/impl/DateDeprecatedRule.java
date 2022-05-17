package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.impl.DateDeprecatedASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * Searches deprecated Date constructs and replaces it with legal ones.
 * 
 * @author Martin Huter
 * @since 2.5
 *
 */
public class DateDeprecatedRule extends RefactoringRuleImpl<DateDeprecatedASTVisitor> {

	public static final String RULE_ID = "DateDeprecated"; //$NON-NLS-1$
	public DateDeprecatedRule() {
		this.visitorClass = DateDeprecatedASTVisitor.class;
		this.id = RULE_ID;
		this.ruleDescription = new RuleDescription(Messages.DateDeprecatedRule_name,
				Messages.DateDeprecatedRule_description, Duration.ofMinutes(1),
				Arrays.asList(Tag.JAVA_1_1, Tag.FORMATTING, Tag.READABILITY));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_1;
	}

}
