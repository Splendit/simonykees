package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.exception.runtime.ITypeNotFoundRuntimeException;
import eu.jsparrow.core.visitor.security.EscapeUserInputsInSQLQueriesASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * 
 * @see EscapeUserInputsInSQLQueriesASTVisitor
 * 
 * @since 3.17.0
 *
 */
public class EscapeUserInputsInSQLQueriesRule extends RefactoringRuleImpl<EscapeUserInputsInSQLQueriesASTVisitor> {

	private static final Logger logger = LoggerFactory.getLogger(EscapeUserInputsInSQLQueriesRule.class);

	public EscapeUserInputsInSQLQueriesRule() {
		this.visitorClass = EscapeUserInputsInSQLQueriesASTVisitor.class;
		this.id = "EscapeUserInputsInSQLQueries"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.EscapeUserInputsInSQLQueriesRule_name,
				Messages.EscapeUserInputsInSQLQueriesRule_description,
				Duration.ofMinutes(5),
				Arrays.asList(Tag.JAVA_1_1, Tag.SECURITY));
	}

	@Override
	public boolean ruleSpecificImplementation(IJavaProject project) {

		for (String fullyQuallifiedClassName : EscapeUserInputsInSQLQueriesASTVisitor.CODEC_TYPES_QUALIFIED_NAMES) {
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
	
	@Override
	public String requiredLibraries() {
		return "The Enterprise Security API (ESAPI)"; //$NON-NLS-1$
	}

}
