package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.impl.inline.InlineLocalVariablesASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see InlineLocalVariablesASTVisitor
 * 
 * @since 4.19.0
 *
 */
public class InlineLocalVariablesRule extends RefactoringRuleImpl<InlineLocalVariablesASTVisitor> {

	public static final String RULE_ID = "InlineLocalVariables"; //$NON-NLS-1$

	public InlineLocalVariablesRule() {
		this.visitorClass = InlineLocalVariablesASTVisitor.class;
		this.id = RULE_ID;
		String name = Messages.InlineLocalVariablesRule_name;
		String description = Messages.InlineLocalVariablesRule_description;
		this.ruleDescription = new RuleDescription(name, description, Duration.ofMinutes(2),
				Arrays.asList(Tag.JAVA_1_1, Tag.CODING_CONVENTIONS, Tag.READABILITY));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_1;
	}

}
