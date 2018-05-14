package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.apache.commons.lang3.JavaVersion;

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

	public StringLiteralEqualityCheckRule() {
		super();
		this.visitorClass = StringLiteralEqualityCheckASTVisitor.class;
		this.id = "StringLiteralEqualityCheck"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.StringLiteralEqualityCheckRule_name,
				Messages.StringLiteralEqualityCheckRule_description, Duration.ofMinutes(10),
				Arrays.asList(Tag.JAVA_1_1, Tag.STRING_MANIPULATION));
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_1;
	}

}
