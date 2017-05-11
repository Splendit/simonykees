package at.splendit.simonykees.core.visitor.loop;

import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

/**
 * 
 * @author Ardit Ymeri
 * @since 1.2
 * 
 */
class ForLoopOverArraysASTVisitor extends ForLoopIteratingIndexASTVisitor {

	private static final String LENGTH = "length";
	
	private SimpleName iteratingIndexName;
	private SimpleName iterableName;
	private ForStatement forStatement;
	private SimpleName newIteratorName;
	private VariableDeclarationFragment preferredNameFragment;
	
	public ForLoopOverArraysASTVisitor(SimpleName iteratingIndexName, SimpleName iterableName,
			ForStatement forStatement) {
		super(iteratingIndexName, forStatement);
		this.iteratingIndexName = iteratingIndexName;
		this.iterableName = iterableName;
		this.forStatement = super.getForStatment();
		
	}
	
	@Override
	public boolean visit(SimpleName simpleName) {
		IBinding resolvedBinding = simpleName.resolveBinding();
		if (resolvedBinding != null && IBinding.VARIABLE == resolvedBinding.getKind()
				&& simpleName.getIdentifier().equals(iteratingIndexName.getIdentifier())) {

			ASTNode parent = simpleName.getParent();
			if (isBeforeLoop()) {
				//TODO: room for improvement
				if (ASTNode.VARIABLE_DECLARATION_FRAGMENT == parent.getNodeType()) {
					VariableDeclarationFragment declarationFragment = (VariableDeclarationFragment) parent;
					putIndexInitializer(OUTSIDE_LOOP_INDEX_DECLARATION, declarationFragment);
					markAsToBeRemoved(declarationFragment);

				} else {
					setIndexReferencedOutsideLoop();
				}
			} else if (isInsideLoop()
					&& (parent != getIndexInitializer(LOOP_INITIALIZER)
							|| parent.getParent() != getIndexInitializer(LOOP_INITIALIZER))
					&& parent != forStatement.getExpression() && (parent != getIndexInitializer(LOOP_UPDATER)
							|| parent.getParent() != getIndexInitializer(LOOP_UPDATER))) {

				if (ASTNode.ARRAY_ACCESS == parent.getNodeType()) {
					ArrayAccess arrayAccess = (ArrayAccess) parent;
					Expression arrayExpression = arrayAccess.getArray();

					ASTMatcher matcher = new ASTMatcher();

					if(ASTNode.SIMPLE_NAME == arrayExpression.getNodeType() 
					&& ((SimpleName)arrayExpression).getIdentifier().equals(iterableName.getIdentifier())) {
						addIteratingObjectInitializer(arrayAccess);
						
						if (newIteratorName == null
								&& VariableDeclarationFragment.INITIALIZER_PROPERTY == arrayAccess
										.getLocationInParent()) {
							VariableDeclarationFragment fragment = (VariableDeclarationFragment) arrayAccess
									.getParent();
							this.newIteratorName = fragment.getName();
							this.preferredNameFragment = fragment;
						}
					} else {
						setIndexReferencedInsideLoop();
					}
				} else if (parent.getLocationInParent() != ForStatement.UPDATERS_PROPERTY
						&& parent.getParent().getLocationInParent() != ForStatement.UPDATERS_PROPERTY
						&& parent.getLocationInParent() != ForStatement.INITIALIZERS_PROPERTY
						&& parent.getParent().getLocationInParent() != ForStatement.INITIALIZERS_PROPERTY
						&& parent.getParent() != getIndexUpdater(INTERNAL_INDEX_UPDATER)
						&& parent.getParent().getParent() != getIndexUpdater(INTERNAL_INDEX_UPDATER)) {

					setIndexReferencedInsideLoop();
				}
			} else if (isAfterLoop()) {
				setIndexReferencedOutsideLoop();
			}
		}

		return true;
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
