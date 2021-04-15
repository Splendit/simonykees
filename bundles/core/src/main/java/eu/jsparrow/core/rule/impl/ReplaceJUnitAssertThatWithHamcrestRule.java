package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;
import java.util.function.Predicate;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.osgi.framework.Version;

import eu.jsparrow.core.visitor.junit.ReplaceJUnitAssertThatWithHamcrestASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see ReplaceJUnitAssertThatWithHamcrestASTVisitor
 * 
 * @since 3.29.0
 *
 */
public class ReplaceJUnitAssertThatWithHamcrestRule
		extends RefactoringRuleImpl<ReplaceJUnitAssertThatWithHamcrestASTVisitor> {

	private static final String MIN_HAMCREST_VERSION = "1.3"; //$NON-NLS-1$
	private static final String ORG_HAMCREST_MATCHER_ASSERT = "org.hamcrest.MatcherAssert"; //$NON-NLS-1$

	public ReplaceJUnitAssertThatWithHamcrestRule() {
		this.visitorClass = ReplaceJUnitAssertThatWithHamcrestASTVisitor.class;
		this.id = "ReplaceJUnitAssertThatWithHamcrest"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(
				Messages.ReplaceJUnitAssertThatWithHamcrestRule_name,
				Messages.ReplaceJUnitAssertThatWithHamcrestRule_description,
				Duration.ofMinutes(2), Arrays.asList(Tag.JAVA_1_5, Tag.TESTING));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_5;
	}

	@Override
	public String requiredLibraries() {
		return "Hamcrest 1.3 or later"; //$NON-NLS-1$
	}

	@Override
	public boolean ruleSpecificImplementation(IJavaProject project) {
		Predicate<Version> versionComparator = version -> version
			.compareTo(Version.parseVersion(MIN_HAMCREST_VERSION)) >= 0;
		return isInProjectLibraries(project, ORG_HAMCREST_MATCHER_ASSERT, versionComparator);
	}
}
