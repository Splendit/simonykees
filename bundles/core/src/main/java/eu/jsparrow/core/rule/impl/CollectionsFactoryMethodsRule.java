package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.factory.methods.CollectionsFactoryMethodsASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see CollectionsFactoryMethodsASTVisitor
 * 
 * @since 3.6.0
 *
 */
public class CollectionsFactoryMethodsRule extends RefactoringRuleImpl<CollectionsFactoryMethodsASTVisitor> {

	public static final String RULE_ID = "CollectionsFactoryMethods"; //$NON-NLS-1$

	public CollectionsFactoryMethodsRule() {
		this.visitorClass = CollectionsFactoryMethodsASTVisitor.class;
		this.id = RULE_ID;
		this.ruleDescription = new RuleDescription(Messages.CollectionsFactoryMethodsRule_name,
				Messages.CollectionsFactoryMethodsRule_description, Duration.ofMinutes(5),
				Arrays.asList(Tag.JAVA_9, Tag.OLD_LANGUAGE_CONSTRUCTS, Tag.READABILITY));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_9;
	}

}
