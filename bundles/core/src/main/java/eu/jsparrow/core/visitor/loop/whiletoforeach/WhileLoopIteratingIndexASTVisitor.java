package eu.jsparrow.core.visitor.loop.whiletoforeach;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.WhileStatement;

import eu.jsparrow.core.visitor.loop.LoopIteratingIndexASTVisitor;
import eu.jsparrow.rules.common.util.ASTNodeUtil;

/**
 * A visitor for investigating the replace precondition of a while loop with an
 * enhanced for loop.
 * 
 * @author Ardit Ymeri
 * @since 1.2
 *
 */
abstract class WhileLoopIteratingIndexASTVisitor extends LoopIteratingIndexASTVisitor {

	protected static final String INDEX_DECLARATION = "index-declaration-fragment"; //$NON-NLS-1$
	protected static final String INDEX_UPDATER = "index-updater"; //$NON-NLS-1$

	private Block parentBlock;
	private SimpleName iteratingIndexName;

	private WhileStatement whileStatement;
	private VariableDeclarationFragment indexDeclaration;
	private ExpressionStatement indexUpdater;

	private boolean prequisite = false;

	protected WhileLoopIteratingIndexASTVisitor(SimpleName iteratingIndexName, SimpleName iterableNode,
			WhileStatement whileStatement, Block parentBlock) {
		super(iterableNode);
		this.whileStatement = whileStatement;
		this.iteratingIndexName = iteratingIndexName;
		this.parentBlock = parentBlock;

		// checking loop updater inside the body
		Statement loopBody = whileStatement.getBody();
		if (loopBody.getNodeType() == ASTNode.BLOCK) {
			List<Statement> statements = ASTNodeUtil.returnTypedList(((Block) loopBody).statements(), Statement.class);
			if (!statements.isEmpty()) {
				Statement lastStatement = statements.get(statements.size() - 1);
				if (lastStatement.getNodeType() == ASTNode.EXPRESSION_STATEMENT) {
					ExpressionStatement lastBodyExpressionStatement = (ExpressionStatement) lastStatement;
					Expression expression = ((ExpressionStatement) lastStatement).getExpression();
					if (isValidIncrementExpression(expression, iteratingIndexName)) {
						indexUpdater = lastBodyExpressionStatement;
						markAsToBeRemoved(lastStatement);
					}
				}
			}
		}
	}

	@Override
	public boolean preVisit2(ASTNode node) {
		this.prequisite = indexUpdater != null
				&& this.parentBlock == ASTNodeUtil.getSpecificAncestor(whileStatement, Block.class);
		return prequisite;
	}

	@Override
	public boolean visit(WhileStatement node) {
		if (node == whileStatement) {
			insideLoop = true;
			beforeLoop = false;
		}
		return true;
	}

	@Override
	public void endVisit(WhileStatement node) {
		if (node == whileStatement) {
			insideLoop = false;
			afterLoop = true;
		}
	}

	@Override
	public boolean visit(Block block) {
		boolean visitBlock = true;
		/*
		 * inner blocks before declaration of the loop initializer have their
		 * own scope
		 */
		if (block != parentBlock && isBeforeLoop() && indexDeclaration == null) {
			visitBlock = false;
		}

		return visitBlock;
	}

	@Override
	protected boolean isNameOfIteratingIndex(SimpleName simpleName) {
		boolean doVisit = false;

		IBinding resolvedBinding = simpleName.resolveBinding();
		if (resolvedBinding != null && IBinding.VARIABLE == resolvedBinding.getKind() && simpleName.getIdentifier()
			.equals(iteratingIndexName.getIdentifier())) {

			doVisit = true;
		}

		return doVisit;
	}

	/**
	 * Checks whether the loop index is initialized to zero.
	 * 
	 * @return true if the iterating index is assigned to zero.
	 */
	private boolean isIndexInitToZero() {
		boolean initOutsideLoop = false;

		if (indexDeclaration != null) {
			initOutsideLoop = isInitializationToZero(indexDeclaration);
		}

		return initOutsideLoop;
	}

	private boolean isIndexIncremented() {
		return isValidIncrementExpression(indexUpdater.getExpression(), iteratingIndexName);
	}

	@Override
	public boolean checkTransformPrecondition() {
		return super.checkTransformPrecondition() && prequisite && isIndexInitToZero() && isIndexIncremented();
	}

	/**
	 * Checks whether the given simpleName is the name property of a
	 * {@link VariableDeclarationFragment}. Otherwise, a flag is stored for
	 * indicating that the simpleName is referenced outside the loop.
	 * 
	 * @param simpleName
	 */
	@Override
	protected void analyzeBeforeLoopOccurrence(SimpleName simpleName) {

		if (VariableDeclarationFragment.NAME_PROPERTY == simpleName.getLocationInParent()) {
			VariableDeclarationFragment declarationFragment = (VariableDeclarationFragment) simpleName.getParent();
			this.indexDeclaration = declarationFragment;
			markAsToBeRemoved(declarationFragment);

		} else {
			setIndexReferencedOutsideLoop();
		}
	}

	/**
	 * Checks whether the given simpleName is occurring in the properties of a
	 * while loop (i.e. loop expression).
	 * 
	 * @param simpleName
	 *            simpleName to be checked.
	 * @return {@code true} if the simpleName is occurring in the while loop
	 *         properties.
	 */
	@Override
	protected boolean isLoopProperty(SimpleName simpleName) {
		ASTNode parent = simpleName.getParent();
		ASTNode grandParent = parent.getParent();
		return (parent.getLocationInParent() == WhileStatement.EXPRESSION_PROPERTY && grandParent == whileStatement)
				|| parent == indexUpdater || parent.getParent() == indexUpdater || parent.getParent()
					.getParent() == indexUpdater
				|| parent == indexDeclaration || parent.getParent() == indexDeclaration;
	}
}
