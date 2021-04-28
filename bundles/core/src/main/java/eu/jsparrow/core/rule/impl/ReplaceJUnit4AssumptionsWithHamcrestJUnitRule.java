package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;
import java.util.function.Predicate;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.osgi.framework.Version;

import eu.jsparrow.core.visitor.junit.jupiter.assertions.ReplaceJUnit4AssumptionsWithHamcrestJUnitASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see ReplaceJUnit4AssumptionsWithHamcrestJUnitASTVisitor
 * 
 * @since 3.31.0
 *
 */
public class ReplaceJUnit4AssumptionsWithHamcrestJUnitRule
		extends RefactoringRuleImpl<ReplaceJUnit4AssumptionsWithHamcrestJUnitASTVisitor> {

	private static final String ORG_HAMCREST_JUNIT_JUPITER_MATCHER_ASSUME = "org.hamcrest.junit.MatcherAssume"; //$NON-NLS-1$
	private static final String ORG_JUNIT_ASSUME = "org.junit.Assume"; //$NON-NLS-1$
	private static final String MIN_JUNIT_4_VERSION = "4.13"; //$NON-NLS-1$
	private static final String MIN_HAMCREST_JUNIT_VERSION = "1.0.0.0"; //$NON-NLS-1$

	public ReplaceJUnit4AssumptionsWithHamcrestJUnitRule() {
		this.visitorClass = ReplaceJUnit4AssumptionsWithHamcrestJUnitASTVisitor.class;
		this.id = "ReplaceJUnit4AssumptionsWithHamcrestJUnit"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(
				Messages.ReplaceJUnit4AssumptionsWithHamcrestJUnitRule_name,
				Messages.ReplaceJUnit4AssumptionsWithHamcrestJUnitRule_description,
				Duration.ofMinutes(2), Arrays.asList(Tag.JAVA_1_8, Tag.TESTING));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		/*
		 * Hamcrest JUnit Requires Java 8.
		 * 
		 */
		return JavaCore.VERSION_1_8;
	}

	@Override
	public String requiredLibraries() {
		return "JUnit 4.13 and Hamcrest JUnit 1.0"; //$NON-NLS-1$
	}

	@Override
	public boolean ruleSpecificImplementation(IJavaProject project) {
		Predicate<Version> jupiterVersionComparator = version -> version
			.compareTo(Version.parseVersion(MIN_HAMCREST_JUNIT_VERSION)) >= 0;
		Predicate<Version> junitVersionComparator = version -> version
			.compareTo(Version.parseVersion(MIN_JUNIT_4_VERSION)) >= 0;

		return isInProjectLibraries(project, ORG_HAMCREST_JUNIT_JUPITER_MATCHER_ASSUME, jupiterVersionComparator)
				&& isInProjectLibraries(project, ORG_JUNIT_ASSUME, junitVersionComparator);

	}
}
