package at.splendit.simonykees.core.visitor.loop;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.WhileStatement;

import at.splendit.simonykees.core.util.ASTNodeUtil;

/**
 * A super class of the visitors that analyzes a loop (@link {@link ForStatement} or {@link WhileStatement})
 * whether it can be transformed to an {@link EnhancedForStatement} or not.
 * 
 * @author Ardit Ymeri
 * @since 1.2
 *
 */
public abstract class LoopIteratingIndexASTVisitor extends ASTVisitor {
	
	protected static final String ONE = "1"; //$NON-NLS-1$
	protected static final String ZERO = "0"; //$NON-NLS-1$
	private static final String GET = "get"; //$NON-NLS-1$
	
	private SimpleName iterableName;
	protected SimpleName newIteratorName;
	protected VariableDeclarationFragment preferredNameFragment;
	private List<ASTNode> nodesToBeRemoved;
	
	protected boolean insideLoop = false;
	protected boolean beforeLoop = true;
	protected boolean afterLoop = false;
	protected boolean hasEmptyStatement = false;
	
	private List<ASTNode> iteratingObjectInitializers;
	protected boolean indexReferencedOutsideLoop = false;
	protected boolean indexReferencedInsideLoop = false;
	
	protected LoopIteratingIndexASTVisitor(SimpleName iterableNode) {
		this.iterableName = iterableNode;
		this.nodesToBeRemoved = new ArrayList<>();
		this.iteratingObjectInitializers = new ArrayList<>();
	}

	/**
	 * Checks whether the given expression is an assignment to zero
	 * expression of a variable with the same name as the given simple name. 
	 * 
	 * @param name simple name to check for
	 * @param expression expression to investigate
	 * 
	 * @return {@code true} if the expression is an assignment to zero expression
	 * of a variable with the given simple name. 
	 */
	protected boolean isAssignmetnToZero(SimpleName name, Expression expression) {
		boolean isAssignmentOfIteratingIndex = false;
		if(ASTNode.ASSIGNMENT == expression.getNodeType()) {
			Assignment assignment = (Assignment)expression;
			Expression lhs = assignment.getLeftHandSide();
			Expression rhs = assignment.getRightHandSide();
			if(ASTNode.SIMPLE_NAME == lhs.getNodeType() && ((SimpleName)lhs).getIdentifier().equals(name.getIdentifier()) 
					&& ASTNode.NUMBER_LITERAL == rhs.getNodeType() && ZERO.equals(((NumberLiteral)rhs).getToken())) {
				isAssignmentOfIteratingIndex = true;
			}
		}
		return isAssignmentOfIteratingIndex;
	}

	@Override
	public boolean visit(AnonymousClassDeclaration node) {
		// Anonymous classes have their own scope
		return false;
	}
	
	public SimpleName getIteratorName() {
		return this.newIteratorName;
	}
	
	public List<ASTNode> getNodesToBeRemoved() {
		return this.nodesToBeRemoved;
	}
	
	protected void markAsToBeRemoved(ASTNode node) {
		nodesToBeRemoved.add(node);
	}
	
	public List<ASTNode> getIteratingObjectInitializers() {
		return iteratingObjectInitializers;
	}

