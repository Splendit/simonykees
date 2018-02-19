package eu.jsparrow.core.rule.impl;

import java.time.Duration;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.util.TagUtil;
import eu.jsparrow.core.visitor.impl.RemoveToStringOnStringASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.RuleDescription;

/**
 * @see RemoveToStringOnStringASTVisitor
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
public class RemoveToStringOnStringRule extends RefactoringRule<RemoveToStringOnStringASTVisitor> {

	public RemoveToStringOnStringRule() {
		super();
		this.visitorClass = RemoveToStringOnStringASTVisitor.class;
		this.id = "RemoveToStringOnString"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.RemoveToStringOnStringRule_name,
				Messages.RemoveToStringOnStringRule_description, Duration.ofMinutes(2),
				TagUtil.getTagsForRule(this.getClass()));
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_1;
	}

}
