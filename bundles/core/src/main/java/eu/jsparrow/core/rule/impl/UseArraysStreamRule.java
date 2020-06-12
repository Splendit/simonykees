package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.impl.UseArraysStreamASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see UseArraysStreamASTVisitor
 * 
 * @since 3.18.0
 *
 */
public class UseArraysStreamRule extends RefactoringRuleImpl<UseArraysStreamASTVisitor> {
	
	public UseArraysStreamRule() {
		this.id = "UseArraysStream"; //$NON-NLS-1$
		this.visitorClass = UseArraysStreamASTVisitor.class;
		this.ruleDescription = new RuleDescription(Messages.UseArraysStreamRule_name,
				Messages.UseArraysStreamRule_description, Duration.ofMinutes(5),
				Arrays.asList(Tag.JAVA_1_8, Tag.PERFORMANCE, Tag.CODING_CONVENTIONS));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_8;
	}

}
