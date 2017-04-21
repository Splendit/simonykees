package at.splendit.simonykees.core.rule.impl;

import org.eclipse.jdt.core.IJavaProject;

import at.splendit.simonykees.core.rule.GroupEnum;
import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.util.GroupUtil;
import at.splendit.simonykees.core.visitor.RearrangeClassMembersASTVisitor;
import at.splendit.simonykees.i18n.Messages;

/**
 * @see RearrangeClassMembersASTVisitor
 * 
 * @author Ardit Ymeri
 * @since 1.1
 */
public class RearrangeClassMembersRule extends RefactoringRule<RearrangeClassMembersASTVisitor> {

	public RearrangeClassMembersRule(Class<RearrangeClassMembersASTVisitor> visitor) {
		super(visitor);
		this.name = Messages.RearrangeClassMembersRule_name;
		this.description = Messages.RearrangeClassMembersRule_description;
		this.groups.addAll(GroupUtil.allJavaVersionTo(GroupEnum.JAVA_1));
	}
	
	@Override
	public void calculateEnabledForProject(IJavaProject project) {
		this.enabled = true;
	}

}
