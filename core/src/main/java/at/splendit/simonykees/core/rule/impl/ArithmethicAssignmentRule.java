package at.splendit.simonykees.core.rule.impl;

import org.eclipse.jdt.core.IJavaProject;

import at.splendit.simonykees.core.rule.GroupEnum;
import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.util.GroupUtil;
import at.splendit.simonykees.core.visitor.arithmetic.ArithmethicAssignmentASTVisitor;
import at.splendit.simonykees.i18n.Messages;

/** 
 * @see ArithmethicAssignmentASTVisitor
 * 
 * @author Martin Huter
 * @since 0.9
 *
 */
public class ArithmethicAssignmentRule extends RefactoringRule<ArithmethicAssignmentASTVisitor> {

	public ArithmethicAssignmentRule(Class<ArithmethicAssignmentASTVisitor> visitor) {
		super(visitor);
		this.name = Messages.ArithmethicAssignmentRule_name;
		this.description = Messages.ArithmethicAssignmentRule_description;
		this.groups.addAll(GroupUtil.allJavaVersionTo(GroupEnum.JAVA_4));
	}
	
	@Override
	public void calculateEnabledForProject(IJavaProject project) {
		this.enabled = true;
	}

}
