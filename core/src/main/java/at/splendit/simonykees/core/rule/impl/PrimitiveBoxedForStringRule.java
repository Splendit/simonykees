package at.splendit.simonykees.core.rule.impl;

import at.splendit.simonykees.core.rule.GroupEnum;
import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.util.GroupUtil;
import at.splendit.simonykees.core.visitor.PrimitiveBoxedForStringASTVisitor;
import at.splendit.simonykees.i18n.Messages;

/**
 * @see PrimitiveBoxedForStringASTVisitor
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
public class PrimitiveBoxedForStringRule extends RefactoringRule<PrimitiveBoxedForStringASTVisitor> {

	public PrimitiveBoxedForStringRule(Class<PrimitiveBoxedForStringASTVisitor> visitor) {
		super(visitor);
		this.name = Messages.PrimitiveBoxedForStringRule_name;
		this.description = Messages.PrimitiveBoxedForStringRule_description;
		this.groups.addAll(GroupUtil.allJavaVersionSince(GroupEnum.JAVA_1_1));
	}
}
