package at.splendit.simonykees.core.visitor.loop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
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
class ForLoopIteratingIndexVisitor extends ASTVisitor {

	private static final String OUTSIDE_LOOP_INDEX_DECLARATION = "outside-declaration-fragment"; //$NON-NLS-1$
	private static final String LOOP_INITIALIZER = "loop-initializer"; //$NON-NLS-1$
	private static final String LOOP_UPDATER = "loop-updater"; //$NON-NLS-1$
	private static final String INTERNAL_INDEX_UPDATER = "internal-index-updater"; //$NON-NLS-1$

	private static final String PLUS_PLUS = "++"; //$NON-NLS-1$
	private static final String PLUS = "+"; //$NON-NLS-1$
	private static final String ONE = "1"; //$NON-NLS-1$
	private static final String ZERO = "0"; //$NON-NLS-1$
	private static final String GET = "get"; //$NON-NLS-1$

	private SimpleName iteratingIndexName;
	private SimpleName newIteratorName;
	private SimpleName iterableName;
	private ForStatement forStatement;

	private Map<String, ASTNode> indexInitializer;
	private Map<String, ASTNode> indexUpdater;

	private boolean insideLoop = false;
	private boolean beforeLoop = true;
	private boolean afterLoop = false;

	private boolean indexReferencedOutsideLoop = false;
	private boolean indexReferencedInsideLoop = false;
	private boolean multipleLoopInits = false;
	private boolean multipleLoopUpdaters = false;
	private boolean hasEmptyStatement = false;

	private List<MethodInvocation> iteratingObjectInitializers;
	private List<ASTNode> nodesToBeRemoved;
	private VariableDeclarationFragment preferredNameFragment;

	public ForLoopIteratingIndexVisitor(SimpleName iteratingIndexName, SimpleName iterableName,
			ForStatement forStatement) {
		this.iteratingIndexName = iteratingIndexName;
		this.iterableName = iterableName;
		this.forStatement = forStatement;
		this.iteratingObjectInitializers = new ArrayList<>();
		this.indexInitializer = new HashMap<>();
		this.indexUpdater = new HashMap<>();
		this.nodesToBeRemoved = new ArrayList<>();

		List<Expression> initializers = ASTNodeUtil.returnTypedList(forStatement.initializers(), Expression.class);
		if (initializers.size() == 1) {
			indexInitializer.put(LOOP_INITIALIZER, initializers.get(0));
		} else if (initializers.size() > 0) {
			multipleLoopInits = true;
		}

		List<Expression> updaters = ASTNodeUtil.returnTypedList(forStatement.updaters(), Expression.class);
		if (updaters.size() == 1) {
			indexUpdater.put(LOOP_UPDATER, updaters.get(0));
		} else if (updaters.size() > 1) {
			multipleLoopUpdaters = true;
		}

		Statement loopBody = forStatement.getBody();
		if(loopBody.getNodeType() == ASTNode.BLOCK) {
			List<Statement> statements = ASTNodeUtil.returnTypedList(((Block) loopBody).statements(),
					Statement.class);
			if (!statements.isEmpty()) {
				Statement lastStatement = statements.get(statements.size() - 1);
				if (isValidIncrementStatement(lastStatement, iteratingIndexName)) {
					indexUpdater.put(INTERNAL_INDEX_UPDATER, lastStatement);
					nodesToBeRemoved.add(lastStatement);
				}
			}
		}

	}

	public VariableDeclarationFragment getPreferredNameFragment() {
		return preferredNameFragment;
	}

	public List<ASTNode> getNodesToBeRemoved() {
		return this.nodesToBeRemoved;
	}

	public SimpleName getIteratorName() {
		return this.newIteratorName;
	}

	public List<MethodInvocation> getIteratorExpressions() {
		return iteratingObjectInitializers;
	}

