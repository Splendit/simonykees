package at.splendit.simonykees.core.rule.impl;

import org.eclipse.jdt.core.IJavaProject;

import at.splendit.simonykees.core.rule.GroupEnum;
import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.util.GroupUtil;
import at.splendit.simonykees.core.visitor.BracketsToControlASTVisitor;
import at.splendit.simonykees.i18n.Messages;

/** 
 * @see BracketsToControlASTVisitor
 * 
 * @author Martin Huter
 * @since 0.9
 *
 */
public class BracketsToControlRule extends RefactoringRule<BracketsToControlASTVisitor> {

	public BracketsToControlRule(Class<BracketsToControlASTVisitor> visitor) {
		super(visitor);
		this.name = Messages.BracketsToControlRule_name;
		this.description = Messages.BracketsToControlRule_description;
		this.groups.addAll(GroupUtil.allJavaVersionTo(GroupEnum.JAVA_1));
	}
	
	@Override
	public void calculateEnabledForProject(IJavaProject project) {
		this.enabled = true;
	}
}
