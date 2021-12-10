package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.impl.CollectionRemoveAllASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see CollectionRemoveAllASTVisitor
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
public class CollectionRemoveAllRule extends RefactoringRuleImpl<CollectionRemoveAllASTVisitor> {

	public static final String RULE_ID = "CollectionRemoveAll"; //$NON-NLS-1$
	public CollectionRemoveAllRule() {
		super();
		this.visitorClass = CollectionRemoveAllASTVisitor.class;
		this.id = RULE_ID;
		this.ruleDescription = new RuleDescription(Messages.CollectionRemoveAllRule_name,
				Messages.CollectionRemoveAllRule_description, Duration.ofMinutes(2),
				Arrays.asList(Tag.JAVA_1_2, Tag.OLD_LANGUAGE_CONSTRUCTS));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_2;
	}

}
