package at.splendit.simonykees.core.rule;

import at.splendit.simonykees.core.i18n.Messages;
import at.splendit.simonykees.core.visitor.SerialVersionUidASTVisitor;

/**
 * @see SerialVersionUidRule
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
public class SerialVersionUidRule extends RefactoringRule<SerialVersionUidASTVisitor> {

	public SerialVersionUidRule(Class<SerialVersionUidASTVisitor> visitor) {
		super(visitor);
		this.name = Messages.CollectionRemoveAllRule_name;
		this.description = Messages.CollectionRemoveAllRule_description;
	}

}
