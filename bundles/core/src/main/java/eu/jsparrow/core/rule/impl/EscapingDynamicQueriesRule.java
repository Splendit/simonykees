package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.exception.runtime.ITypeNotFoundRuntimeException;
import eu.jsparrow.core.visitor.security.EscapingDynamicQueriesASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * 
 * @since 3.17.0
 *
 */
public class EscapingDynamicQueriesRule extends RefactoringRuleImpl<EscapingDynamicQueriesASTVisitor> {

	Logger logger = LoggerFactory.getLogger(EscapingDynamicQueriesRule.class);

	public EscapingDynamicQueriesRule() {
		this.visitorClass = EscapingDynamicQueriesASTVisitor.class;
		this.id = "EscapingDynamicQueries"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription("Escaping Dynamic Queries", //$NON-NLS-1$
				"This rule escapes parameters in dynamic queries.", //$NON-NLS-1$
				Duration.ofMinutes(5),
				Arrays.asList(Tag.JAVA_1_1, Tag.SECURITY));
	}

	@Override
	public boolean ruleSpecificImplementation(IJavaProject project) {

		for (String fullyQuallifiedClassName : EscapingDynamicQueriesASTVisitor.IMPORTS_FOR_ESCAPE) {
			try {
				if (project.findType(fullyQuallifiedClassName) == null) {
					return false;
				}
			} catch (JavaModelException e) {
				logger.error(e.getMessage(), new ITypeNotFoundRuntimeException());
				return false;
			}
		}
		return true;
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_1;
	}

}
