package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.visitor.impl.DateDeprecatedASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * Searches deprecated Date constructs and replaces it with legal ones.
 * 
 * @author Martin Huter
 * @since 2.5
 *
 */
public class DateDeprecatedRule extends RefactoringRule<DateDeprecatedASTVisitor> {

	public DateDeprecatedRule() {
		this.visitorClass = DateDeprecatedASTVisitor.class;
		this.id = "DateDeprecated"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.DateDeprecatedRule_name,
				Messages.DateDeprecatedRule_description, Duration.ofMinutes(1),
				Arrays.asList(Tag.JAVA_1_1, Tag.FORMATTING, Tag.READABILITY));
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_1;
	}

}
