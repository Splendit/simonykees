package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.exception.runtime.ITypeNotFoundRuntimeException;
import eu.jsparrow.core.visitor.security.UseParameterizedJPAQueryASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see UseParameterizedJPAQueryASTVisitor
 * 
 * @since 3.18.0
 *
 */
public class UseParameterizedJPAQueryRule extends RefactoringRuleImpl<UseParameterizedJPAQueryASTVisitor> {
	private static final Logger logger = LoggerFactory.getLogger(UseParameterizedJPAQueryRule.class);

	public UseParameterizedJPAQueryRule() {
		this.visitorClass = UseParameterizedJPAQueryASTVisitor.class;
		this.id = "UseParameterizedJPAQuery"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.UseParameterizedJPAQueryRule_name,
				Messages.UseParameterizedJPAQueryRule_description,
				Duration.ofMinutes(10),
				Arrays.asList(Tag.JAVA_1_1, Tag.SECURITY));
	}

	@SuppressWarnings("nls")
	@Override
	public boolean ruleSpecificImplementation(IJavaProject project) {

		try {
			if (project.findType("javax.persistence.EntityManager") == null) {
				return false;
			}
			if (project.findType("javax.persistence.Query") == null) {
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
		return JavaCore.VERSION_1_1;
	}

	@Override
	public String requiredLibraries() {
		return "The Java Persistence API (JPA)"; //$NON-NLS-1$
	}
}
