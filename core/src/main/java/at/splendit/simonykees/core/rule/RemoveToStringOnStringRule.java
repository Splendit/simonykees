package at.splendit.simonykees.core.rule;

import at.splendit.simonykees.core.i18n.Messages;
import at.splendit.simonykees.core.visitor.RemoveToStringOnStringASTVisitor;

/**
 * @see RemoveToStringOnStringASTVisitor
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
public class RemoveToStringOnStringRule extends RefactoringRule<RemoveToStringOnStringASTVisitor> {

	public RemoveToStringOnStringRule(Class<RemoveToStringOnStringASTVisitor> visitor) {
		super(visitor);
		this.name = Messages.RemoveToStringOnStringRule_name;
		this.description = Messages.RemoveToStringOnStringRule_description;
	}

}
