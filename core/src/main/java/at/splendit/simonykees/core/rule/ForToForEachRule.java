package at.splendit.simonykees.core.rule;

import at.splendit.simonykees.core.i18n.Messages;
import at.splendit.simonykees.core.visitor.loop.ForToForEachASTVisitor;

/**
 * @see ForToForEachASTVisitor
 * 
 * @author Martin Huter
 * @since 0.9.2
 *
 */
public class ForToForEachRule extends RefactoringRule<ForToForEachASTVisitor> {

	public ForToForEachRule(Class<ForToForEachASTVisitor> visitor) {
		super(visitor);
		this.name = Messages.ForToForEachRule_name;
		this.description = Messages.ForToForEachRule_description;
	}

}
