package eu.jsparrow.core.rule.impl;

import java.time.Duration;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.util.TagUtil;
import eu.jsparrow.core.visitor.impl.ReImplementingInterfaceASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.RuleDescription;

/**
 * 
 * @author Matthias Webhofer
 * @since 2.1.1
 */
public class ReImplementingInterfaceRule extends RefactoringRule<ReImplementingInterfaceASTVisitor> {

	public ReImplementingInterfaceRule() {
		super();
		this.visitorClass = ReImplementingInterfaceASTVisitor.class;
		this.id = "ReImplementingInterface"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.ReImplementingInterfaceRule_name,
				Messages.ReImplementingInterfaceRule_description, Duration.ofMinutes(2),
				TagUtil.getTagsForRule(this.getClass()));
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_1;
	}

}