	protected void addIteratingObjectInitializer(ASTNode node) {
		iteratingObjectInitializers.add(node);
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

	public VariableDeclarationFragment getPreferredNameFragment() {
		return this.preferredNameFragment;
	}
	
	protected void setIndexReferencedInsideLoop() {
		this.indexReferencedInsideLoop = true;

	}
	
	protected void setHasEmptyStatement() {
		this.hasEmptyStatement = true;
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
	protected boolean isValidIncrementExpression(Expression expression, SimpleName operandName) {
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
	 * Checks whether the given expression is an initialization to zero
	 * expression.
	 */
	protected boolean isInitializationToZero(ASTNode initExpresion) {
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
	
	/**
	 * Checks whether the given instance of a {@link SimpleName} is used as an
	 * index for accessing an element of the given iterableName. Furthermore, checks
	 * whether the array access occurs in the left hand side of an assignment.
	 * 
	 * @param simpleName
	 *            simple name to be checked.
	 * @param iterableName
	 *            name of the iterable object
	 * @return {@code true} if the parent of the simpleName is an
	 *         {@link ArrayAccess} which matches with {@code iterableName}.
	 */
	protected boolean isReplaceableArrayAccess(SimpleName simpleName, SimpleName iterableName) {
		ASTNode node = simpleName.getParent();
		boolean replacableAccess = false;
		if (ASTNode.ARRAY_ACCESS == node.getNodeType()
				&& node.getLocationInParent() != Assignment.LEFT_HAND_SIDE_PROPERTY) {
			ArrayAccess arrayAccess = (ArrayAccess) node;
			Expression arrayExpression = arrayAccess.getArray();
			if (ASTNode.SIMPLE_NAME == arrayExpression.getNodeType()) {
				SimpleName arrayName = (SimpleName) arrayExpression;
				replacableAccess = arrayName.getIdentifier().equals(iterableName.getIdentifier());
			}
		}
		return replacableAccess;
	}
	
	/**
	 * Analyzes the occurrences of the iterating index of a loop. Checks whether 
	 * the iterating index is referenced outside the loop and whether it is 
	 * used only as an index of an array access. 
	 * 
	 * @param simpleName name of the iterating index
	 */
	protected void visitIndexOfArrayAccess(SimpleName simpleName) {
		if (!isNameOfIteratingIndex(simpleName)) {
			return;
		}

		if (isBeforeLoop()) {
			analyseBeforeLoopOccurrence(simpleName);
		} else if (isInsideLoop() && !isLoopProperty(simpleName)) {

			if (isReplaceableArrayAccess(simpleName, this.iterableName)) {
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
	
	/**
	 * Analyzes the occurrences of the iterating index of a loop. Checks whether 
	 * the iterating index is referenced outside the loop and whether it is 
	 * only used as an index for getting an element of the iterable object {@link #iterableName}
	 * i.e. it is only used as a parameter in the invocation of {@link List#get(int)} . 
	 * 
	 * @param simpleName name of the iterating index
	 */
	protected void visitIndexOfIterableGet(SimpleName simpleName) {
		if(!isNameOfIteratingIndex(simpleName)) {
			return;
		}

		ASTNode parent = simpleName.getParent();
		if (isBeforeLoop()) {
			analyseBeforeLoopOccurrence(simpleName);
		} else if (isInsideLoop() && !isLoopProperty(simpleName)) {

			if (ASTNode.METHOD_INVOCATION == parent.getNodeType()) {
				MethodInvocation methodInvocation = (MethodInvocation) parent;
				Expression methodExpression = methodInvocation.getExpression();

				if (GET.equals(methodInvocation.getName().getIdentifier())
						&& methodInvocation.arguments().size() == 1 && methodExpression != null
						&& methodExpression.getNodeType() == ASTNode.SIMPLE_NAME
						&& ((SimpleName) methodExpression).getIdentifier().equals(iterableName.getIdentifier())) {
					
					if (ASTNode.EXPRESSION_STATEMENT == methodInvocation.getParent().getNodeType()) {
						/*
						 * replacing the expression statement with a variable name leads to compile error
						 */
						setHasEmptyStatement();
						return;
					}
					
					/*
					 * simpleName is the parameter of the get() method in the iterable object. 
					 */
					addIteratingObjectInitializer(methodInvocation);

					// store the preferred iterator name
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
			} else {
				setIndexReferencedInsideLoop();
			}
		} else if (isAfterLoop()) {
			setIndexReferencedOutsideLoop();
		}
	}
	
	protected abstract boolean isNameOfIteratingIndex(SimpleName simpleName);
	
	protected abstract boolean isLoopProperty(SimpleName simpleName);
	
	protected abstract void analyseBeforeLoopOccurrence(SimpleName simpleName);
	
	public abstract boolean checkTransformPrecondition();

}
