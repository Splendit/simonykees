package eu.jsparrow.core.rule.impl;

import java.time.Duration;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.util.TagUtil;
import eu.jsparrow.core.visitor.impl.ImmutableStaticFinalCollectionsASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.RuleDescription;

/**
 * 
 * @author Matthias Webhofer
 * @since 2.1.1
 */
public class ImmutableStaticFinalCollectionsRule extends RefactoringRule<ImmutableStaticFinalCollectionsASTVisitor> {

	public ImmutableStaticFinalCollectionsRule() {
		super();
		this.visitorClass = ImmutableStaticFinalCollectionsASTVisitor.class;
		this.id = "ImmutableStaticFinalCollections"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.ImmutableStaticFinalCollectionsRule_name,
				Messages.ImmutableStaticFinalCollectionsRule_description, Duration.ofMinutes(10),
				TagUtil.getTagsForRule(this.getClass()));
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_2;
	}

}
