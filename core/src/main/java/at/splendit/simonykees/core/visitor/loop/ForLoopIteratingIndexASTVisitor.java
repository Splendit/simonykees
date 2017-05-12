package at.splendit.simonykees.core.visitor.loop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import at.splendit.simonykees.core.util.ASTNodeUtil;

/**
 * A visitor for investigating the replace precondition of a for loop with an
 * enhanced for loop.
 * 
 * @author Ardit Ymeri
 * @since 1.2
 */
abstract class ForLoopIteratingIndexASTVisitor extends ASTVisitor {

	protected static final String OUTSIDE_LOOP_INDEX_DECLARATION = "outside-declaration-fragment"; //$NON-NLS-1$
	protected static final String LOOP_INITIALIZER = "loop-initializer"; //$NON-NLS-1$
	protected static final String LOOP_UPDATER = "loop-updater"; //$NON-NLS-1$
	protected static final String INTERNAL_INDEX_UPDATER = "internal-index-updater"; //$NON-NLS-1$

	private static final String ONE = "1"; //$NON-NLS-1$
	private static final String ZERO = "0"; //$NON-NLS-1$

	private ForStatement forStatement;
	private Map<String, ASTNode> indexInitializer;
	private Map<String, ASTNode> indexUpdater;
	private boolean indexReferencedInsideLoop = false;
	private boolean multipleLoopInits = false;
	private boolean multipleLoopUpdaters = false;
	private boolean hasEmptyStatement = false;
	private boolean indexReferencedOutsideLoop = false;
	private List<ASTNode> iteratingObjectInitializers;
	private List<ASTNode> nodesToBeRemoved;
	private boolean insideLoop = false;
	private boolean beforeLoop = true;
	private boolean afterLoop = false;
	private Block parentBlock;

	public ForLoopIteratingIndexASTVisitor(SimpleName iteratingIndexName,
			ForStatement forStatement, Block scopeBlock) {

		this.forStatement = forStatement;
		this.iteratingObjectInitializers = new ArrayList<>();
		this.indexInitializer = new HashMap<>();
		this.indexUpdater = new HashMap<>();
		this.nodesToBeRemoved = new ArrayList<>();
		this.parentBlock = scopeBlock;

		List<Expression> initializers = ASTNodeUtil.returnTypedList(forStatement.initializers(), Expression.class);
		if (initializers.size() == 1) {
			indexInitializer.put(LOOP_INITIALIZER, initializers.get(0));
		} else if (initializers.size() > 0) {
			multipleLoopInits = true;
		}

		List<Expression> updaters = ASTNodeUtil.returnTypedList(forStatement.updaters(), Expression.class);
		if (updaters.size() == 1 ) {
			Expression updater = updaters.get(0);
			if(isValidIncrementExpression(updater, iteratingIndexName)) {				
				indexUpdater.put(LOOP_UPDATER, updater);
			}
		} else if (updaters.size() > 1) {
			multipleLoopUpdaters = true;
		}

		Statement loopBody = forStatement.getBody();
		if(loopBody.getNodeType() == ASTNode.BLOCK) {
			List<Statement> statements = ASTNodeUtil.returnTypedList(((Block) loopBody).statements(),
					Statement.class);
			if (!statements.isEmpty()) {
				Statement lastStatement = statements.get(statements.size() - 1);
				if(lastStatement.getNodeType() == ASTNode.EXPRESSION_STATEMENT) {
					Expression expression = ((ExpressionStatement)lastStatement).getExpression();
					if (isValidIncrementExpression(expression, iteratingIndexName)) {
						indexUpdater.put(INTERNAL_INDEX_UPDATER, lastStatement);
						nodesToBeRemoved.add(lastStatement);
					}
				}
			}
		}
	}
	
	public abstract SimpleName getIteratorName();
	
	public abstract VariableDeclarationFragment getPreferredNameFragment();

