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
 * @since 3.21.0
 *
 */
public class UseOffsetBasedStringMethodsRule
		extends RefactoringRuleImpl<UseOffsetBasedStringMethodsASTVisitor> {

	public static final String RULE_ID = "UseOffsetBasedStringMethods"; //$NON-NLS-1$

	public UseOffsetBasedStringMethodsRule() {
		this.visitorClass = UseOffsetBasedStringMethodsASTVisitor.class;
		this.id = RULE_ID;
		this.ruleDescription = new RuleDescription(Messages.UseOffsetBasedStringMethodsRule_name,
				Messages.UseOffsetBasedStringMethodsRule_description,
				Duration.ofMinutes(5),
				Arrays.asList(Tag.JAVA_1_1, Tag.STRING_MANIPULATION, Tag.PERFORMANCE, Tag.FREE));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_1;
	}
}
