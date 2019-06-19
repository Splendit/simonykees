package eu.jsparrow.rules.java10.factory.methods;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

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

	public CollectionsFactoryMethodsRule() {
		this.visitorClass = CollectionsFactoryMethodsASTVisitor.class;
		this.id = "CollectionsFactoryMethods"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.CollectionsFactoryMethodsRule_name,
				Messages.CollectionsFactoryMethodsRule_description, Duration.ofMinutes(5),
				Arrays.asList(Tag.JAVA_1_9, Tag.OLD_LANGUAGE_CONSTRUCTS, Tag.READABILITY));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_9;
	}

}
