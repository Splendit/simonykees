package at.splendit.simonykees.core.visitor.loop;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

/**
 * A visitor for checking the precondition of replacing a for loop iterating
 * over a {@link List} with an {@link EnhancedForStatement}.
 * 
 * @author Ardit Ymeri
 * @since 1.2
 *
 */
class ForLoopOverListsASTVisitor extends ForLoopIteratingIndexASTVisitor {
	private static final String GET = "get"; //$NON-NLS-1$

	private SimpleName iterableName;
	private SimpleName newIteratorName;
	private VariableDeclarationFragment preferredNameFragment;

	public ForLoopOverListsASTVisitor(SimpleName iteratingIndexName, SimpleName iterableName, ForStatement forStatement,
			Block scopeBlock) {
		super(iteratingIndexName, forStatement, scopeBlock);
		this.iterableName = iterableName;
	}

	@Override
	public boolean visit(SimpleName simpleName) {
		if (!isNameOfIteratingIndex(simpleName)) {
			return true;
		}

		ASTNode parent = simpleName.getParent();
		if (isBeforeLoop()) {
			analyseBeforeLoopOccurrence(simpleName);
		} else if (isInsideLoop() && !isLoopProperty(simpleName)) {

			if (ASTNode.METHOD_INVOCATION == parent.getNodeType()) {
				MethodInvocation methodInvocation = (MethodInvocation) parent;
				Expression methodExpression = methodInvocation.getExpression();

				if (ASTNode.EXPRESSION_STATEMENT == methodInvocation.getParent().getNodeType()) {
					/*
					 * replacing the expression statement with a variable name
					 * leads to compile error
					 */
					setHasEmptyStatement();
				} else if (GET.equals(methodInvocation.getName().getIdentifier())
						&& methodInvocation.arguments().size() == 1 && methodExpression != null
						&& methodExpression.getNodeType() == ASTNode.SIMPLE_NAME
						&& ((SimpleName) methodExpression).getIdentifier().equals(iterableName.getIdentifier())) {
					/*
					 * simpleName is the parameter of the get() method in the
					 * iterable object.
					 */
					addIteratingObjectInitializer(methodInvocation);

					// store the preferred iterator name
					if (newIteratorName == null && VariableDeclarationFragment.INITIALIZER_PROPERTY == methodInvocation
							.getLocationInParent()) {
						VariableDeclarationFragment fragment = (VariableDeclarationFragment) methodInvocation
								.getParent();
						this.newIteratorName = fragment.getName();
						this.preferredNameFragment = fragment;
					}

				} else {
					setIndexReferencedInsideLoop();
				}
			} else {
				setIndexReferencedInsideLoop();
			}
		} else if (isAfterLoop()) {
			setIndexReferencedOutsideLoop();
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
