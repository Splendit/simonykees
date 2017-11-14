package eu.jsparrow.core.rule.impl;

import java.time.Duration;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.rule.RuleDescription;
import eu.jsparrow.core.util.TagUtil;
import eu.jsparrow.core.visitor.impl.CollectionRemoveAllASTVisitor;
import eu.jsparrow.i18n.Messages;

/**
 * @see CollectionRemoveAllASTVisitor
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
public class CollectionRemoveAllRule extends RefactoringRule<CollectionRemoveAllASTVisitor> {

	public CollectionRemoveAllRule() {
		super();
		this.visitorClass = CollectionRemoveAllASTVisitor.class;
		this.id = "CollectionRemoveAll"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.CollectionRemoveAllRule_name,
				Messages.CollectionRemoveAllRule_description, Duration.ofMinutes(2),
				TagUtil.getTagsForRule(this.getClass()));
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_2;
	}

}
