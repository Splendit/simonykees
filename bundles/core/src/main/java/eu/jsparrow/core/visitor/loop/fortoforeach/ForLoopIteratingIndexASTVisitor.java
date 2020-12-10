package eu.jsparrow.core.visitor.loop.fortoforeach;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import eu.jsparrow.core.visitor.loop.LoopIteratingIndexVisitor;
import eu.jsparrow.rules.common.util.ASTNodeUtil;

/**
 * A visitor for investigating the replace precondition of a for loop with an
 * enhanced for loop.
 * 
 * @author Ardit Ymeri
 * @since 1.2
 */
abstract class ForLoopIteratingIndexASTVisitor extends LoopIteratingIndexVisitor {

	protected static final String OUTSIDE_LOOP_INDEX_DECLARATION = "outside-declaration-fragment"; //$NON-NLS-1$
	protected static final String LOOP_INITIALIZER = "loop-initializer"; //$NON-NLS-1$
	protected static final String LOOP_INITIALIZER_INCORRECT_EXPRESSION = "incorrect-expression"; //$NON-NLS-1$
	protected static final String LOOP_UPDATER = "loop-updater"; //$NON-NLS-1$
	protected static final String INTERNAL_INDEX_UPDATER = "internal-index-updater"; //$NON-NLS-1$

	private Block parentBlock;
	private SimpleName iteratingIndexName;

	private ForStatement forStatement;
	private Map<String, ASTNode> indexInitializer;
	private Map<String, ASTNode> indexUpdater;
	private boolean multipleLoopInits = false;
	private boolean multipleLoopUpdaters = false;
	private boolean prequisite = false;
	private boolean indexDeclaredInInitializer = false;

	protected ForLoopIteratingIndexASTVisitor(SimpleName iteratingIndexName, SimpleName iterableName,
			ForStatement forStatement, Block scopeBlock) {
		super(iterableName);
		this.forStatement = forStatement;
		this.indexInitializer = new HashMap<>();
		this.indexUpdater = new HashMap<>();
		this.parentBlock = scopeBlock;
		this.iteratingIndexName = iteratingIndexName;

		// checking loop initializer
		List<Expression> initializers = ASTNodeUtil.returnTypedList(forStatement.initializers(), Expression.class);
		if (initializers.size() == 1) {
			Expression initializer = initializers.get(0);
			if (isVariableDeclarationExpression(iteratingIndexName, initializer)) {
				indexInitializer.put(LOOP_INITIALIZER, initializer);
				indexDeclaredInInitializer = true;
			} else if (isAssignmentToZero(iteratingIndexName, initializer)) {
				indexInitializer.put(LOOP_INITIALIZER, initializer);
			} else {
				indexInitializer.put(LOOP_INITIALIZER_INCORRECT_EXPRESSION, initializer);
			}
		} else if (!initializers.isEmpty()) {
			multipleLoopInits = true;
		}

		// checking loop updater
		List<Expression> updaters = ASTNodeUtil.returnTypedList(forStatement.updaters(), Expression.class);
		if (updaters.size() == 1) {
			Expression updater = updaters.get(0);
			if (isValidIncrementExpression(updater, iteratingIndexName)) {
				indexUpdater.put(LOOP_UPDATER, updater);
			} else {
				indexUpdater.put(LOOP_INITIALIZER_INCORRECT_EXPRESSION, updater);
			}
		} else if (updaters.size() > 1) {
			multipleLoopUpdaters = true;
		}

		// checking loop updater inside the body
		Statement loopBody = forStatement.getBody();
		if (loopBody.getNodeType() == ASTNode.BLOCK) {
			List<Statement> statements = ASTNodeUtil.returnTypedList(((Block) loopBody).statements(), Statement.class);
			if (!statements.isEmpty()) {
				Statement lastStatement = statements.get(statements.size() - 1);
				if (lastStatement.getNodeType() == ASTNode.EXPRESSION_STATEMENT) {
					Expression expression = ((ExpressionStatement) lastStatement).getExpression();
					if (isValidIncrementExpression(expression, iteratingIndexName)) {
						indexUpdater.put(INTERNAL_INDEX_UPDATER, lastStatement);
						markAsToBeRemoved(lastStatement);
					}
				}
			}
		}
	}

	/**
	 * Checks whether the given expression is a variable declaration expression
	 * of a variable with the same name as the given simple name.
	 * 
	 * @param name
	 *            simple name to check for
	 * @param initializer
	 *            expression to investigate
	 * 
	 * @return {@code true} if the expression is a variable declaration.
	 */
	private boolean isVariableDeclarationExpression(SimpleName name, Expression initializer) {
		boolean isSingleVarDecl = false;
		if (ASTNode.VARIABLE_DECLARATION_EXPRESSION == initializer.getNodeType()) {
			VariableDeclarationExpression varDeclExp = (VariableDeclarationExpression) initializer;
			List<VariableDeclarationFragment> fragments = ASTNodeUtil.convertToTypedList(varDeclExp.fragments(),
					VariableDeclarationFragment.class);
			if (fragments.size() == 1) {
				VariableDeclarationFragment fragment = fragments.get(0);
				if (fragment.getName()
					.getIdentifier()
					.equals(name.getIdentifier())) {
					isSingleVarDecl = true;
				}
			}

		}
		return isSingleVarDecl;
	}

