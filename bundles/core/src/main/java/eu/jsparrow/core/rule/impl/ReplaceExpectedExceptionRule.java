package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;
import java.util.function.Predicate;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.osgi.framework.Version;

import eu.jsparrow.core.visitor.junit.ReplaceExpectedExceptionASTVisitor;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see ReplaceExpectedExceptionASTVisitor
 * 
 * @since 3.24.0
 */
public class ReplaceExpectedExceptionRule
		extends RefactoringRuleImpl<ReplaceExpectedExceptionASTVisitor> {

	private static final String ORG_JUNIT_JUPITER_API_TEST = "org.junit.jupiter.api.Test"; //$NON-NLS-1$
	private static final String ORG_JUNIT_TEST = "org.junit.Test"; //$NON-NLS-1$
	private static final String MIN_JUNIT_4_VERSION = "4.13"; //$NON-NLS-1$
	private static final String MIN_JUNIT_5_VERSION = "5.0.0"; //$NON-NLS-1$
	private static final String JUPITER_ASSERT_THROWS = "org.junit.jupiter.api.Assertions.assertThrows"; //$NON-NLS-1$
	private static final String JUNIT_ASSERT_THROWS = "org.junit.Assert.assertThrows"; //$NON-NLS-1$
	private String assertThrowsQualifiedName = JUNIT_ASSERT_THROWS;

	public ReplaceExpectedExceptionRule() {
		this.visitorClass = ReplaceExpectedExceptionASTVisitor.class;
		this.id = "ReplaceExpectedException"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(
				"Replace ExpectedException JUnit Rule with assertThrows",
				"The ExpectedException.none() is deprecated since deprecated since JUnit 4.13. The recommended alternative is to use assertThrows(). This makes JUnit tests easier to understand and prevents the scenarios where some parts the test code is unreachable.\nThe goal of this rule is to replace expectedException.expect() with assertThrows. Additionally, new assertions are added for each invocation of expectMessage() and expectCause().",
				Duration.ofMinutes(5), Arrays.asList(Tag.JAVA_1_1, Tag.JUNIT));
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
		return "JUnit 4.13 or JUnit 5.6 and above"; //$NON-NLS-1$
	}

	@Override
	public boolean ruleSpecificImplementation(IJavaProject project) {
		Predicate<Version> versionComparator = version -> version
			.compareTo(Version.parseVersion(MIN_JUNIT_4_VERSION)) >= 0
				|| version.compareTo(Version.parseVersion(MIN_JUNIT_5_VERSION)) >= 0;
		if(isInProjectLibraries(project, ORG_JUNIT_JUPITER_API_TEST, versionComparator)) {
			this.assertThrowsQualifiedName = JUPITER_ASSERT_THROWS;
			return true;
		}
		
		if(isInProjectLibraries(project, ORG_JUNIT_TEST, versionComparator)) {
			this.assertThrowsQualifiedName = JUNIT_ASSERT_THROWS;
			return true;
		}
		return false;
	}
	
	@Override
	public ReplaceExpectedExceptionASTVisitor visitorFactory() {
		return new ReplaceExpectedExceptionASTVisitor(assertThrowsQualifiedName);
	}

}
