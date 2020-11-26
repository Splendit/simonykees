package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;
import java.util.function.Predicate;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.osgi.framework.Version;

import eu.jsparrow.core.visitor.junit.ReplaceExpectedExceptionByAssertThrowsASTVisitor;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

public class ReplaceExpectedExceptionByAssertThrowsRule
		extends RefactoringRuleImpl<ReplaceExpectedExceptionByAssertThrowsASTVisitor> {

	private static final String ORG_JUNIT_ASSERT = "org.junit.Assert";
	private static final String MIN_JUNIT_4_VERSION = "4.13";
	private static final String MIN_JUNIT_5_VERSION = "5.0";

	public ReplaceExpectedExceptionByAssertThrowsRule() {
		this.visitorClass = ReplaceExpectedExceptionByAssertThrowsASTVisitor.class;
		this.id = "ReplaceExpectedWithAssertThrows"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(
				"Replace ExpectedException rule with assertThrows",
				"The expected exception rule is deperecated since 4.13. Assert throws should be used instead.",
				Duration.ofMinutes(10), Arrays.asList(Tag.JAVA_1_1, Tag.JUNIT));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		/*
		 * assertThrows expects a lambda expression. 
		 */
		return JavaCore.VERSION_1_8;
	}

	@Override
	public boolean ruleSpecificImplementation(IJavaProject project) {
		String fullyQuallifiedClassName = ORG_JUNIT_ASSERT;
		Predicate<Version> versionComparator = version -> version
			.compareTo(Version.parseVersion(MIN_JUNIT_4_VERSION)) >= 0
				|| version.compareTo(Version.parseVersion(MIN_JUNIT_5_VERSION)) >= 0;

		return isInProjectLibraries(project, fullyQuallifiedClassName, versionComparator);
	}

}
