package at.splendit.simonykees.core.rule;

import at.splendit.simonykees.core.i18n.Messages;
import at.splendit.simonykees.core.visitor.RemoveNewStringConstructorASTVisitor;

/**
 * @see RemoveNewStringConstructorASTVisitor
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
public class RemoveNewStringConstructorRule extends RefactoringRule<RemoveNewStringConstructorASTVisitor> {

	public RemoveNewStringConstructorRule(Class<RemoveNewStringConstructorASTVisitor> visitor) {
		super(visitor);
		this.name = Messages.RemoveNewStringConstructorRule_name;
		this.description = Messages.RemoveNewStringConstructorRule_description;
	}

}
