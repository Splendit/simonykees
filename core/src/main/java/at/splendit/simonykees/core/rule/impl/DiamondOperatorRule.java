package at.splendit.simonykees.core.rule.impl;

import org.eclipse.jdt.core.IJavaProject;

import at.splendit.simonykees.core.rule.GroupEnum;
import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.util.GroupUtil;
import at.splendit.simonykees.core.visitor.DiamondOperatorASTVisitor;
import at.splendit.simonykees.i18n.Messages;

/**
 * @see DiamondOperatorASTVisitor
 * 
 * Minimum java version that supports diamond operator is {@value JavaVersion.JAVA_1_7}
 * 
 * @author Ardit Ymeri
 * @since 1.0
 *
 */
public class DiamondOperatorRule extends RefactoringRule<DiamondOperatorASTVisitor> {

	public DiamondOperatorRule(Class<DiamondOperatorASTVisitor> visitor) {
		super(visitor);
		this.name = Messages.DiamondOperatorRule_name;
		this.description = Messages.DiamondOperatorRule_description;
		this.groups.addAll(GroupUtil.allJavaVersionTo(GroupEnum.JAVA_7));
	}
	
	@Override
	public void calculateEnabledForProject(IJavaProject project) {
		this.enabled = true;
	}

}
