package eu.jsparrow.core.rule.impl;

import java.time.Duration;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.rule.RuleDescription;
import eu.jsparrow.core.util.TagUtil;
import eu.jsparrow.core.visitor.impl.IndexOfToContainsASTVisitor;
import eu.jsparrow.i18n.Messages;

/**
 * 
 * @author Matthias Webhofer
 * @since 2.1.1
 *
 */
public class IndexOfToContainsRule extends RefactoringRule<IndexOfToContainsASTVisitor> {

	public IndexOfToContainsRule() {
		super();
		this.visitorClass = IndexOfToContainsASTVisitor.class;
		this.id = "IndexOfToContains"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.IndexOfToContainsRule_name,
				Messages.IndexOfToContainsRule_description, Duration.ofMinutes(2),
				TagUtil.getTagsForRule(this.getClass()));
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_5; // for lists 1.2, but for strings 1.5
	}

}
