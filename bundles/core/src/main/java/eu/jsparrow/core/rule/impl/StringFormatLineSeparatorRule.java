package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

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

	public static final String RULE_ID = "StringFormatLineSeparator"; //$NON-NLS-1$

	public StringFormatLineSeparatorRule() {
		super();
		this.visitorClass = StringFormatLineSeparatorASTVisitor.class;
		this.id = RULE_ID;
		this.ruleDescription = new RuleDescription(Messages.StringFormatLineSeparatorRule_name,
				Messages.StringFormatLineSeparatorRule_description, Duration.ofMinutes(1),
				Arrays.asList(Tag.JAVA_1_5, Tag.STRING_MANIPULATION));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_5;
	}

}
