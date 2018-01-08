package eu.jsparrow.core.rule.impl;

import java.time.Duration;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.rule.RuleDescription;
import eu.jsparrow.core.util.TagUtil;
import eu.jsparrow.core.visitor.impl.trycatch.TryWithResourceASTVisitor;
import eu.jsparrow.i18n.Messages;

/**
 * @see TryWithResourceASTVisitor
 * 
 * @author Martin Huter
 * @since 0.9
 *
 */
public class TryWithResourceRule extends RefactoringRule<TryWithResourceASTVisitor> {

	public TryWithResourceRule() {
		super();
		this.visitorClass = TryWithResourceASTVisitor.class;
		this.id = "TryWithResource"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.TryWithResourceRule_name,
				Messages.TryWithResourceRule_description, Duration.ofMinutes(15),
				TagUtil.getTagsForRule(this.getClass()));
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_7;
	}

}
