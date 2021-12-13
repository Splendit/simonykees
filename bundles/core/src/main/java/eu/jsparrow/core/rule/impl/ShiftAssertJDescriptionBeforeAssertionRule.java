package eu.jsparrow.core.rule.impl;

import java.time.Duration;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.visitor.assertj.ShiftAssertJDescriptionBeforeAssertionASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;
import eu.jsparrow.rules.common.exception.runtime.ITypeNotFoundRuntimeException;

public class ShiftAssertJDescriptionBeforeAssertionRule extends RefactoringRuleImpl<ShiftAssertJDescriptionBeforeAssertionASTVisitor> {

	private static final Logger logger = LoggerFactory.getLogger(ShiftAssertJDescriptionBeforeAssertionRule.class);

	public ShiftAssertJDescriptionBeforeAssertionRule() {
		this.visitorClass = ShiftAssertJDescriptionBeforeAssertionASTVisitor.class;
		this.id = "ShiftAssertJDescriptionBeforeAssertion"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.ShiftAssertJDescriptionBeforeAssertionRule_name,
				Messages.ShiftAssertJDescriptionBeforeAssertionRule_description, 
				Duration.ofMinutes(5),
				Tag.JAVA_1_7, Tag.TESTING, Tag.ASSERTJ, Tag.CODING_CONVENTIONS);
	}
	
	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_7;
	}
	
	@Override
	public boolean ruleSpecificImplementation(IJavaProject project) {
		try {
			if (project.findType("org.assertj.core.api.Descriptable") == null) { //$NON-NLS-1$
				return false;
			}
			if (project.findType("org.assertj.core.api.AbstractAssert") == null) { //$NON-NLS-1$
				return false;
			}
		} catch (JavaModelException e) {
			logger.error(e.getMessage(), new ITypeNotFoundRuntimeException());
			return false;
		}
		return true;
	}
	
	@Override
	public String requiredLibraries() {
		return "AssertJ"; //$NON-NLS-1$
	}

}
