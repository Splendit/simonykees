package at.splendit.simonykees.core.rule.impl;

import at.splendit.simonykees.core.rule.GroupEnum;
import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.util.GroupUtil;
import at.splendit.simonykees.core.visitor.tryStatement.MultiCatchASTVisitor;
import at.splendit.simonykees.i18n.Messages;
/** 
 * @see MultiCatchASTVisitor
 * 
 * @author Martin Huter
 * @since 0.9
 *
 */
public class MultiCatchRule extends RefactoringRule<MultiCatchASTVisitor> {

	public MultiCatchRule(Class<MultiCatchASTVisitor> visitor) {
		super(visitor);
		this.name = Messages.MultiCatchRule_name;
		this.description = Messages.MultiCatchRule_description;
		this.groups.addAll(GroupUtil.allJavaVersionSince(GroupEnum.JAVA_1_7));
	}
}
