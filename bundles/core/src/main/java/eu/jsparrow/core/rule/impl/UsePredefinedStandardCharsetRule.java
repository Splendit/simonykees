package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.impl.UsePredefinedStandardCharsetASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see UsePredefinedStandardCharsetASTVisitor
 * 
 * @since 3.21.0
 *
 */
public class UsePredefinedStandardCharsetRule
		extends RefactoringRuleImpl<UsePredefinedStandardCharsetASTVisitor> {

	public UsePredefinedStandardCharsetRule() {
		this.visitorClass = UsePredefinedStandardCharsetASTVisitor.class;
		this.id = "UsePredefinedStandardCharset"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.UsePredefinedStandardCharsetRule_name,
				Messages.UsePredefinedStandardCharsetRule_description,
				Duration.ofMinutes(2),
				Arrays.asList(Tag.JAVA_1_7, Tag.PERFORMANCE));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_7;
	}
}
