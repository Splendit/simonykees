package at.splendit.simonykees.core.rule;

import at.splendit.simonykees.core.visitor.CollectionRemoveAllASTVisitor;

/**
 * @see CollectionRemoveAllRule
 * 
 * @author Martin Huter
 * @since 0.9.2
 *
 */
public class CollectionRemoveAllRule extends RefactoringRule<CollectionRemoveAllASTVisitor> {

	public CollectionRemoveAllRule(Class<CollectionRemoveAllASTVisitor> visitor) {
		super(visitor);
		this.name = "TODO NAME";
		this.description = "TODO DESCRIPTION";
	}

}
