package eu.jsparrow.core.rule.impl;

import java.time.Duration;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.impl.UseStringBuilderAppendASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see UseStringBuilderAppendASTVisitor
 * 
 * @since 2.7.0
 *
 */
public class UseStringBuilderAppendRule extends RefactoringRuleImpl<UseStringBuilderAppendASTVisitor> {

	public UseStringBuilderAppendRule() {
		this.visitorClass = UseStringBuilderAppendASTVisitor.class;
		this.id = "UseStringBuilderAppend"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.UseStringBuilderAppendRule_name,
				Messages.UseStringBuilderAppendRule_description, Duration.ofMinutes(2),
				Tag.STRING_MANIPULATION, Tag.PERFORMANCE);

	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_5;
	}

}
