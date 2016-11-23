package at.splendit.simonykees.core.visitor.loop;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import at.splendit.simonykees.core.Activator;
import at.splendit.simonykees.core.builder.NodeBuilder;
import at.splendit.simonykees.core.util.ASTNodeUtil;
import at.splendit.simonykees.core.util.ClassRelationUtil;
import at.splendit.simonykees.core.visitor.AbstractCompilationUnitASTVisitor;

/**
 * For loops with an iterator can be replaced with a forEach loop since 1.7
 * 
 * @author Martin Huter
 * @since 0.9.2
 *
 */
public class ForToForEachASTVisitor extends AbstractCompilationUnitASTVisitor {

	private static final Integer ITERATOR_KEY = 1;
	private static final String ITERATOR_FULLY_QUALLIFIED_NAME = "java.util.Iterator"; //$NON-NLS-1$

	private static final Integer LIST_KEY = 2;
	private static final String LIST_FULLY_QUALLIFIED_NAME = "java.util.List"; //$NON-NLS-1$

	public ForToForEachASTVisitor() {
		super();
		this.fullyQuallifiedNameMap.put(ITERATOR_KEY, generateFullyQuallifiedNameList(ITERATOR_FULLY_QUALLIFIED_NAME));
		this.fullyQuallifiedNameMap.put(LIST_KEY, generateFullyQuallifiedNameList(LIST_FULLY_QUALLIFIED_NAME));
	}

