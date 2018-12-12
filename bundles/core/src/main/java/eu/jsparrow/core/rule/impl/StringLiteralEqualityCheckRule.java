package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.impl.StringLiteralEqualityCheckASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see StringLiteralEqualityCheckASTVisitor
 * 
 * @author Ardit Ymeri
 * @since 1.2
 *
 */
public class StringLiteralEqualityCheckRule extends RefactoringRuleImpl<StringLiteralEqualityCheckASTVisitor> {

	public static final String STRING_LITERAL_EQUALITY_CHECK_RULE_ID = "StringLiteralEqualityCheck"; //$NON-NLS-1$

	public StringLiteralEqualityCheckRule() {
		super();
		this.visitorClass = StringLiteralEqualityCheckASTVisitor.class;
		this.id = STRING_LITERAL_EQUALITY_CHECK_RULE_ID;
		this.ruleDescription = new RuleDescription(Messages.StringLiteralEqualityCheckRule_name,
				Messages.StringLiteralEqualityCheckRule_description, Duration.ofMinutes(10),
				Arrays.asList(Tag.JAVA_1_1, Tag.STRING_MANIPULATION));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_1;
	}

	@Override
	public boolean isFree() {
		return true;
	}
}
