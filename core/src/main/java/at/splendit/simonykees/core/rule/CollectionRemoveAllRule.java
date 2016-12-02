package at.splendit.simonykees.core.rule;

import at.splendit.simonykees.core.i18n.Messages;
import at.splendit.simonykees.core.visitor.CollectionRemoveAllASTVisitor;

/**
 * @see CollectionRemoveAllASTVisitor
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
public class CollectionRemoveAllRule extends RefactoringRule<CollectionRemoveAllASTVisitor> {

	public CollectionRemoveAllRule(Class<CollectionRemoveAllASTVisitor> visitor) {
		super(visitor);
		this.name = Messages.CollectionRemoveAllRule_name;
		this.description = Messages.CollectionRemoveAllRule_description;
	}

}
