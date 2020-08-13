package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.impl.UseOffsetBasedStringMethodsASTVisitor;
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
		this.ruleDescription = new RuleDescription("Use Offset Based String Methods", //$NON-NLS-1$
				"Looking for a given substring starting from a specified offset can be achieved by an operation like str.substring(beginIndex).indexOf(char1). Because each call to the substring method creates a new String, this can lead to performance problems, especially in connection with loops or with large String expressions. Therefore this rule replaces expressions like str.substring(beginIndex).indexOf(char1) by using the corresponding offset based String method. In this exaplle the code replacement will be str.indexOf(char1, beginIndex).", //$NON-NLS-1$
				Duration.ofMinutes(10),
				Arrays.asList(Tag.JAVA_1_1, Tag.PERFORMANCE));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_1;
	}
}
