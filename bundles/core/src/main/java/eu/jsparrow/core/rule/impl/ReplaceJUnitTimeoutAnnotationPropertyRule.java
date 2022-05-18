package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;
import java.util.function.Predicate;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.osgi.framework.Version;

import eu.jsparrow.core.visitor.junit.ReplaceJUnitTimeoutAnnotationPropertyASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see ReplaceJUnitTimeoutAnnotationPropertyASTVisitor
 * 
 * @since 3.26.0
 *
 */
public class ReplaceJUnitTimeoutAnnotationPropertyRule
		extends RefactoringRuleImpl<ReplaceJUnitTimeoutAnnotationPropertyASTVisitor> {

	private static final String MIN_JUNIT_5_VERSION = "5.0.0"; //$NON-NLS-1$
	private static final String ORG_JUNIT_JUPITER_API_ASSERTIONS = "org.junit.jupiter.api.Assertions"; //$NON-NLS-1$
	public static final String RULE_ID = "ReplaceJUnitTimeoutAnnotationProperty"; //$NON-NLS-1$

	public ReplaceJUnitTimeoutAnnotationPropertyRule() {
		this.visitorClass = ReplaceJUnitTimeoutAnnotationPropertyASTVisitor.class;
		this.id = RULE_ID;
		this.ruleDescription = new RuleDescription(
				Messages.ReplaceJUnitTimeoutAnnotationPropertyRule_name,
				Messages.ReplaceJUnitTimeoutAnnotationPropertyRule_description,
				Duration.ofMinutes(5),
				Arrays.asList(Tag.JAVA_1_8, Tag.TESTING, Tag.JUNIT, Tag.LAMBDA, Tag.READABILITY));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		/*
		 * assertTimeout expects a lambda expression.
		 */
		return JavaCore.VERSION_1_8;
	}

	@Override
	public String requiredLibraries() {
		return "JUnit 5"; //$NON-NLS-1$
	}

	@Override
	public boolean ruleSpecificImplementation(IJavaProject project) {
		Predicate<Version> versionComparator = version -> version
			.compareTo(Version.parseVersion(MIN_JUNIT_5_VERSION)) >= 0;
		return isInProjectLibraries(project, ORG_JUNIT_JUPITER_API_ASSERTIONS, versionComparator);
	}

}