	@Override
	public boolean visit(ForStatement node) {
		if (node.getExpression() instanceof MethodInvocation) {
			MethodInvocation methodInvocation = (MethodInvocation) node.getExpression();
			// check for hasNext operation on Iterator

			if (StringUtils.equals("hasNext", methodInvocation.getName().getFullyQualifiedName()) //$NON-NLS-1$
					&& methodInvocation.getExpression() instanceof SimpleName) {
				SimpleName iteratorName = (SimpleName) methodInvocation.getExpression();

				if (iteratorName != null && !ClassRelationUtil
						.isContentOfRegistertITypes(iteratorName.resolveTypeBinding(), iTypeMap.get(ITERATOR_KEY))) {
					// Type is not an Iterator
					return false;
				}
				IteratorDefinitionASTVisior iteratorDefinitionAstVisior = new IteratorDefinitionASTVisior(iteratorName);
				if (1 == node.initializers().size()) {
					((ASTNode) node.initializers().get(0)).accept(iteratorDefinitionAstVisior);
				}

				FindNextVariableASTVisitor findNextVariableAstVisitor = new FindNextVariableASTVisitor(
						(SimpleName) iteratorName);
				findNextVariableAstVisitor.setAstRewrite(this.astRewrite);
				node.getBody().accept(findNextVariableAstVisitor);
				if (findNextVariableAstVisitor.isTransformable() && iteratorDefinitionAstVisior.getList() != null) {

					SimpleName iterationVariable = findNextVariableAstVisitor.getVariableName();
					Type iterationType = findNextVariableAstVisitor.getIteratorVariableType();
					// wenn der typ == null ist muss der typ wieder außerhab
					// gesucht werden

					SingleVariableDeclaration iterationVariableDefinition = NodeBuilder.newSingleVariableDeclaration(
							node.getAST(), (SimpleName) astRewrite.createMoveTarget(iterationVariable),
							(Type) astRewrite.createMoveTarget(iterationType));

					Expression iterationList = (Expression) astRewrite
							.createMoveTarget(iteratorDefinitionAstVisior.getList());

					EnhancedForStatement newFor = NodeBuilder.newEnhandesForStatement(node.getAST(),
							(Statement) astRewrite.createMoveTarget(node.getBody()), iterationList,
							iterationVariableDefinition);
					astRewrite.remove(findNextVariableAstVisitor.getRemoveWithTransformation(), null);
					astRewrite.replace(node, newFor, null);
				}
			}
		}

		// Preconditions for second case
		// node expression is an infixExpression of iterationVariable <
		// listName.size()
		// updaters have only one entry
		// initializers have only one entry
		if (node.getExpression() instanceof InfixExpression && node.updaters().size() == 1
				&& node.initializers().size() == 1) {
			// needed components for refactoring
			SimpleName listName = null;
			SimpleName iterationVariable = null;

			/*
			 * first condition: node expression is an infixExpression of
			 * iterationVariable < listName.size()
			 */
			{
				InfixExpression infixExpression = (InfixExpression) node.getExpression();

				/*
				 * lesserSide < greaterSide; greaterSide > lesserSide
				 */
				Expression lesserSide = null;
				Expression greaterSide = null;
				if (infixExpression.getOperator().equals(InfixExpression.Operator.GREATER)) {
					greaterSide = infixExpression.getLeftOperand();
					lesserSide = infixExpression.getRightOperand();
				}
				if (infixExpression.getOperator().equals(InfixExpression.Operator.LESS)) {
					lesserSide = infixExpression.getLeftOperand();
					greaterSide = infixExpression.getRightOperand();
				}
				if (lesserSide != null && greaterSide != null && greaterSide instanceof MethodInvocation) {
					MethodInvocation methodInvocation = (MethodInvocation) greaterSide;
					if (StringUtils.equals("size", methodInvocation.getName().getFullyQualifiedName()) //$NON-NLS-1$
							&& methodInvocation.getExpression() instanceof SimpleName
							&& lesserSide instanceof SimpleName) {
						listName = (SimpleName) methodInvocation.getExpression();
						iterationVariable = (SimpleName) lesserSide;
						if (listName != null && !ClassRelationUtil.isInheritingContentOfRegistertITypes(
								listName.resolveTypeBinding(), iTypeMap.get(LIST_KEY))) {
							// Iteration objects are no List
							return true;
						}
					} else {
						return true;
					}
				}
			}
			/*
			 * second condition: updater is only an increment on variable
			 */
			{
				// Check if its only an In
				CheckIfOnlyIncrementASTVisitor checkIfOnlyIncrement = new CheckIfOnlyIncrementASTVisitor(
						iterationVariable);
				((ASTNode) node.updaters().get(0)).accept(checkIfOnlyIncrement);
				if (!checkIfOnlyIncrement.isEligible()) {
					return true;
				}
			}
			/*
			 * third condition: iteration variable is initialized to 0 and only
			 * used in while
			 */
			{
				CheckVariableInitializationASTVisitor checkVariableInitialization = new CheckVariableInitializationASTVisitor(
						iterationVariable);
				((ASTNode) node.initializers().get(0)).accept(checkVariableInitialization);
				if (checkVariableInitialization.isEligible()) {
					if (checkVariableInitialization.getVariableType() == null) {
						Block surroundingBlock = ASTNodeUtil.getSurroundingBlock(node);
						if (surroundingBlock != null) {
							VariableDefinitionASTVisitor variableDefinitionAstVisitor = new VariableDefinitionASTVisitor(
									iterationVariable, node);
							surroundingBlock.accept(variableDefinitionAstVisitor);
							if (!variableDefinitionAstVisitor.isUseableLoopVariable()) {
								return true;
							}
						}
					}
				}
			}
			/**
			 * forth condition: the list is only used with get in the loop body
			 */
			{
				ListOnlyGetMethodInvocationASTVisitor listOnlyGetMethodInvocationASTVisitor = new ListOnlyGetMethodInvocationASTVisitor(
						iterationVariable, listName);
				node.getBody().accept(listOnlyGetMethodInvocationASTVisitor);
				if (!listOnlyGetMethodInvocationASTVisitor.isEligible()) {
					return false;
				}
			}

			/*
			 * All Conditions met Do refactoring
			 */
			{
				Activator.log("WE DID IT"); //$NON-NLS-1$
			}
		}
		return true;
	}

	private class ListOnlyGetMethodInvocationASTVisitor extends ASTVisitor {

		private boolean eligible = true;
		private ASTMatcher astMatcher = new ASTMatcher();

		private SimpleName iterationVariable;
		private SimpleName listVariable;

		public ListOnlyGetMethodInvocationASTVisitor(SimpleName iterationVariable, SimpleName listVariable) {
			this.listVariable = listVariable;
			this.iterationVariable = iterationVariable;
		}

