package at.splendit.simonykees.core.visitor.loop;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

/**
 * A visitor for checking the precondition of replacing a for loop 
 * iterating over a {@link List} with an {@link EnhancedForStatement}.
 * 
 * @author Ardit Ymeri
 * @since 1.2
 * 
 */
class ForLoopOverArraysASTVisitor extends ForLoopIteratingIndexASTVisitor {
	
	private SimpleName iteratingIndexName;
	private SimpleName iterableName;
	private SimpleName newIteratorName;
	private VariableDeclarationFragment preferredNameFragment;
	
	public ForLoopOverArraysASTVisitor(SimpleName iteratingIndexName, SimpleName iterableName,
			ForStatement forStatement, Block scopeBloc) {
		super(iteratingIndexName, forStatement, scopeBloc);
		this.iteratingIndexName = iteratingIndexName;
		this.iterableName = iterableName;
	}
	
	@Override
	public boolean visit(SimpleName simpleName) {
		IBinding resolvedBinding = simpleName.resolveBinding();
		if (resolvedBinding != null && IBinding.VARIABLE == resolvedBinding.getKind()
				&& simpleName.getIdentifier().equals(iteratingIndexName.getIdentifier())) {

			if (isBeforeLoop()) {
				analyseBeforeLoopOccurrence(simpleName);
			} else if (isInsideLoop() && !isLoopProperty(simpleName)) {

				if (isReplaceableArrayAccess(simpleName)) {
					ArrayAccess arrayAccess = (ArrayAccess) simpleName.getParent();
					addIteratingObjectInitializer(arrayAccess);

					// store the preferred name for the iterator
					if (newIteratorName == null
							&& VariableDeclarationFragment.INITIALIZER_PROPERTY == arrayAccess.getLocationInParent()) {
						VariableDeclarationFragment fragment = (VariableDeclarationFragment) arrayAccess.getParent();
						this.newIteratorName = fragment.getName();
						this.preferredNameFragment = fragment;
					}

				} else {
					setIndexReferencedInsideLoop();
				}
			} else if (isAfterLoop()) {
				// the iterating index is referenced after the loop
				setIndexReferencedOutsideLoop();
			}
		}

		return true;
	}

	/**
	 * Checks whether the given instance of a {@link SimpleName} is used
	 * as an index for accessing an element of the {@link #iterableName}. 
	 * 
	 * @param simpleName simple name to be checked.
	 * @return {@code true} if the parent of the simpleName is an  {@link ArrayAccess} which 
	 * matches with  {@link #iterableName}.
	 */
	private boolean isReplaceableArrayAccess(SimpleName simpleName) {
		ASTNode node = simpleName.getParent();
		boolean replacableAccess = false;
		if(ASTNode.ARRAY_ACCESS == node.getNodeType() && node.getLocationInParent() != Assignment.LEFT_HAND_SIDE_PROPERTY) {
			ArrayAccess arrayAccess = (ArrayAccess) node;
			Expression arrayExpression = arrayAccess.getArray();
			if(ASTNode.SIMPLE_NAME == arrayExpression.getNodeType()) {
				SimpleName arrayName = (SimpleName)arrayExpression;
				replacableAccess = arrayName.getIdentifier().equals(iterableName.getIdentifier());
			}
		}
		return replacableAccess;
	}

	@Override
	public SimpleName getIteratorName() {
		return this.newIteratorName;
	}

	@Override
	public VariableDeclarationFragment getPreferredNameFragment() {
		return this.preferredNameFragment;
	}
	
}
