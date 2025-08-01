package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;
import java.util.function.Predicate;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.osgi.framework.Version;

import eu.jsparrow.core.visitor.junit.ReplaceJUnitExpectedAnnotationPropertyASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;
import eu.jsparrow.rules.common.statistics.RuleApplicationCount;

/**
 * @see ReplaceJUnitExpectedAnnotationPropertyASTVisitor
 * 
 * @since 3.24.0
 *
 */
public class ReplaceJUnitExpectedAnnotationPropertyRule
		extends RefactoringRuleImpl<ReplaceJUnitExpectedAnnotationPropertyASTVisitor> {

	private static final String ORG_JUNIT_JUPITER_API_TEST = "org.junit.jupiter.api.Test"; //$NON-NLS-1$
	private static final String ORG_JUNIT_TEST = "org.junit.Test"; //$NON-NLS-1$
	private static final String MIN_JUNIT_4_VERSION = "4.13"; //$NON-NLS-1$
	private static final String MIN_JUNIT_5_VERSION = "5.0.0"; //$NON-NLS-1$
	private static final String JUPITER_ASSERT_THROWS = "org.junit.jupiter.api.Assertions.assertThrows"; //$NON-NLS-1$
	private static final String JUNIT_ASSERT_THROWS = "org.junit.Assert.assertThrows"; //$NON-NLS-1$
	private String assertThrowsQualifiedName = JUNIT_ASSERT_THROWS;

	public static final String RULE_ID = "ReplaceJUnitExpectedAnnotationProperty"; //$NON-NLS-1$

	public ReplaceJUnitExpectedAnnotationPropertyRule() {
		this.visitorClass = ReplaceJUnitExpectedAnnotationPropertyASTVisitor.class;
		this.id = RULE_ID;
		this.ruleDescription = new RuleDescription(
				Messages.ReplaceExpectedAnnotationPropertyRule_name,
				Messages.ReplaceExpectedAnnotationPropertyRule_description,
				Duration.ofMinutes(5), Arrays.asList(Tag.JAVA_1_8, Tag.TESTING, Tag.JUNIT, Tag.LAMBDA, Tag.READABILITY));
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
	public ReplaceJUnitExpectedAnnotationPropertyASTVisitor visitorFactory() {
		ReplaceJUnitExpectedAnnotationPropertyASTVisitor visitor = new ReplaceJUnitExpectedAnnotationPropertyASTVisitor(
				assertThrowsQualifiedName);
		visitor.addRewriteListener(RuleApplicationCount.getFor(this));
		return visitor;
	}

	public String getAssertThrowsQualifiedName() {
		return this.assertThrowsQualifiedName;
	}

}
