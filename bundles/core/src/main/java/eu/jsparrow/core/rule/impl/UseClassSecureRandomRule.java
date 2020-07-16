package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.security.random.UseClassSecureRandomASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see UseClassSecureRandomASTVisitor
 * 
 * @since 3.20.0
 *
 */
public class UseClassSecureRandomRule extends RefactoringRuleImpl<UseClassSecureRandomASTVisitor> {

	public UseClassSecureRandomRule() {
		this.visitorClass = UseClassSecureRandomASTVisitor.class;
		this.id = "UseClassSecureRandomRule"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.UseClassSecureRandomRule_name,
				Messages.UseClassSecureRandomRule_description,
				Duration.ofMinutes(5),
				Arrays.asList(Tag.JAVA_1_2, Tag.SECURITY));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_2;
	}

}
