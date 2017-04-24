package at.splendit.simonykees.core.rule.impl;

import at.splendit.simonykees.core.rule.GroupEnum;
import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.util.GroupUtil;
import at.splendit.simonykees.core.visitor.StringConcatToPlusASTVisitor;
import at.splendit.simonykees.i18n.Messages;

/**
 * @see StringConcatToPlusASTVisitor
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
public class StringConcatToPlusRule extends RefactoringRule<StringConcatToPlusASTVisitor> {

	public StringConcatToPlusRule(Class<StringConcatToPlusASTVisitor> visitor) {
		super(visitor);
		this.name = Messages.StringConcatToPlusRule_name;
		this.description = Messages.StringConcatToPlusRule_description;
		this.groups.addAll(GroupUtil.allJavaVersionSince(GroupEnum.JAVA_1_1));
	}
}
