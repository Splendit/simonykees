package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.exception.runtime.ITypeNotFoundRuntimeException;
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
	private static final Logger logger = LoggerFactory.getLogger(UseParameterizedLDAPQueryRule.class);

	public UseParameterizedLDAPQueryRule() {
		this.visitorClass = UseParameterizedLDAPQueryASTVisitor.class;
		this.id = "UseParameterizedLDAPQuery"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription("Use Parameterized LDAP Query", //$NON-NLS-1$
				"Like SQL queries or JPA queries, also LDAP statements strings may be constructed by concatenating string literals with user defined expressions, therefore they are also vulnerable to injections. This rule looks for Strings which are used as filter in invocations of javax.naming.directory.DirContext#search(String name, String filter, SearchControls cons) and javax.naming.directory.DirContext#search(Name name, String filter, SearchControls cons), and vulnerable concats are replaced by parameterizing the string.", //$NON-NLS-1$
				Duration.ofMinutes(30),
				Arrays.asList(Tag.JAVA_1_1, Tag.SECURITY));
	}

	//TODO: we do not need this. DirContext is Java built-in type. It will always be there. 
	@SuppressWarnings("nls")
	@Override
	public boolean ruleSpecificImplementation(IJavaProject project) {

		try {
			if (project.findType("javax.naming.directory.DirContext") == null) {
				return false;
			}
		} catch (JavaModelException e) {
			logger.error(e.getMessage(), new ITypeNotFoundRuntimeException());
			return false;
		}
		return true;
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_1; //FIXME This is 1.3. javax.naming was introduced in 1.3
	}

	@Override
	public String requiredLibraries() {
		return "Java LDAP API"; //$NON-NLS-1$
	}
}
