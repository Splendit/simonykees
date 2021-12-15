package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;
import java.util.function.Predicate;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.visitor.assertj.ChainAssertJAssertThatStatementsASTVisitor;
import eu.jsparrow.core.visitor.assertj.dedicated.UseDedicatedAssertJAssertionsASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;
import eu.jsparrow.rules.common.exception.runtime.ITypeNotFoundRuntimeException;

/**
 * @see ChainAssertJAssertThatStatementsASTVisitor
 * 
 * @since 4.6.0
 *
 */
@SuppressWarnings("nls")
public class UseDedicatedAssertJAssertionsRule
		extends RefactoringRuleImpl<UseDedicatedAssertJAssertionsASTVisitor> {

	private static final Logger logger = LoggerFactory.getLogger(UseDedicatedAssertJAssertionsRule.class);

	public UseDedicatedAssertJAssertionsRule() {
		this.visitorClass = UseDedicatedAssertJAssertionsASTVisitor.class;
		this.id = "UseDedicatedAssertJAssertions"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.UseDedicatedAssertJAssertionsRule_name,
				Messages.UseDedicatedAssertJAssertionsRule_description,
				Duration.ofMinutes(5),
				Arrays.asList(Tag.JAVA_1_8, Tag.CODING_CONVENTIONS, Tag.READABILITY));
	}

	@Override
	public boolean ruleSpecificImplementation(IJavaProject project) {

		try {
			if (project.findType("org.assertj.core.api.Assertions") == null) { //$NON-NLS-1$
				return false;
			}
		} catch (JavaModelException e) {
			logger.error(e.getMessage(), new ITypeNotFoundRuntimeException());
			return false;
		}
		return true;
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_8;
	}

	@Override
	public String requiredLibraries() {
		return "AssertJ fluent assertions 3.21.0 API"; //$NON-NLS-1$
	}

	// @Override
	// TODO: discuss
	// Rule does not work in runtime eclipse if this method is used ...	
	public boolean ruleSpecificImplementation_NOT_USED(IJavaProject project) {
		Predicate<Version> assertJVersionComparator = version -> version.compareTo(Version.parseVersion("3.20.2")) >= 0
				&& version.compareTo(Version.parseVersion("3.21.9")) <= 0;

		return isInProjectLibraries(project, "org.assertj.core.api.Assertions", assertJVersionComparator);
	}
}