	@Override
	public boolean preVisit2(ASTNode node) {
		return this.parentBlock == forStatement.getParent() && !multipleLoopInits && !multipleLoopUpdaters;
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
	public boolean visit(AnonymousClassDeclaration node) {
		// Anonymous classes have their own scope
		return false;
	}
	
	public boolean visit(Block block) {
		boolean visitBlock = true;
		/*
		 * inner blocks before declaration of the loop initializer
		 * have their own loop
		 */
		if(block != parentBlock && isBeforeLoop() && getIndexInitializer(OUTSIDE_LOOP_INDEX_DECLARATION) == null) {
			visitBlock = false;
		}
		
		return visitBlock;
	}
	
	/**
	 * Checks if the given expression is incrementing the given operand by 1.
	 * The following three cases are considered:
	 * <ul>
	 * <li>{@code operand++;}</li>
	 * <li>{@code ++operand;}</li>
	 * <li>{@code operand = operand + 1;}</li>
	 * <li>{@code operand += 1;}</li>
	 * </ul>
	 * 
	 * @param expressionNode
	 *            statement to be checked
	 * @param operandName
	 *            operand name
	 * @return if the statement is an increment statement.
	 */
	private boolean isValidIncrementExpression(Expression expression, SimpleName operandName) {
		boolean isIncrement = false;
		
		int expressionType = expression.getNodeType();

		if (ASTNode.POSTFIX_EXPRESSION == expressionType) {
			// covers the case: operand++;
			PostfixExpression postfixExpression = (PostfixExpression) expression;
			Expression operand = postfixExpression.getOperand();
			if (ASTNode.SIMPLE_NAME == operand.getNodeType()
					&& ((SimpleName) operand).getIdentifier().equals(operandName.getIdentifier())
					&& PostfixExpression.Operator.INCREMENT.equals(postfixExpression.getOperator())) {

				isIncrement = true;
			}
		} else if (ASTNode.ASSIGNMENT == expressionType) {
			Assignment assignmentExpression = (Assignment) expression;
			Expression lhs = assignmentExpression.getLeftHandSide();
			Expression rhs = assignmentExpression.getRightHandSide();

			if (ASTNode.SIMPLE_NAME == lhs.getNodeType()
					&& ((SimpleName) lhs).getIdentifier().equals(operandName.getIdentifier())) {
				if(ASTNode.INFIX_EXPRESSION == rhs.getNodeType()) {
					// covers the case: operand = operand +1;

					InfixExpression infixExpression = (InfixExpression) rhs;
					Expression leftOperand = infixExpression.getLeftOperand();
					Expression rightOperand = infixExpression.getRightOperand();
					/*
					 * the form of the expression should either be:
					 * 		operand = operand + 1;
					 * or
					 * 		operand = 1 + operand; 
					 * 
					 */
					if (InfixExpression.Operator.PLUS.equals(infixExpression.getOperator()) && ((ASTNode.SIMPLE_NAME == leftOperand.getNodeType()
							&& ((SimpleName) leftOperand).getIdentifier().equals(operandName.getIdentifier())
							&& ASTNode.NUMBER_LITERAL == rightOperand.getNodeType()
							&& ONE.equals(((NumberLiteral) rightOperand).getToken()))
							|| ((ASTNode.SIMPLE_NAME == rightOperand.getNodeType()
									&& ((SimpleName) rightOperand).getIdentifier().equals(operandName.getIdentifier())
									&& ASTNode.NUMBER_LITERAL == leftOperand.getNodeType()
									&& ONE.equals(((NumberLiteral) leftOperand).getToken()))))) {

						isIncrement = true;
					}
				} else if (ASTNode.NUMBER_LITERAL == rhs.getNodeType()) {
					// covers the case: operand += 1;
					String numLiteral = ((NumberLiteral)rhs).getToken();
					if(ONE.equals(numLiteral) && Assignment.Operator.PLUS_ASSIGN.equals(assignmentExpression.getOperator())) {
						isIncrement = true;
					}
					
				}
			}

		} else if (ASTNode.PREFIX_EXPRESSION == expressionType) {
			// covers the case: ++operand;
			PrefixExpression postfixExpression = (PrefixExpression) expression;
			Expression operand = postfixExpression.getOperand();
			if (ASTNode.SIMPLE_NAME == operand.getNodeType()
					&& ((SimpleName) operand).getIdentifier().equals(operandName.getIdentifier())
					&& PrefixExpression.Operator.INCREMENT.equals(postfixExpression.getOperator())) {
				isIncrement = true;
			}
		}

		return isIncrement;
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
			initOutsideLoop = isInitializedToZero(outIndexInit);
		}

		if (indexInitializer.containsKey(LOOP_INITIALIZER)) {
			loopInitializer = indexInitializer.get(LOOP_INITIALIZER);
			initInLoop = isInitializedToZero(loopInitializer);
		}

		return initOutsideLoop || initInLoop;
	}

	private boolean isIndexIncremented() {
		return (indexUpdater.containsKey(LOOP_UPDATER) && !indexUpdater.containsKey(INTERNAL_INDEX_UPDATER))
				|| (!indexUpdater.containsKey(LOOP_UPDATER) && indexUpdater.containsKey(INTERNAL_INDEX_UPDATER));
	}

