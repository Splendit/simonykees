package at.splendit.simonykees.core.rule.impl;

import org.eclipse.jdt.core.IJavaProject;

import at.splendit.simonykees.core.rule.GroupEnum;
import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.util.GroupUtil;
import at.splendit.simonykees.core.visitor.loop.ForToForEachASTVisitor;
import at.splendit.simonykees.i18n.Messages;

/**
 * @see ForToForEachASTVisitor
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
public class ForToForEachRule extends RefactoringRule<ForToForEachASTVisitor> {

	public ForToForEachRule(Class<ForToForEachASTVisitor> visitor) {
		super(visitor);
		this.name = Messages.ForToForEachRule_name;
		this.description = Messages.ForToForEachRule_description;
		this.groups.addAll(GroupUtil.allJavaVersionTo(GroupEnum.JAVA_5));
	}
	
	@Override
	public void calculateEnabledForProject(IJavaProject project) {
		this.enabled = true;
	}

}
