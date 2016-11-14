package at.splendit.simonykees.core.rule;

import at.splendit.simonykees.core.i18n.Messages;
import at.splendit.simonykees.core.visitor.WhileToForASTVisitor;

/**
 * @see WhileToForRule
 * 
 * @author Martin Huter
 * @since 0.9.2
 *
 */
public class WhileToForRule extends RefactoringRule<WhileToForASTVisitor> {

	public WhileToForRule(Class<WhileToForASTVisitor> visitor) {
		super(visitor);
		this.name = Messages.WhileToForRule_name;
		this.description = Messages.WhileToForRule_description;
	}

}