	/**
	 * Checks whether the given expression is an initialization to zero
	 * expression.
	 */
	private boolean isInitializedToZero(ASTNode initExpresion) {
		boolean assignedToZero = false;
		if (ASTNode.ASSIGNMENT == initExpresion.getNodeType()) {
			Assignment assignment = (Assignment) initExpresion;
			Expression rhs = assignment.getRightHandSide();
			if (ASTNode.NUMBER_LITERAL == rhs.getNodeType()) {
				assignedToZero = ((NumberLiteral) rhs).getToken().equals(ZERO);
			}
		} else if (ASTNode.VARIABLE_DECLARATION_EXPRESSION == initExpresion.getNodeType()) {
			VariableDeclarationExpression declExpresion = (VariableDeclarationExpression) initExpresion;
			List<VariableDeclarationFragment> fragments = ASTNodeUtil.returnTypedList(declExpresion.fragments(),
					VariableDeclarationFragment.class);
			if (fragments.size() == 1) {
				VariableDeclarationFragment fragment = fragments.get(0);
				Expression initializer = fragment.getInitializer();
				if (initializer != null && ASTNode.NUMBER_LITERAL == initializer.getNodeType()) {
					assignedToZero = ((NumberLiteral) initializer).getToken().equals(ZERO);
				}
			}
		} else if (ASTNode.VARIABLE_DECLARATION_FRAGMENT == initExpresion.getNodeType()) {
			VariableDeclarationFragment declFragment = (VariableDeclarationFragment) initExpresion;
			Expression initializer = declFragment.getInitializer();
			if (initializer != null && ASTNode.NUMBER_LITERAL == initializer.getNodeType()) {
				assignedToZero = ((NumberLiteral) initializer).getToken().equals(ZERO);
			}
		}

		return assignedToZero;
	}

	public boolean checkTransformPrecondition() {
		return !hasEmptyStatement && !indexReferencedInsideLoop && !indexReferencedOutsideLoop && isIndexInitToZero()
				&& isIndexIncremented();
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
	
	protected ForStatement getForStatment() {
		return this.forStatement;
	}
	
	protected ASTNode getIndexUpdater(String key) {
		return indexUpdater.get(key);
	}
	
	protected void setHasEmptyStatement() {
		this.hasEmptyStatement = true;
	}
	
	protected ASTNode getIndexInitializer(String key) {
		return indexInitializer.get(key);
	}
	
	protected void setIndexReferencedInsideLoop() {
		this.indexReferencedInsideLoop = true;
		
	}

	public List<ASTNode> getNodesToBeRemoved() {
		return this.nodesToBeRemoved;
	}

	public List<ASTNode> getIteratingObjectInitializers() {
		return iteratingObjectInitializers;
	}
	
	protected void addIteratingObjectInitializer(ASTNode node) {
		iteratingObjectInitializers.add(node);
	}
	
	protected void markAsToBeRemoved(ASTNode node) {
		nodesToBeRemoved.add(node);
	}
	
	protected boolean isBeforeLoop() {
		return beforeLoop;
	}
	
	protected boolean isInsideLoop() {
		return insideLoop;
	}
	
	protected boolean isAfterLoop() {
		return afterLoop;
	}
	
	protected void setIndexReferencedOutsideLoop() {
		this.indexReferencedOutsideLoop = true;
	}
	
	protected Block getForStatementParent() {
		return this.parentBlock;
	}

	/**
	 * Checks whether the given simpleName is occurring in the 
	 * properties of a for loop (i.e. loop initializer, loop expression
	 * or loop updater).
	 *  
	 * @param simpleName simpleName to be checked.
	 * @return {@code true} if the simpleName is occurring 
	 * in the for loop properties. 
	 */
	protected boolean isLoopProperty(SimpleName simpleName) {
		ASTNode parent = simpleName.getParent();
		ASTNode grandParent = parent.getParent();
		
		return parent.getLocationInParent() == ForStatement.UPDATERS_PROPERTY
				|| grandParent.getLocationInParent() == ForStatement.UPDATERS_PROPERTY
				|| parent.getLocationInParent() == ForStatement.INITIALIZERS_PROPERTY
				|| grandParent.getLocationInParent() == ForStatement.INITIALIZERS_PROPERTY
				|| parent.getLocationInParent() == ForStatement.EXPRESSION_PROPERTY
				|| grandParent == getIndexUpdater(INTERNAL_INDEX_UPDATER)
				|| grandParent.getParent() == getIndexUpdater(INTERNAL_INDEX_UPDATER);
	}

	/**
	 * Checks whether the given simpleName is the name property
	 * of a {@link VariableDeclarationFragment}. Otherwise, a flag
	 * is stored for indicating that the simpleName is referenced
	 * outside the loop.
	 *  
	 * @param simpleName
	 */
	protected void analyseBeforeLoopOccurrence(SimpleName simpleName) {
		
		if (VariableDeclarationFragment.NAME_PROPERTY == simpleName.getLocationInParent()) {
			VariableDeclarationFragment declarationFragment = (VariableDeclarationFragment) simpleName.getParent();
			putIndexInitializer(OUTSIDE_LOOP_INDEX_DECLARATION, declarationFragment);
			markAsToBeRemoved(declarationFragment);
	
		} else {
			setIndexReferencedOutsideLoop();
		}
	}
}

