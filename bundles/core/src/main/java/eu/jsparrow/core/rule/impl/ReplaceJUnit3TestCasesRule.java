package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;
import java.util.function.Predicate;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.osgi.framework.Version;

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

	private static final String ORG_JUNIT_JUPITER_API_ASSERTIONS = "org.junit.jupiter.api.Assertions"; //$NON-NLS-1$
	private static final String ORG_JUNIT_ASSERT = "org.junit.Assert"; //$NON-NLS-1$
	private static final String MIN_JUNIT_4_VERSION = "4.0"; //$NON-NLS-1$
	private static final String MIN_JUNIT_5_VERSION = "5.0"; //$NON-NLS-1$
	private boolean transformationToJupiter;

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
		return "JUnit 4.0 or JUnit 5.0"; //$NON-NLS-1$
	}

	@Override
	public boolean ruleSpecificImplementation(IJavaProject project) {
		Predicate<Version> jupiterVersionComparator = version -> version
			.compareTo(Version.parseVersion(MIN_JUNIT_5_VERSION)) >= 0;
		if (isInProjectLibraries(project, ORG_JUNIT_JUPITER_API_ASSERTIONS, jupiterVersionComparator)) {
			transformationToJupiter = true;
			return true;
		}

		Predicate<Version> junitVersionComparator = version -> version
			.compareTo(Version.parseVersion(MIN_JUNIT_4_VERSION)) >= 0;
		return isInProjectLibraries(project, ORG_JUNIT_ASSERT, junitVersionComparator);
	}

	@Override
	protected ReplaceJUnit3TestCasesASTVisitor visitorFactory() throws InstantiationException, IllegalAccessException {
		ReplaceJUnit3TestCasesASTVisitor visitor = new ReplaceJUnit3TestCasesASTVisitor(transformationToJupiter);
		visitor.addRewriteListener(RuleApplicationCount.getFor(this));
		return visitor;
	}
}