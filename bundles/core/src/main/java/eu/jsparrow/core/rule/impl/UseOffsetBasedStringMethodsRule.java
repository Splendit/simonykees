package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.impl.UseOffsetBasedStringMethodsASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see UseOffsetBasedStringMethodsASTVisitor
 * 
 * @since 3.20.0
 *
 */
public class UseOffsetBasedStringMethodsRule
		extends RefactoringRuleImpl<UseOffsetBasedStringMethodsASTVisitor> {

	public UseOffsetBasedStringMethodsRule() {
		this.visitorClass = UseOffsetBasedStringMethodsASTVisitor.class;
		this.id = "UseOffsetBasedStringMethods"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.UseOffsetBasedStringMethodsRule_name,
				Messages.UseOffsetBasedStringMethodsRule_description,
				Duration.ofMinutes(10),
				Arrays.asList(Tag.JAVA_1_1, Tag.PERFORMANCE));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_1;
	}
}
