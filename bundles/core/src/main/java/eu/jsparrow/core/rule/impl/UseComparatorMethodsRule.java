package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.impl.comparatormethods.UseComparatorMethodsASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see UseComparatorMethodsASTVisitor
 * 
 * @since 3.23.0
 *
 */
public class UseComparatorMethodsRule
		extends RefactoringRuleImpl<UseComparatorMethodsASTVisitor> {

	public static final String RULE_ID = "UseComparatorMethods"; //$NON-NLS-1$
	public UseComparatorMethodsRule() {
		this.visitorClass = UseComparatorMethodsASTVisitor.class;
		this.id = RULE_ID;
		this.ruleDescription = new RuleDescription(Messages.UseComparatorMethodsRule_name,
				Messages.UseComparatorMethodsRule_description,
				Duration.ofMinutes(5),
				Arrays.asList(Tag.JAVA_1_8, Tag.LAMBDA, Tag.READABILITY, Tag.CODING_CONVENTIONS));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_8;
	}
}
