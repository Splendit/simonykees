package eu.jsparrow.core.rule.impl;

import java.time.Duration;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.rule.RuleDescription;
import eu.jsparrow.core.util.TagUtil;
import eu.jsparrow.core.visitor.impl.StringConcatToPlusASTVisitor;
import eu.jsparrow.i18n.Messages;

/**
 * @see StringConcatToPlusASTVisitor
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
public class StringConcatToPlusRule extends RefactoringRule<StringConcatToPlusASTVisitor> {

	public StringConcatToPlusRule() {
		super();
		this.visitorClass = StringConcatToPlusASTVisitor.class;
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_1;
	}
	
	@Override
	public RuleDescription getRuleDescription() {
		return new RuleDescription(Messages.StringConcatToPlusRule_name, Messages.StringConcatToPlusRule_description,
				Duration.ofMinutes(5), TagUtil.getTagsForRule(this.getClass()));
	}
}
