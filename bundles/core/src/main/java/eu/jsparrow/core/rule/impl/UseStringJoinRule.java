package eu.jsparrow.core.rule.impl;

import java.time.Duration;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.impl.UseStringJoinASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see UseStringJoinASTVisitor
 * 
 * @since 3.15.0
 *
 */
public class UseStringJoinRule extends RefactoringRuleImpl<UseStringJoinASTVisitor> {

	public UseStringJoinRule() {
		this.visitorClass = UseStringJoinASTVisitor.class;
		this.id = "UseStringJoin"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.UseStringJoinRule_name,
				Messages.UseStringJoinRule_description, Duration.ofMinutes(5),
				Tag.STRING_MANIPULATION, Tag.PERFORMANCE);
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_8;
	}

}
