package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.impl.HideDefaultConstructorInUtilityClassesASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * see {@link HideDefaultConstructorInUtilityClassesASTVisitor}
 * 
 * @since 3.11.0
 */
public class HideDefaultConstructorInUtilityClassesRule
		extends RefactoringRuleImpl<HideDefaultConstructorInUtilityClassesASTVisitor> {

	public HideDefaultConstructorInUtilityClassesRule() {

		this.visitorClass = HideDefaultConstructorInUtilityClassesASTVisitor.class;
		this.id = "HideDefaultConstructorInUtilityClasses"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.HideDefaultConstructorInUtilityClassesRule_name,
				Messages.HideDefaultConstructorInUtilityClassesRule_description, Duration.ofMinutes(5),
				Arrays.asList(Tag.JAVA_1_1, Tag.CODING_CONVENTIONS));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_1;
	}

}
