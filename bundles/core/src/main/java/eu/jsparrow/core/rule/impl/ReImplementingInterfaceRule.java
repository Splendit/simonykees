package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.apache.commons.lang3.JavaVersion;

import eu.jsparrow.core.visitor.impl.ReImplementingInterfaceASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * 
 * @author Matthias Webhofer
 * @since 2.1.1
 */
public class ReImplementingInterfaceRule extends RefactoringRuleImpl<ReImplementingInterfaceASTVisitor> {

	public ReImplementingInterfaceRule() {
		super();
		this.visitorClass = ReImplementingInterfaceASTVisitor.class;
		this.id = "ReImplementingInterface"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.ReImplementingInterfaceRule_name,
				Messages.ReImplementingInterfaceRule_description, Duration.ofMinutes(2),
				Arrays.asList(Tag.JAVA_1_1, Tag.CODING_CONVENTIONS));
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_1;
	}

}
