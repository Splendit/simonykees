package at.splendit.simonykees.core.visitor.loop;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.WhileStatement;

import at.splendit.simonykees.core.builder.NodeBuilder;
import at.splendit.simonykees.core.visitor.AbstractCompilationUnitAstVisitor;

/**
 * While-loops over Iterators that could be expressed with a for-loop are
 * transformed to a equivalent for-loop.
 * 
 * @author Martin Huter
 * @since 0.9.2
 *
 */
public class WhileToForASTVisitor extends AbstractCompilationUnitAstVisitor {

	private static String ITERATOR = "java.util.Iterator"; //$NON-NLS-1$
	private SimpleName iterationVariable = null;;

	@Override
	public boolean visit(WhileStatement node) {
		iterationVariable = null;
		SimpleName iteratorExpression = replaceAbleWhileCondition(node.getExpression());
		if (iteratorExpression != null) {
			ITypeBinding iteratorBinding = iteratorExpression.resolveTypeBinding();
			if (isContentofRegistertITypes(iteratorBinding)) {
				ASTNode parentNode = findParentBlock(node);
				if (parentNode == null) {
					// No surrounding parent block found
					// should not happen, because the Iterator has to be
					// defined in an parent block.
					return false;
				}
				IteratorDefinitionAstVisior iteratorDefinitionAstVisior = new IteratorDefinitionAstVisior(
						(SimpleName) iteratorExpression);
				iteratorDefinitionAstVisior.setAstRewrite(this.astRewrite);
				parentNode.accept(iteratorDefinitionAstVisior);
				Type svdType = null;
				FindNextVariableAstVisitor findNextVariableAstVisitor = null;
				if (iterationVariable == null) {
					findNextVariableAstVisitor = new FindNextVariableAstVisitor((SimpleName) iteratorExpression);
					findNextVariableAstVisitor.setAstRewrite(this.astRewrite);
					node.getBody().accept(findNextVariableAstVisitor);
					if (findNextVariableAstVisitor.getVariableName() != null
							&& findNextVariableAstVisitor.isTransformable()) {
						iterationVariable = findNextVariableAstVisitor.getVariableName();
						svdType = findNextVariableAstVisitor.getIteratorVariableType();
					}
				}

				if (iteratorDefinitionAstVisior.getList() != null && iterationVariable != null) {

					if (svdType == null) {
						// variable is not in while defined check if
						// unused in other context and extract type
						VariableDefinitionAstVisiotr variableDefinitionAstVisior = new VariableDefinitionAstVisiotr(
								iterationVariable, node);
						parentNode.accept(variableDefinitionAstVisior);
						if (variableDefinitionAstVisior.getVariableDeclarationStatement() != null) {
							svdType = variableDefinitionAstVisior.getVariableDeclarationStatement().getType();
							astRewrite.remove(variableDefinitionAstVisior.getVariableDeclarationStatement(), null);
						} else {
							// exclusion ground found
							return false;
						}
					} else {

					}
					SingleVariableDeclaration svd = NodeBuilder.newSingleVariableDeclaration(node.getAST(),
							(SimpleName) astRewrite.createMoveTarget(iterationVariable),
							(Type) astRewrite.createMoveTarget(svdType));
					EnhancedForStatement newFor = NodeBuilder.newEnhandesForStatement(node.getAST(),
							(Statement) astRewrite.createMoveTarget(node.getBody()),
							(Expression) astRewrite.createMoveTarget(iteratorDefinitionAstVisior.getList()), svd);
					astRewrite.replace(node, newFor, null);
					// executed here, because a breaking statement can
					// be found after the setting of the type
					if (findNextVariableAstVisitor != null) {
						astRewrite.remove(findNextVariableAstVisitor.getRemoveWithTransformation(), null);
					}
					astRewrite.remove(iteratorDefinitionAstVisior.getIteratorDeclarationStatement(), null);
				}
			}
		}
		return true;
	}

	private SimpleName replaceAbleWhileCondition(Expression node) {
		if (node instanceof MethodInvocation) {
			MethodInvocation methodInvocation = (MethodInvocation) node;
			// check for hasNext operation on Iterator
			if (StringUtils.equals("hasNext", methodInvocation.getName().getFullyQualifiedName()) //$NON-NLS-1$
					&& methodInvocation.getExpression() instanceof SimpleName) {
				return (SimpleName) methodInvocation.getExpression();
			}
		}
		if (node instanceof InfixExpression) {
			InfixExpression infixExpression = (InfixExpression) node;
			if (InfixExpression.Operator.NOT_EQUALS.equals(infixExpression.getOperator())) {
				Expression possibleNextOperation = null;
				if (infixExpression.getLeftOperand() instanceof NullLiteral) {
					possibleNextOperation = infixExpression.getRightOperand();
				}
				if (infixExpression.getRightOperand() instanceof NullLiteral) {
					possibleNextOperation = infixExpression.getLeftOperand();
				}
				if (possibleNextOperation != null) {
					if (possibleNextOperation instanceof ParenthesizedExpression
							&& ((ParenthesizedExpression) possibleNextOperation)
									.getExpression() instanceof Assignment) {
						Assignment loopVariableAssignment = (Assignment) ((ParenthesizedExpression) possibleNextOperation)
								.getExpression();
						if (loopVariableAssignment.getRightHandSide() instanceof MethodInvocation) {
							MethodInvocation methodInvocation = (MethodInvocation) loopVariableAssignment
									.getRightHandSide();
							// check for hasNext operation on Iterator
							if (StringUtils.equals("next", methodInvocation.getName().getFullyQualifiedName()) //$NON-NLS-1$
									&& methodInvocation.getExpression() instanceof SimpleName
									&& loopVariableAssignment.getLeftHandSide() instanceof SimpleName) {
								iterationVariable = (SimpleName) loopVariableAssignment.getLeftHandSide();
								return (SimpleName) methodInvocation.getExpression();
							}
						}
					}
				}
			}
		}
		return null;
	}

	private Block findParentBlock(ASTNode node) {
		if (node == null) {
			return null;
		}
		if (node.getParent() instanceof Block) {
			return (Block) node.getParent();
		}
		return findParentBlock(node.getParent());
	}

	@Override
	protected String[] relevantClasses() {
		return new String[] { ITERATOR };
	}

}
