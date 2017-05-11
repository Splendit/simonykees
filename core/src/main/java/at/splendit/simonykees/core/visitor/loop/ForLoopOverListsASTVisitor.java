package at.splendit.simonykees.core.visitor.loop;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

/**
 * 
 * 
 * @author Ardit Ymeri
 * @since 1.2
 *
 */
class ForLoopOverListsASTVisitor extends ForLoopIteratingIndexASTVisitor {
	private static final String GET = "get"; //$NON-NLS-1$
	
	private SimpleName iteratingIndexName;
	private SimpleName iterableName;
	private ForStatement forStatement;
	private SimpleName newIteratorName;
	private VariableDeclarationFragment preferredNameFragment;
	
	public ForLoopOverListsASTVisitor(SimpleName iteratingIndexName, SimpleName iterableName,
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

				if (ASTNode.METHOD_INVOCATION == parent.getNodeType()) {
					MethodInvocation methodInvocation = (MethodInvocation) parent;
					Expression methodExpression = methodInvocation.getExpression();

					if (ASTNode.EXPRESSION_STATEMENT == methodInvocation.getParent().getNodeType()) {
						setHasEmptyStatement();
					} else if (GET.equals(methodInvocation.getName().getIdentifier())
							&& methodInvocation.arguments().size() == 1 && methodExpression != null
							&& methodExpression.getNodeType() == ASTNode.SIMPLE_NAME
							&& ((SimpleName) methodExpression).getIdentifier().equals(iterableName.getIdentifier())) {
						addIteratingObjectInitializer(methodInvocation);

						if (newIteratorName == null
								&& VariableDeclarationFragment.INITIALIZER_PROPERTY == methodInvocation
										.getLocationInParent()) {
							VariableDeclarationFragment fragment = (VariableDeclarationFragment) methodInvocation
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
		return preferredNameFragment;
	}
}
