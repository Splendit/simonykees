package at.splendit.simonykees.core.rule.impl;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.splendit.simonykees.core.exception.runtime.ITypeNotFoundRuntimeException;
import at.splendit.simonykees.core.rule.GroupEnum;
import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.util.GroupUtil;
import at.splendit.simonykees.core.visitor.StringUtilsASTVisitor;
import at.splendit.simonykees.i18n.Messages;

/**
 * @see StringUtilsASTVisitor
 * 
 * @author Martin Huter
 * @since 0.9
 *
 */
public class StringUtilsRule extends RefactoringRule<StringUtilsASTVisitor> {
	
	Logger logger = LoggerFactory.getLogger(StringUtilsRule.class);

	public StringUtilsRule(Class<StringUtilsASTVisitor> visitor) {
		super(visitor);
		this.name = Messages.StringUtilsRule_name;
		this.description = Messages.StringUtilsRule_description;
		this.groups.addAll(GroupUtil.allJavaVersionSince(GroupEnum.JAVA_1_1));
	}

	@Override
	public boolean ruleSpecificImplementation(IJavaProject project) {
		try {
			String fullyQuallifiedClassName = "org.apache.commons.lang3.StringUtils"; //$NON-NLS-1$
			IType classtype = project.findType(fullyQuallifiedClassName);
			if (classtype != null) {
				// StringUtils from commons lang3 is in classpath
				return true;
			} else {
				logger.debug(String.format("Class not in classpath [%s]", fullyQuallifiedClassName)); //$NON-NLS-1$
				return false;
			}
		} catch (JavaModelException e) {
			logger.error(e.getMessage(), new ITypeNotFoundRuntimeException());
			return false;
		}
	}
}
