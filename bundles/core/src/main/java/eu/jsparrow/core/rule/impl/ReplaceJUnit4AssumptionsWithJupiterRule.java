package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;
import java.util.function.Predicate;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.osgi.framework.Version;

import eu.jsparrow.core.visitor.junit.jupiter.assertions.ReplaceJUnit4AssumptionsWithJupiterASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see ReplaceJUnit4AssumptionsWithJupiterASTVisitor
 * 
 * @since 3.30.0
 *
 */
public class ReplaceJUnit4AssumptionsWithJupiterRule
		extends RefactoringRuleImpl<ReplaceJUnit4AssumptionsWithJupiterASTVisitor> {

	private static final String ORG_JUNIT_JUPITER_API_ASSUMPTIONS = "org.junit.jupiter.api.Assumptions"; //$NON-NLS-1$
	private static final String ORG_JUNIT_ASSUME = "org.junit.Assume"; //$NON-NLS-1$
	private static final String MIN_JUNIT_4_VERSION = "4.13"; //$NON-NLS-1$
	private static final String MIN_JUNIT_5_VERSION = "5.4"; //$NON-NLS-1$

	public ReplaceJUnit4AssumptionsWithJupiterRule() {
		this.visitorClass = ReplaceJUnit4AssumptionsWithJupiterASTVisitor.class;
		this.id = "ReplaceJUnit4AssumptionsWithJupiter"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(
				Messages.ReplaceJUnit4AssumptionsWithJupiterRule_name, 
				Messages.ReplaceJUnit4AssumptionsWithJupiterRule_description,
				Duration.ofMinutes(2), Arrays.asList(Tag.JAVA_1_8, Tag.TESTING));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		/*
		 * JUnit Jupiter Requires Java 8.
		 */
		return JavaCore.VERSION_1_8;
	}

	@Override
	public String requiredLibraries() {
		return "JUnit 4.13 and JUnit 5.4"; //$NON-NLS-1$
	}

	@Override
	public boolean ruleSpecificImplementation(IJavaProject project) {
		Predicate<Version> jupiterVersionComparator = version -> version
			.compareTo(Version.parseVersion(MIN_JUNIT_5_VERSION)) >= 0;
		Predicate<Version> junitVersionComparator = version -> version
			.compareTo(Version.parseVersion(MIN_JUNIT_4_VERSION)) >= 0;

		return isInProjectLibraries(project, ORG_JUNIT_JUPITER_API_ASSUMPTIONS, jupiterVersionComparator)
				&& isInProjectLibraries(project, ORG_JUNIT_ASSUME, junitVersionComparator);

	}
}
