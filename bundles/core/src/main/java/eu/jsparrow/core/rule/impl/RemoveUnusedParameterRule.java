package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.impl.RemoveUnusedParameterASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see RemoveUnusedParameterASTVisitor
 * 
 * @since 3.4.0
 *
 */
public class RemoveUnusedParameterRule extends RefactoringRuleImpl<RemoveUnusedParameterASTVisitor> {

	public static final String RULE_ID = "RemoveUnusedParameter"; //$NON-NLS-1$

	public RemoveUnusedParameterRule() {
		this.visitorClass = RemoveUnusedParameterASTVisitor.class;
		this.id = RULE_ID;
		this.ruleDescription = new RuleDescription(Messages.RemoveUnusedParameterRule_name,
				Messages.RemoveUnusedParameterRule_description,
				Duration.ofMinutes(5), Arrays.asList(Tag.JAVA_1_1, Tag.READABILITY, Tag.CODING_CONVENTIONS));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_1;
	}

}
