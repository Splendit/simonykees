package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.security.UseParameterizedJPAQueryASTVisitor;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see UseParameterizedJPAQueryASTVisitor
 * 
 * @since 3.18.0
 *
 */
public class UseParameterizedJPAQueryRule
		extends RefactoringRuleImpl<UseParameterizedJPAQueryASTVisitor> {

	public UseParameterizedJPAQueryRule() {
		this.visitorClass = UseParameterizedJPAQueryASTVisitor.class;
		this.id = "UseParameterizedJPAQuery"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription("Use Parameterized JPA Query", //$NON-NLS-1$
				"A JPQL query may be constructed by concatenating string literals with user defined expressions (e.g. variables, method invocations, user input, etc) and this may cause JPQL injection vulnerabilities. Parameterized queries enforce a distinction between the JPQL code and the data passed through parameters.", //$NON-NLS-1$
				Duration.ofMinutes(10),
				Arrays.asList(Tag.JAVA_1_1, Tag.SECURITY));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_1;
	}
}
