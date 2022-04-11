package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.impl.PrimitiveObjectUseEqualsASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * This rule replaces ==, != when called on primitive objects with equals.
 * 
 * @see PrimitiveObjectUseEqualsASTVisitor
 * 
 * @author Hans-Jörg Schrödl
 * @since 2.1.1
 */
public class PrimitiveObjectUseEqualsRule extends RefactoringRuleImpl<PrimitiveObjectUseEqualsASTVisitor> {

	public static final String RULE_ID = "PrimitiveObjectUseEquals"; //$NON-NLS-1$

	public PrimitiveObjectUseEqualsRule() {
		super();
		this.visitorClass = PrimitiveObjectUseEqualsASTVisitor.class;
		this.id = RULE_ID;
		this.ruleDescription = new RuleDescription(Messages.PrimitiveObjectUseEqualsRule_name,
				Messages.PrimitiveObjectUseEqualsRule_description, Duration.ofMinutes(2),
				Arrays.asList(Tag.JAVA_1_1, Tag.CODING_CONVENTIONS, Tag.STRING_MANIPULATION));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_1;
	}

}
