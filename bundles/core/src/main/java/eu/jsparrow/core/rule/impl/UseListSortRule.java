package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.impl.UseListSortASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see UseListSortASTVisitor
 * 
 * @since 3.6.0
 */
public class UseListSortRule extends RefactoringRuleImpl<UseListSortASTVisitor> {

	public static final String RULE_ID = "UseListSort"; //$NON-NLS-1$

	public UseListSortRule() {
		this.id = RULE_ID;
		this.visitorClass = UseListSortASTVisitor.class;
		this.ruleDescription = new RuleDescription(Messages.UseListSortRule_name,
				Messages.UseListSortRule_description, Duration.ofMinutes(2), Arrays.asList(Tag.JAVA_1_8,
						Tag.OLD_LANGUAGE_CONSTRUCTS, Tag.CODING_CONVENTIONS, Tag.READABILITY));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_8;
	}

}
