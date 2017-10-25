package eu.jsparrow.core.rule.impl;

import java.time.Duration;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.rule.RuleDescription;
import eu.jsparrow.core.util.TagUtil;
import eu.jsparrow.core.visitor.impl.StringFormatLineSeparatorASTVisitor;
import eu.jsparrow.i18n.Messages;

/**
 * @see StringFormatLineSeparatorRule
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
public class StringFormatLineSeparatorRule extends RefactoringRule<StringFormatLineSeparatorASTVisitor> {

	public StringFormatLineSeparatorRule() {
		super();
		this.visitorClass = StringFormatLineSeparatorASTVisitor.class;
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_5;
	}
	
	@Override
	public RuleDescription getRuleDescription() {
		return new RuleDescription(Messages.StringFormatLineSeparatorRule_name, Messages.StringFormatLineSeparatorRule_description,
				Duration.ofMinutes(1), TagUtil.getTagsForRule(this.getClass()));
	}
}
