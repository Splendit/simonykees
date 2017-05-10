package at.splendit.simonykees.core.rule;

import at.splendit.simonykees.core.visitor.AbstractASTRewriteASTVisitor;

/**
 * 
 * @author Ardit Ymeri
 * @since 1.2
 *
 * @param <T>
 */
public abstract class SemiAutomaticRefactoringRule<T extends AbstractASTRewriteASTVisitor> extends RefactoringRule<T> {

	public SemiAutomaticRefactoringRule(Class<T> visitor) {
		super(visitor);
	}

}
