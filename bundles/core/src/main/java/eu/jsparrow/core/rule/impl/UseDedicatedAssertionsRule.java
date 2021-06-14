package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.visitor.junit.dedicated.UseDedicatedAssertionsASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;
import eu.jsparrow.rules.common.exception.runtime.ITypeNotFoundRuntimeException;

/**
 * @see UseDedicatedAssertionsASTVisitor
 * 
 * @since 3.32.0
 *
 */
public class UseDedicatedAssertionsRule
		extends RefactoringRuleImpl<UseDedicatedAssertionsASTVisitor> {

	private static final Logger logger = LoggerFactory.getLogger(UseDedicatedAssertionsRule.class);

	private static final String ORG_JUNIT_JUPITER_API_ASSERTIONS = "org.junit.jupiter.api.Assertions"; //$NON-NLS-1$
	private static final String ORG_JUNIT_ASSERT = "org.junit.Assert"; //$NON-NLS-1$

	public UseDedicatedAssertionsRule() {
		this.visitorClass = UseDedicatedAssertionsASTVisitor.class;
		this.id = "UseDedicatedAssertions"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(
				Messages.UseDedicatedAssertionsRule_name,
				Messages.UseDedicatedAssertionsRule_description,
				Duration.ofMinutes(2), Arrays.asList(Tag.JAVA_1_5, Tag.TESTING, Tag.CODING_CONVENTIONS));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		/*
		 * JUnit Jupiter Requires Java 5.
		 */
		return JavaCore.VERSION_1_5;
	}

	@Override
	public String requiredLibraries() {
		return "JUnit 4.0 or JUnit 5.0"; //$NON-NLS-1$
	}

	@Override
	public boolean ruleSpecificImplementation(IJavaProject project) {
		boolean usesJunit;
		try {
			usesJunit = project.findType(ORG_JUNIT_JUPITER_API_ASSERTIONS) != null || project.findType(ORG_JUNIT_ASSERT) != null;
		} catch (JavaModelException e) {
			logger.error(e.getMessage(), new ITypeNotFoundRuntimeException());
			return false;
		}
		return usesJunit;

	}
}
