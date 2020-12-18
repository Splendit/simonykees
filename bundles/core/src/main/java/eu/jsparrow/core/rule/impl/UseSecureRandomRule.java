package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.security.random.UseSecureRandomASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see UseSecureRandomASTVisitor
 * 
 * @since 3.20.0
 *
 */
public class UseSecureRandomRule extends RefactoringRuleImpl<UseSecureRandomASTVisitor> {

	public UseSecureRandomRule() {
		this.visitorClass = UseSecureRandomASTVisitor.class;
		this.id = "UseSecureRandomRule"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.UseSecureRandomRule_name,
				Messages.UseSecureRandomRule_description,
				Duration.ofMinutes(5),
				Arrays.asList(Tag.JAVA_1_2, Tag.SECURITY, Tag.FREE));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_2;
	}

}
