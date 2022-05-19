package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.impl.RemoveExplicitCallToSuperASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see RemoveExplicitCallToSuperASTVisitor
 * 
 * @since 2.7.0
 *
 */
public class RemoveExplicitCallToSuperRule extends RefactoringRuleImpl<RemoveExplicitCallToSuperASTVisitor> {

	public static final String RULE_ID = "RemoveExplicitCallToSuper"; //$NON-NLS-1$

	public RemoveExplicitCallToSuperRule() {
		this.visitorClass = RemoveExplicitCallToSuperASTVisitor.class;
		this.id = RULE_ID;
		this.ruleDescription = new RuleDescription(Messages.RemoveExplicitCallToSuperRule_name,
				Messages.RemoveExplicitCallToSuperRule_description, Duration.ofMinutes(1),
				Arrays.asList(Tag.JAVA_1_1, Tag.READABILITY));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_1;
	}

}
