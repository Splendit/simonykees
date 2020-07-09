package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.security.UseParameterizedJPAQueryASTVisitor;
import eu.jsparrow.core.visitor.security.UseParameterizedLDAPQueryASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see UseParameterizedJPAQueryASTVisitor
 * 
 * @since 3.19.0
 *
 */
public class UseParameterizedLDAPQueryRule extends RefactoringRuleImpl<UseParameterizedLDAPQueryASTVisitor> {

	public UseParameterizedLDAPQueryRule() {
		this.visitorClass = UseParameterizedLDAPQueryASTVisitor.class;
		this.id = "UseParameterizedLDAPQuery"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.UseParameterizedLDAPQueryRule_name,
				Messages.UseParameterizedLDAPQueryRule_description,
				Duration.ofMinutes(30),
				Arrays.asList(Tag.JAVA_1_3, Tag.SECURITY));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_3;
	}
}
