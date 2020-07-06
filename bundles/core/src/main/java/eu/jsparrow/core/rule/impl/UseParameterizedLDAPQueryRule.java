package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.security.UseParameterizedJPAQueryASTVisitor;
import eu.jsparrow.core.visitor.security.UseParameterizedLDAPQueryASTVisitor;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see UseParameterizedJPAQueryASTVisitor
 * 
 * @since 3.18.0
 *
 */
public class UseParameterizedLDAPQueryRule extends RefactoringRuleImpl<UseParameterizedLDAPQueryASTVisitor> {

	public UseParameterizedLDAPQueryRule() {
		this.visitorClass = UseParameterizedLDAPQueryASTVisitor.class;
		this.id = "UseParameterizedLDAPQuery"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription("Use Parameterized LDAP Query", //$NON-NLS-1$
				"Like SQL queries or JPA queries, also LDAP statements strings may be constructed by concatenating string literals with user defined expressions, therefore they are also vulnerable to injections. This rule looks for Strings which are used as filter in invocations of javax.naming.directory.DirContext#search(String name, String filter, SearchControls cons) and javax.naming.directory.DirContext#search(Name name, String filter, SearchControls cons), and vulnerable concats are replaced by parameterizing the string.", //$NON-NLS-1$
				Duration.ofMinutes(30),
				Arrays.asList(Tag.JAVA_1_3, Tag.SECURITY));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_3;
	}

	@Override
	public String requiredLibraries() {
		return "Java LDAP API"; //$NON-NLS-1$
	}
}
