package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.impl.ReplaceLoggingWithForeignClassASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see ReplaceLoggingWithForeignClassASTVisitor
 * 
 * @since 4.13.0
 *
 */
public class ReplaceLoggingWithForeignClassRule extends RefactoringRuleImpl<ReplaceLoggingWithForeignClassASTVisitor> {

	public static final String RULE_ID = "ReplaceLoggingWithForeignClass"; //$NON-NLS-1$

	public ReplaceLoggingWithForeignClassRule() {
		this.visitorClass = ReplaceLoggingWithForeignClassASTVisitor.class;
		this.id = RULE_ID;
		this.ruleDescription = new RuleDescription(Messages.ReplaceLoggingWithForeignClassRule_name,
				Messages.ReplaceLoggingWithForeignClassRule_description,
				Duration.ofMinutes(2),
				Arrays.asList(Tag.JAVA_1_1, Tag.READABILITY));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_1;
	}

}
