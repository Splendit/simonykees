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

	public UseComparatorMethodsRule() {
		this.visitorClass = UseComparatorMethodsASTVisitor.class;
		this.id = "UseComparatorMethods"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.UseComparatorMethodsRule_name,
				Messages.UseComparatorMethodsRule_description,
				Duration.ofMinutes(15),
				Arrays.asList(Tag.JAVA_1_8, Tag.CODING_CONVENTIONS));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_8;
	}
}
