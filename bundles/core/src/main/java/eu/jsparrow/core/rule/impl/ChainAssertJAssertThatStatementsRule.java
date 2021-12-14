package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.visitor.assertj.ChainAssertJAssertThatStatementsASTVisitor;
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
public class ChainAssertJAssertThatStatementsRule
		extends RefactoringRuleImpl<ChainAssertJAssertThatStatementsASTVisitor> {

	private static final Logger logger = LoggerFactory.getLogger(ChainAssertJAssertThatStatementsRule.class);

	public ChainAssertJAssertThatStatementsRule() {
		this.visitorClass = ChainAssertJAssertThatStatementsASTVisitor.class;
		this.id = "ChainAssertJAssertThatStatements"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.ChainAssertJAssertThatStatementsRule_name,
				Messages.ChainAssertJAssertThatStatementsRule_description,
				Duration.ofMinutes(5),
				Arrays.asList(Tag.JAVA_1_7, Tag.TESTING, Tag.ASSERTJ, Tag.CODING_CONVENTIONS, Tag.READABILITY));
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
		return JavaCore.VERSION_1_7;
	}

	@Override
	public String requiredLibraries() {
		return "AssertJ"; //$NON-NLS-1$
	}
}
