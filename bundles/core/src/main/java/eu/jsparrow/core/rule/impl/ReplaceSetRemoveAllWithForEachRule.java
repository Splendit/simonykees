package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.impl.ReplaceSetRemoveAllWithForEachASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * This rule replaces invocations of the method 'removeAll' by corresponding
 * 'forEach' constructs which have a better performance.
 * <p>
 * For example,
 * 
 * <pre>
 * mySet.removeAll(myList);
 * </pre>
 * 
 * is replaced by
 * 
 * <pre>
 * mySet.forEach(mySet::remove);
 * </pre>
 * 
 * @see ReplaceSetRemoveAllWithForEachASTVisitor
 * 
 * @since 4.13.0
 * 
 */
public class ReplaceSetRemoveAllWithForEachRule
		extends RefactoringRuleImpl<ReplaceSetRemoveAllWithForEachASTVisitor> {

	public static final String RULE_ID = "ReplaceSetRemoveAllWithForEach"; //$NON-NLS-1$

	public ReplaceSetRemoveAllWithForEachRule() {
		this.visitorClass = ReplaceSetRemoveAllWithForEachASTVisitor.class;
		this.id = RULE_ID;
		this.ruleDescription = new RuleDescription(Messages.ReplaceSetRemoveAllWithForEachRule_name,
				Messages.ReplaceSetRemoveAllWithForEachRule_description,
				Duration.ofMinutes(5),
				Arrays.asList(Tag.JAVA_1_8, Tag.PERFORMANCE));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_8;
	}
}