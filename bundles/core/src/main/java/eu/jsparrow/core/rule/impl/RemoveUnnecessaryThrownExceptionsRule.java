package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.impl.RemoveUnnecessaryThrownExceptionsASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see RemoveUnnecessaryThrownExceptionsASTVisitor
 * 
 * @since 2.7.0
 *
 */
public class RemoveUnnecessaryThrownExceptionsRule
		extends RefactoringRuleImpl<RemoveUnnecessaryThrownExceptionsASTVisitor> {

	public static final String REMOVE_UNNECESSARY_THROWN_EXCEPTIONS_RULE_ID = "RemoveUnnecessaryThrows"; //$NON-NLS-1$

	public RemoveUnnecessaryThrownExceptionsRule() {
		this.visitorClass = RemoveUnnecessaryThrownExceptionsASTVisitor.class;
		this.id = REMOVE_UNNECESSARY_THROWN_EXCEPTIONS_RULE_ID;
		this.ruleDescription = new RuleDescription(Messages.RemoveDuplicatedThrowsRule_name,
				Messages.RemoveDuplicatedThrowsRule_description, Duration.ofMinutes(2),
				Arrays.asList(Tag.JAVA_1_1, Tag.READABILITY, Tag.FREE));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_1;
	}
}
