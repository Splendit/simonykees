package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.impl.trycatch.close.RemoveRedundantCloseASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see RemoveRedundantCloseASTVisitor
 * 
 * @since 4.11.0
 */
public class RemoveRedundantCloseRule extends RefactoringRuleImpl<RemoveRedundantCloseASTVisitor> {

	public static final String RULE_ID = "RemoveRedundantClose"; //$NON-NLS-1$

	public RemoveRedundantCloseRule() {
		super();
		this.visitorClass = RemoveRedundantCloseASTVisitor.class;
		this.id = RULE_ID;
		this.ruleDescription = new RuleDescription(Messages.RemoveRedundantCloseRule_name,
				Messages.RemoveRedundantCloseRule_description,
				Duration.ofMinutes(2),
				Arrays.asList(Tag.JAVA_1_7, Tag.CODING_CONVENTIONS, Tag.READABILITY));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_7;
	}
}
