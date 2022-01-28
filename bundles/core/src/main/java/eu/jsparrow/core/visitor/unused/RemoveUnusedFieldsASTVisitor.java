package eu.jsparrow.core.visitor.unused;

import java.util.List;

import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

public class RemoveUnusedFieldsASTVisitor extends AbstractASTRewriteASTVisitor {
	
	private List<UnusedFieldWrapper> unusedFields;
	/*
	 * Gets a list of unused field wrappers.
	 * Finds the relevant ones for the given compilation unit. 
	 * Removes them. 
	 */
	
	public RemoveUnusedFieldsASTVisitor(List<UnusedFieldWrapper> unusedFields) {
		this.unusedFields = unusedFields;
	}

}
