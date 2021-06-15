package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.visitor.junit.junit3.Junit3MigrationConfiguration;
import eu.jsparrow.core.visitor.junit.junit3.Junit3MigrationConfigurationFactory;
import eu.jsparrow.core.visitor.junit.junit3.ReplaceJUnit3TestCasesASTVisitor;
import eu.jsparrow.core.visitor.junit.jupiter.assertions.ReplaceJUnit4AssertionsWithJupiterASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;
import eu.jsparrow.rules.common.statistics.RuleApplicationCount;

/**
 * @see ReplaceJUnit4AssertionsWithJupiterASTVisitor
 * 
 *
 */
public class ReplaceJUnit3TestCasesRule
		extends RefactoringRuleImpl<ReplaceJUnit3TestCasesASTVisitor> {

	private static final Logger logger = LoggerFactory.getLogger(ReplaceJUnit3TestCasesRule.class);

	private static final String ORG_JUNIT_JUPITER_API_ASSERTIONS = "org.junit.jupiter.api.Assertions"; //$NON-NLS-1$
	private static final String ORG_JUNIT_ASSERT = "org.junit.Assert"; //$NON-NLS-1$
	private Junit3MigrationConfiguration junit3MigrationConfiguration;

	public ReplaceJUnit3TestCasesRule() {
		this.visitorClass = ReplaceJUnit3TestCasesASTVisitor.class;
		this.id = "ReplaceJUnit3TestCases"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(
				Messages.ReplaceJUnit3TestCasesRule_name,
				Messages.ReplaceJUnit3TestCasesRule_description,
				Duration.ofMinutes(2), Arrays.asList(Tag.JAVA_1_5, Tag.TESTING));
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
		return "JUnit 4 or JUnit 5"; //$NON-NLS-1$
	}

	@Override
	public boolean ruleSpecificImplementation(IJavaProject project) {

		try {
			if (project.findType(ORG_JUNIT_JUPITER_API_ASSERTIONS) != null) {
				junit3MigrationConfiguration = new Junit3MigrationConfigurationFactory().createJUnitJupiterConfigurationValues();
				return true;
			}
		} catch (JavaModelException e) {
			logger.debug("Cannot find type {} in the classpath.", ORG_JUNIT_JUPITER_API_ASSERTIONS, e); //$NON-NLS-1$
		}

		try {
			if (project.findType(ORG_JUNIT_ASSERT) != null) {
				junit3MigrationConfiguration = new Junit3MigrationConfigurationFactory().createJUnit4ConfigurationValues();
				return true;
				
			}
		} catch (JavaModelException e) {
			logger.debug("Cannot find type {} in the classpath.", ORG_JUNIT_ASSERT, e); //$NON-NLS-1$
		}

		return false;
	}

	@Override
	protected ReplaceJUnit3TestCasesASTVisitor visitorFactory() throws InstantiationException, IllegalAccessException {
		ReplaceJUnit3TestCasesASTVisitor visitor = new ReplaceJUnit3TestCasesASTVisitor(junit3MigrationConfiguration);
		visitor.addRewriteListener(RuleApplicationCount.getFor(this));
		return visitor;
	}
}