	@Override
	public boolean preVisit2(ASTNode node) {
		return !multipleLoopInits && !multipleLoopUpdaters;
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
	public boolean visit(SimpleName simpleName) {
		IBinding resolvedBinding = simpleName.resolveBinding();
		if (resolvedBinding != null && IBinding.VARIABLE == resolvedBinding.getKind()
				&& simpleName.getIdentifier().equals(iteratingIndexName.getIdentifier())) {

			ASTNode parent = simpleName.getParent();
			if (beforeLoop) {
				if (ASTNode.VARIABLE_DECLARATION_FRAGMENT == parent.getNodeType()) {
					VariableDeclarationFragment declarationFragment = (VariableDeclarationFragment) parent;
					indexInitializer.put(OUTSIDE_LOOP_INDEX_DECLARATION, declarationFragment);
					nodesToBeRemoved.add(declarationFragment);

				} else {
					indexReferencedOutsideLoop = true;
				}
			} else if (insideLoop
					&& (parent != indexInitializer.get(LOOP_INITIALIZER)
							|| parent.getParent() != indexInitializer.get(LOOP_INITIALIZER))
					&& parent != forStatement.getExpression() && (parent != indexInitializer.get(LOOP_UPDATER)
							|| parent.getParent() != indexInitializer.get(LOOP_UPDATER))) {

				if (ASTNode.METHOD_INVOCATION == parent.getNodeType()) {
					MethodInvocation methodInvocation = (MethodInvocation) parent;
					Expression methodExpression = methodInvocation.getExpression();

					if (ASTNode.EXPRESSION_STATEMENT == methodInvocation.getParent().getNodeType()) {
						this.hasEmptyStatement = true;
					} else if (GET.equals(methodInvocation.getName().getIdentifier())
							&& methodInvocation.arguments().size() == 1 && methodExpression != null
							&& methodExpression.getNodeType() == ASTNode.SIMPLE_NAME
							&& ((SimpleName) methodExpression).getIdentifier().equals(iterableName.getIdentifier())) {
						iteratingObjectInitializers.add(methodInvocation);

						if (newIteratorName == null
								&& VariableDeclarationFragment.INITIALIZER_PROPERTY == methodInvocation
										.getLocationInParent()) {
							VariableDeclarationFragment fragment = (VariableDeclarationFragment) methodInvocation
									.getParent();
							this.newIteratorName = fragment.getName();
							this.preferredNameFragment = fragment;
						}

					} else {
						this.indexReferencedInsideLoop = true;
					}
				} else if (parent.getLocationInParent() != ForStatement.UPDATERS_PROPERTY
						&& parent.getParent().getLocationInParent() != ForStatement.UPDATERS_PROPERTY
						&& parent.getLocationInParent() != ForStatement.INITIALIZERS_PROPERTY
						&& parent.getParent().getLocationInParent() != ForStatement.INITIALIZERS_PROPERTY) {

					this.indexReferencedInsideLoop = true;
				}
			} else if (afterLoop) {
				indexReferencedOutsideLoop = true;
			}
		}

		return true;
	}

	/**
	 * Checks if the given statement is an increment statement of the operand.
	 * The following three cases are considered:
	 * <ul>
	 * <li>{@code operand++;}</li>
	 * <li>{@code ++operand;}</li>
	 * <li>{@code operand = operand + 1;}</li>
	 * </ul>
	 * 
	 * @param statement
	 *            statement to be checked
	 * @param operandName
	 *            operand name
	 * @return if the statement is an increment statement.
	 */
	private boolean isValidIncrementStatement(Statement statement, SimpleName operandName) {
		boolean isIncrement = false;
		if (ASTNode.EXPRESSION_STATEMENT == statement.getNodeType()) {
			ExpressionStatement expressionStatement = (ExpressionStatement) statement;
			Expression expression = expressionStatement.getExpression();
			int expressionType = expression.getNodeType();

			if (ASTNode.POSTFIX_EXPRESSION == expressionType) {
				// covers the case: operand++;
				PostfixExpression postfixExpression = (PostfixExpression) expression;
				Expression operand = postfixExpression.getOperand();
				if (ASTNode.SIMPLE_NAME == operand.getNodeType()
						&& ((SimpleName) operand).getIdentifier().equals(operandName.getIdentifier())
						&& PLUS_PLUS.equals(postfixExpression.getOperator())) {

					isIncrement = true;
				}
			} else if (ASTNode.ASSIGNMENT == expressionType) {
				Assignment assignmentExpression = (Assignment) expression;
				Expression lhs = assignmentExpression.getLeftHandSide();
				Expression rhs = assignmentExpression.getRightHandSide();

				if (ASTNode.SIMPLE_NAME == lhs.getNodeType()
						&& ((SimpleName) lhs).getIdentifier().equals(operandName.getIdentifier())
						&& ASTNode.INFIX_EXPRESSION == rhs.getNodeType()) {
					// covers the case: operand = operand +1;

					InfixExpression infixExpression = (InfixExpression) rhs;
					Expression leftOperand = infixExpression.getLeftOperand();
					Expression rightOperand = infixExpression.getRightOperand();
					if (PLUS.equals(infixExpression.getOperator()) && ((ASTNode.SIMPLE_NAME == leftOperand.getNodeType()
							&& ((SimpleName) leftOperand).getIdentifier().equals(operandName.getIdentifier())
							&& ASTNode.NUMBER_LITERAL == rightOperand.getNodeType()
							&& ONE.equals(((NumberLiteral) rightOperand).getToken()))
							|| ((ASTNode.SIMPLE_NAME == rightOperand.getNodeType()
									&& ((SimpleName) rightOperand).getIdentifier().equals(operandName.getIdentifier())
									&& ASTNode.NUMBER_LITERAL == leftOperand.getNodeType()
									&& ONE.equals(((NumberLiteral) leftOperand).getToken()))))) {

						isIncrement = true;
					}
				}

			} else if (ASTNode.PREFIX_EXPRESSION == expressionType) {
				// covers the case: ++operand;
				PrefixExpression postfixExpression = (PrefixExpression) expression;
				Expression operand = postfixExpression.getOperand();
				if (ASTNode.SIMPLE_NAME == operand.getNodeType()
						&& ((SimpleName) operand).getIdentifier().equals(operandName.getIdentifier())
						&& PLUS_PLUS.equals(postfixExpression.getOperator())) {
					isIncrement = true;
				}
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
}

