package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.visitor.impl.StringFormatLineSeparatorASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see StringFormatLineSeparatorRule
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
public class StringFormatLineSeparatorRule extends RefactoringRuleImpl<StringFormatLineSeparatorASTVisitor> {

	public StringFormatLineSeparatorRule() {
		super();
		this.visitorClass = StringFormatLineSeparatorASTVisitor.class;
		this.id = "StringFormatLineSeparator"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.StringFormatLineSeparatorRule_name,
				Messages.StringFormatLineSeparatorRule_description, Duration.ofMinutes(1),
				Arrays.asList(Tag.JAVA_1_5, Tag.STRING_MANIPULATION));
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_5;
	}

}
