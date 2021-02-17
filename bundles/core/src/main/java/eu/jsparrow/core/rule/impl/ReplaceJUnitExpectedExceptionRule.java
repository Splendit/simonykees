package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;
import java.util.function.Predicate;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.osgi.framework.Version;

import eu.jsparrow.core.visitor.junit.ReplaceJUnitExpectedExceptionASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;
import eu.jsparrow.rules.common.statistics.RuleApplicationCount;

/**
 * @see ReplaceJUnitExpectedExceptionASTVisitor
 * 
 * @since 3.24.0
 */
public class ReplaceJUnitExpectedExceptionRule
		extends RefactoringRuleImpl<ReplaceJUnitExpectedExceptionASTVisitor> {

	private static final String ORG_JUNIT_JUPITER_API_TEST = "org.junit.jupiter.api.Test"; //$NON-NLS-1$
	private static final String ORG_JUNIT_TEST = "org.junit.Test"; //$NON-NLS-1$
	private static final String MIN_JUNIT_4_VERSION = "4.13"; //$NON-NLS-1$
	private static final String MIN_JUNIT_5_VERSION = "5.0.0"; //$NON-NLS-1$
	private static final String JUPITER_ASSERT_THROWS = "org.junit.jupiter.api.Assertions.assertThrows"; //$NON-NLS-1$
	private static final String JUNIT_ASSERT_THROWS = "org.junit.Assert.assertThrows"; //$NON-NLS-1$
	private String assertThrowsQualifiedName = JUNIT_ASSERT_THROWS;

	public ReplaceJUnitExpectedExceptionRule() {
		this.visitorClass = ReplaceJUnitExpectedExceptionASTVisitor.class;
		this.id = "ReplaceJUnitExpectedException"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(
				Messages.ReplaceExpectedExceptionRule_name,
				Messages.ReplaceExpectedExceptionRule_description,
				Duration.ofMinutes(5), Arrays.asList(Tag.JAVA_1_8, Tag.TESTING, Tag.LAMBDA, Tag.READABILITY));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		/*
		 * assertThrows expects a lambda expression.
		 */
		return JavaCore.VERSION_1_8;
	}

	@Override
	public String requiredLibraries() {
		return "JUnit 4.13 or JUnit 5"; //$NON-NLS-1$
	}

	@Override
	public boolean ruleSpecificImplementation(IJavaProject project) {
		Predicate<Version> versionComparator = version -> version
			.compareTo(Version.parseVersion(MIN_JUNIT_4_VERSION)) >= 0
				|| version.compareTo(Version.parseVersion(MIN_JUNIT_5_VERSION)) >= 0;
		if (isInProjectLibraries(project, ORG_JUNIT_JUPITER_API_TEST, versionComparator)) {
			this.assertThrowsQualifiedName = JUPITER_ASSERT_THROWS;
			return true;
		}

		if (isInProjectLibraries(project, ORG_JUNIT_TEST, versionComparator)) {
			this.assertThrowsQualifiedName = JUNIT_ASSERT_THROWS;
			return true;
		}
		return false;
	}

	@Override
	public ReplaceJUnitExpectedExceptionASTVisitor visitorFactory() {
		ReplaceJUnitExpectedExceptionASTVisitor visitor = new ReplaceJUnitExpectedExceptionASTVisitor(
				assertThrowsQualifiedName);
		visitor.addRewriteListener(RuleApplicationCount.getFor(this));
		return visitor;
	}

}
