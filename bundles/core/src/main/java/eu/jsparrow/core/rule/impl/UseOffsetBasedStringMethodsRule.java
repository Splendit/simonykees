package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.impl.indexof.UseOffsetBasedStringMethodsASTVisitor;
import eu.jsparrow.core.visitor.security.UseParameterizedQueryASTVisitor;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see UseParameterizedQueryASTVisitor
 * 
 * @since 3.16.0
 *
 */
public class UseOffsetBasedStringMethodsRule
		extends RefactoringRuleImpl<UseOffsetBasedStringMethodsASTVisitor> {

	public UseOffsetBasedStringMethodsRule() {
		this.visitorClass = UseOffsetBasedStringMethodsASTVisitor.class;
		this.id = "UseOffsetBasedStringMethods"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription("", "", Duration.ofMinutes(10),  //$NON-NLS-1$//$NON-NLS-2$
				Arrays.asList(Tag.JAVA_1_1, Tag.PERFORMANCE));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_1;
	}
}
