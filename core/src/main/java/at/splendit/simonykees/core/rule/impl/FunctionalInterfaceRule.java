package at.splendit.simonykees.core.rule.impl;

import at.splendit.simonykees.core.rule.GroupEnum;
import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.util.GroupUtil;
import at.splendit.simonykees.core.visitor.FunctionalInterfaceASTVisitor;
import at.splendit.simonykees.i18n.Messages;

/** 
 * @see FunctionalInterfaceASTVisitor
 * 
 * @author Martin Huter
 * @since 0.9
 *
 */
public class FunctionalInterfaceRule extends RefactoringRule<FunctionalInterfaceASTVisitor> {

	public FunctionalInterfaceRule(Class<FunctionalInterfaceASTVisitor> visitor) {
		super(visitor);
		this.name = Messages.FunctionalInterfaceRule_name;
		this.description = Messages.FunctionalInterfaceRule_description;
		this.groups.addAll(GroupUtil.allJavaVersionSince(GroupEnum.JAVA_1_8));
	}
}
