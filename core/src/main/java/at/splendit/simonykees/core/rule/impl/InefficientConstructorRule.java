package at.splendit.simonykees.core.rule.impl;

import at.splendit.simonykees.core.rule.GroupEnum;
import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.util.GroupUtil;
import at.splendit.simonykees.core.visitor.InefficientConstructorASTVisitor;
import at.splendit.simonykees.i18n.Messages;

/**
 * @see InefficientConstructorASTVisitor
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
public class InefficientConstructorRule extends RefactoringRule<InefficientConstructorASTVisitor> {

	public InefficientConstructorRule(Class<InefficientConstructorASTVisitor> visitor) {
		super(visitor);
		this.name = Messages.InefficientConstructorRule_name;
		this.description = Messages.InefficientConstructorRule_description;
		this.groups.addAll(GroupUtil.allJavaVersionSince(GroupEnum.JAVA_1_5));
	}
}
