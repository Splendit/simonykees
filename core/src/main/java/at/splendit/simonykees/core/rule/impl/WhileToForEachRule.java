package at.splendit.simonykees.core.rule.impl;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.splendit.simonykees.core.rule.GroupEnum;
import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.util.GroupUtil;
import at.splendit.simonykees.core.visitor.loop.WhileToForEachASTVisitor;
import at.splendit.simonykees.i18n.Messages;

/**
 * @see WhileToForEachASTVisitor
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
public class WhileToForEachRule extends RefactoringRule<WhileToForEachASTVisitor> {
	
	Logger logger = LoggerFactory.getLogger(WhileToForEachASTVisitor.class);

	public WhileToForEachRule(Class<WhileToForEachASTVisitor> visitor) {
		super(visitor);
		this.name = Messages.WhileToForEachRule_name;
		this.description = Messages.WhileToForEachRule_description;
		this.groups.addAll(GroupUtil.allJavaVersionTo(GroupEnum.JAVA_5));
	}

	@Override
	public void calculateEnabledForProject(IJavaProject project) {
		project.getOptions(true).forEach((s,s1)-> logger.debug("[left:"+s+"];[right:"+s1+"]"));
		String compilerCompliance = project.getOption(JavaCore.COMPILER_COMPLIANCE, true);
		logger.debug(project.toString());
	}

}
