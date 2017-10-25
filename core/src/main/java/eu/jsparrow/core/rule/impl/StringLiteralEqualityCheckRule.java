package eu.jsparrow.core.rule.impl;

import java.time.Duration;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.rule.RuleDescription;
import eu.jsparrow.core.util.TagUtil;
import eu.jsparrow.core.visitor.impl.StringLiteralEqualityCheckASTVisitor;
import eu.jsparrow.i18n.Messages;

/**
 * @see StringLiteralEqualityCheckASTVisitor
 * 
 * @author Ardit Ymeri
 * @since 1.2
 *
 */
public class StringLiteralEqualityCheckRule extends RefactoringRule<StringLiteralEqualityCheckASTVisitor> {

	public StringLiteralEqualityCheckRule() {
		super();
		this.visitorClass = StringLiteralEqualityCheckASTVisitor.class;
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_1;
	}
	
	@Override
	public RuleDescription getRuleDescription() {
		return new RuleDescription(Messages.StringLiteralEqualityCheckRule_name, Messages.StringLiteralEqualityCheckRule_description,
				Duration.ofMinutes(10), TagUtil.getTagsForRule(this.getClass()));
	}
}