		/**
		 * 
		 */
		@Override
		public boolean visit(MethodInvocation node) {
			if (astMatcher.match(listVariable, node.getExpression())) {
				if ("get".equals(node.getName().getFullyQualifiedName())) { //$NON-NLS-1$
					if (node.arguments().size() == 1 && astMatcher.match(iterationVariable, node.arguments().get(0))) {
						eligible = eligible && true;
					} else {
						eligible = eligible && false;
						return false;
					}

				}
				// other function called on the list
				else {
					eligible = eligible && false;
					return false;
				}
			}
			return true;
		}

		public boolean isEligible() {
			return eligible;
		}
	}

	private class CheckIfOnlyIncrementASTVisitor extends ASTVisitor {

		private boolean eligible = false;
		private SimpleName iterationVariable;
		private ASTMatcher astMatcher = new ASTMatcher();

		public CheckIfOnlyIncrementASTVisitor(SimpleName iterationVariable) {
			this.iterationVariable = iterationVariable;
		}

		@Override
		public boolean visit(PostfixExpression node) {
			if (astMatcher.match(iterationVariable, node.getOperand())
					&& PostfixExpression.Operator.INCREMENT.equals(node.getOperand())) {
				eligible = true;
			}
			return false;
		}

		@Override
		public boolean visit(PrefixExpression node) {
			if (astMatcher.match(iterationVariable, node.getOperand())
					&& PrefixExpression.Operator.INCREMENT.equals(node.getOperand())) {
				eligible = true;
			}
			return false;
		}

		@Override
		public boolean visit(Assignment node) {
			if (astMatcher.match(iterationVariable, node.getLeftHandSide())) {
				if (node.getRightHandSide() instanceof InfixExpression) {
					InfixExpression infixExpression = (InfixExpression) node.getRightHandSide();
					if ((infixExpression.extendedOperands() == null || infixExpression.extendedOperands().size() == 0)
							&& InfixExpression.Operator.PLUS.equals(infixExpression.getOperator())) {
						Expression leftOperand = infixExpression.getLeftOperand();
						Expression rightOperand = infixExpression.getRightOperand();
						if (rightOperand instanceof SimpleName && leftOperand instanceof NumberLiteral) {
							eligible = allowedIncrement((SimpleName) rightOperand, (NumberLiteral) leftOperand);
						}
						if (leftOperand instanceof SimpleName && rightOperand instanceof NumberLiteral) {
							eligible = allowedIncrement((SimpleName) leftOperand, (NumberLiteral) rightOperand);
						}
					}
				}
			}
			return false;
		}

		private boolean allowedIncrement(SimpleName identifier, NumberLiteral increment) {
			return astMatcher.match(iterationVariable, identifier) && "1".equals(increment.getToken()); //$NON-NLS-1$
		}

		public boolean isEligible() {
			return eligible;
		}
	}

	private class CheckVariableInitializationASTVisitor extends ASTVisitor {

		private SimpleName iterationVariable;
		private ASTMatcher astMatcher = new ASTMatcher();

		private boolean eligible = false;
		private Type variableType;

		public CheckVariableInitializationASTVisitor(SimpleName iterationVariable) {
			this.iterationVariable = iterationVariable;
		}

		@Override
		public void endVisit(VariableDeclarationExpression node) {
			if (eligible) {
				// useless? get of Collection must be an int/Integer!
				variableType = node.getType();
			}
		}

		@Override
		public boolean visit(Assignment node) {
			if (node.getRightHandSide() instanceof NumberLiteral) {
				eligible = testLiteralIsZeroAndVariableMatches(node.getLeftHandSide(),
						(NumberLiteral) node.getRightHandSide());
			}
			return false;
		}

		@Override
		public boolean visit(VariableDeclarationFragment node) {
			if (node.getInitializer() instanceof NumberLiteral) {
				eligible = testLiteralIsZeroAndVariableMatches(node.getName(), (NumberLiteral) node.getInitializer());
			}
			return false;
		}

		public boolean testLiteralIsZeroAndVariableMatches(Expression variable, NumberLiteral literal) {
			return "0".equals(literal.getToken()) && astMatcher.match(iterationVariable, variable); //$NON-NLS-1$
		}

		public boolean isEligible() {
			return eligible;
		}

		public Type getVariableType() {
			return variableType;
		}
	}
}
