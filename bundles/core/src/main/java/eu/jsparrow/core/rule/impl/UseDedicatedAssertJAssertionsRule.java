package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;
import java.util.function.Predicate;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.osgi.framework.Version;

import eu.jsparrow.core.visitor.assertj.ChainAssertJAssertThatStatementsASTVisitor;
import eu.jsparrow.core.visitor.assertj.dedicated.UseDedicatedAssertJAssertionsASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see ChainAssertJAssertThatStatementsASTVisitor
 * 
 * @since 4.6.0
 *
 */
public class UseDedicatedAssertJAssertionsRule
		extends RefactoringRuleImpl<UseDedicatedAssertJAssertionsASTVisitor> {

	public UseDedicatedAssertJAssertionsRule() {
		this.visitorClass = UseDedicatedAssertJAssertionsASTVisitor.class;
		this.id = "UseDedicatedAssertJAssertions"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.UseDedicatedAssertJAssertionsRule_name,
				Messages.UseDedicatedAssertJAssertionsRule_description,
				Duration.ofMinutes(5),
				Arrays.asList(Tag.JAVA_1_8, Tag.TESTING, Tag.ASSERTJ, Tag.CODING_CONVENTIONS, Tag.READABILITY));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_8;
	}

	@Override
	public String requiredLibraries() {
		return "AssertJ [3.20.2, 3.22.x]"; //$NON-NLS-1$
	}

	@Override
	public boolean ruleSpecificImplementation(IJavaProject project) {
		Predicate<Version> assertJVersionComparator = version -> version.compareTo(Version.parseVersion("3.20.2")) >= 0 //$NON-NLS-1$
				&& version.compareTo(Version.parseVersion("3.22.9")) <= 0; //$NON-NLS-1$

		return isInProjectLibraries(project, "org.assertj.core.api.Assertions", assertJVersionComparator); //$NON-NLS-1$
	}
}