	@Override
	public boolean preVisit2(ASTNode node) {
		this.prequisite = !indexInitializer.containsKey(LOOP_INITIALIZER_INCORRECT_EXPRESSION)
				&& !indexUpdater.containsKey(LOOP_INITIALIZER_INCORRECT_EXPRESSION)
				&& this.parentBlock == ASTNodeUtil.getSpecificAncestor(forStatement, Block.class) && !multipleLoopInits
				&& !multipleLoopUpdaters;
		return prequisite;
	}

	@Override
	public boolean visit(ForStatement node) {
		if (node == forStatement) {
			insideLoop = true;
			beforeLoop = false;
		}
		return true;
	}

	@Override
	public void endVisit(ForStatement node) {
		if (node == forStatement) {
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
		if (block != parentBlock && isBeforeLoop() && getIndexInitializer(OUTSIDE_LOOP_INDEX_DECLARATION) == null) {
			visitBlock = false;
		}

		return visitBlock;
	}

	@Override
	protected boolean isNameOfIteratingIndex(SimpleName simpleName) {
		boolean doVisit = false;
		if (!indexDeclaredInInitializer || isInsideLoop()) {
			IBinding resolvedBinding = simpleName.resolveBinding();
			if (resolvedBinding != null && IBinding.VARIABLE == resolvedBinding.getKind() && simpleName.getIdentifier()
				.equals(iteratingIndexName.getIdentifier())) {

				doVisit = true;
			}
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
		boolean initInLoop = false;

		ASTNode outIndexInit = null;
		ASTNode loopInitializer = null;
		if (indexInitializer.containsKey(OUTSIDE_LOOP_INDEX_DECLARATION)) {
			outIndexInit = indexInitializer.get(OUTSIDE_LOOP_INDEX_DECLARATION);
			initOutsideLoop = isInitializationToZero(outIndexInit);
		}

		if (indexInitializer.containsKey(LOOP_INITIALIZER)) {
			loopInitializer = indexInitializer.get(LOOP_INITIALIZER);
			initInLoop = isInitializationToZero(loopInitializer);
		}

		return initOutsideLoop || initInLoop;
	}

	private boolean isIndexIncremented() {
		return (indexUpdater.containsKey(LOOP_UPDATER) && !indexUpdater.containsKey(INTERNAL_INDEX_UPDATER))
				|| (!indexUpdater.containsKey(LOOP_UPDATER) && indexUpdater.containsKey(INTERNAL_INDEX_UPDATER));
	}

	@Override
	public boolean checkTransformPrecondition() {
		return super.checkTransformPrecondition() && prequisite && isIndexInitToZero() && isIndexIncremented();
	}

	public VariableDeclarationFragment getOutsideIndexDeclaration() {
		return (VariableDeclarationFragment) indexInitializer.get(OUTSIDE_LOOP_INDEX_DECLARATION);
	}

	public ExpressionStatement getInternalIndexUpdater() {
		return (ExpressionStatement) indexUpdater.get(INTERNAL_INDEX_UPDATER);
	}

	protected void putIndexInitializer(String key, ASTNode node) {
		indexInitializer.put(key, node);
	}

	protected ForStatement getForStatement() {
		return this.forStatement;
	}

	protected ASTNode getIndexUpdater(String key) {
		return indexUpdater.get(key);
	}

	protected ASTNode getIndexInitializer(String key) {
		return indexInitializer.get(key);
	}

	/**
	 * Checks whether the given simpleName is occurring in the properties of a
	 * for loop (i.e. loop initializer, loop expression or loop updater).
	 * 
	 * @param simpleName
	 *            simpleName to be checked.
	 * @return {@code true} if the simpleName is occurring in the for loop
	 *         properties.
	 */
	@Override
	protected boolean isLoopProperty(SimpleName simpleName) {
		ASTNode parent = simpleName.getParent();
		ASTNode grandParent = parent.getParent();
		ASTNode ggParent = grandParent.getParent();

		/*
		 * check if it is a direct increment (i++;++i;i+=1) or left hand side of
		 * the expression
		 */
		if (((parent.getLocationInParent() == ForStatement.UPDATERS_PROPERTY
				|| parent.getLocationInParent() == ForStatement.INITIALIZERS_PROPERTY
				|| parent.getLocationInParent() == ForStatement.EXPRESSION_PROPERTY) && grandParent == forStatement)
				|| grandParent == getIndexUpdater(INTERNAL_INDEX_UPDATER)) {
			return true;
		}

		/*
		 * check if it is a increment with another layer (i=i+1)
		 */
		if (((grandParent.getLocationInParent() == ForStatement.UPDATERS_PROPERTY
				|| grandParent.getLocationInParent() == ForStatement.INITIALIZERS_PROPERTY) && ggParent == forStatement)
				|| ggParent == getIndexUpdater(INTERNAL_INDEX_UPDATER)) {
			return true;
		}

		return false;
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
			putIndexInitializer(OUTSIDE_LOOP_INDEX_DECLARATION, declarationFragment);
			markAsToBeRemoved(declarationFragment);

		} else {
			setIndexReferencedOutsideLoop();
		}
	}
}
