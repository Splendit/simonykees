package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;
import java.util.function.Predicate;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.osgi.framework.Version;

import eu.jsparrow.core.visitor.junit.ReplaceJUnitAssertThatWithHamcrestASTVisitor;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

public class ReplaceJunitAssertThatWIthHamcrestRule extends RefactoringRuleImpl<ReplaceJUnitAssertThatWithHamcrestASTVisitor> {

	public ReplaceJunitAssertThatWIthHamcrestRule() {
		this.visitorClass = ReplaceJUnitAssertThatWithHamcrestASTVisitor.class;
		this.id = "ReplaceJunitAssertThatWIthHamcrest"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(
				"Replace JUnit assertThat with Hamcrest", 
				"JUnit Asserts.assertThat is deprecated. The recommended alternative is to use the equivalent assertion in Hamcrest library.",
				Duration.ofMinutes(2), Arrays.asList(Tag.JAVA_1_8, Tag.TESTING));
	}

	
	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_8;
	}

	@Override
	public String requiredLibraries() {
		return "Hamcrest"; //$NON-NLS-1$
	}
	
	@Override
	public boolean ruleSpecificImplementation(IJavaProject project) {
		Predicate<Version> versionComparator = version -> version
			.compareTo(Version.parseVersion("1.3")) >= 0;
		return isInProjectLibraries(project, "org.hamcrest.MatcherAssert", versionComparator);
	}
}
