package at.splendit.simonykees.core.rule.impl;

import at.splendit.simonykees.core.rule.GroupEnum;
import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.util.GroupUtil;
import at.splendit.simonykees.core.visitor.tryStatement.TryWithResourceASTVisitor;
import at.splendit.simonykees.i18n.Messages;
/** 
 * @see TryWithResourceASTVisitor
 * 
 * @author Martin Huter
 * @since 0.9
 *
 */
public class TryWithResourceRule extends RefactoringRule<TryWithResourceASTVisitor> {

	public TryWithResourceRule(Class<TryWithResourceASTVisitor> visitor) {
		super(visitor);
		this.name = Messages.TryWithResourceRule_name;
		this.description = Messages.TryWithResourceRule_description;
		this.groups.addAll(GroupUtil.allJavaVersionSince(GroupEnum.JAVA_1_7));
	}
}
