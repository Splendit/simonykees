package at.splendit.simonykees.core.visitor.loop;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.WhileStatement;

import at.splendit.simonykees.core.util.ASTNodeUtil;
import at.splendit.simonykees.core.util.ClassRelationUtil;

/**
 * While-loops over Iterators that could be expressed with a for-loop are
 * transformed to a equivalent for-loop.
 * 
 * @author Martin Huter
 * @since 0.9.2
 *
 */
public class WhileToForEachASTVisitor extends LoopToForEachASTVisitor {

	private Map<WhileStatement, LoopOptimizationASTVisior> replaceInformationASTVisitorList;
	private Map<String, Integer> multipleIteratorUse;

	public WhileToForEachASTVisitor() {
		this.replaceInformationASTVisitorList = new HashMap<>();
		this.multipleIteratorUse = new HashMap<>();
	}

	@Override
	public boolean visit(WhileStatement node) {
		SimpleName iteratorName = ASTNodeUtil.replaceableIteratorCondition(node.getExpression());
		Expression loopCondition = node.getExpression();
		if (iteratorName != null) {
			if (ClassRelationUtil.isContentOfTypes(iteratorName.resolveTypeBinding(),
					generateFullyQuallifiedNameList(ITERATOR_FULLY_QUALLIFIED_NAME))) {
				Block parentNode = ASTNodeUtil.getSpecificAncestor(node, Block.class);
				if (parentNode == null) {
					/*
					 * No surrounding parent block found should not happen,
					 * because the Iterator has to be defined in an parent
					 * block.
					 */
					return false;
				}
				LoopOptimizationASTVisior iteratorDefinitionAstVisior = new LoopOptimizationASTVisior(
						(SimpleName) iteratorName, node);
				iteratorDefinitionAstVisior.setAstRewrite(this.astRewrite);
				parentNode.accept(iteratorDefinitionAstVisior);

				if (iteratorDefinitionAstVisior.allParametersFound()) {
					replaceInformationASTVisitorList.put(node, iteratorDefinitionAstVisior);
				}
			}
		} else if (loopCondition != null && ASTNode.INFIX_EXPRESSION == loopCondition.getNodeType()) {
			// if the condition of the for loop is an infix expression....
			InfixExpression infixExpression = (InfixExpression) loopCondition;
			Expression rhs = infixExpression.getRightOperand();
			Expression lhs = infixExpression.getLeftOperand();

			// if the expression operator is '<' and lhs is a simple name...
			if (InfixExpression.Operator.LESS.equals(infixExpression.getOperator())
					&& Expression.SIMPLE_NAME == lhs.getNodeType()) {
				SimpleName index = (SimpleName) lhs;

				if (ASTNode.METHOD_INVOCATION == rhs.getNodeType()) {
					// iterating over Lists
					MethodInvocation condition = (MethodInvocation) rhs;
					Expression conditionExpression = condition.getExpression();
					if (conditionExpression != null && Expression.SIMPLE_NAME == conditionExpression.getNodeType()) {
						SimpleName iterableNode = (SimpleName) conditionExpression;
						ITypeBinding iterableTypeBinding = iterableNode.resolveTypeBinding();

						/*
						 * ...and the right hand side of the infix expression is
						 * an invocation of List::size in the iterable object
						 */
						if (ClassRelationUtil.isInheritingContentOfTypes(iterableTypeBinding,
								Collections.singletonList(ITERABLE_FULLY_QUALIFIED_NAME))
								&& StringUtils.equals(SIZE, condition.getName().getIdentifier())
								&& condition.arguments().isEmpty()) {

							/*
							 * Initiate a visitor for investigating the
							 * replacement precondition and gathering the
							 * replacement information
							 */
							Block outerBlock = ASTNodeUtil.getSpecificAncestor(node, Block.class);
							LoopIteratingIndexASTVisitor indexVisitor = new WhileLoopOverListsASTVisitor(index,
									iterableNode, node, outerBlock);
							outerBlock.accept(indexVisitor);

							if (indexVisitor.checkTransformPrecondition()) {
								Type iteratorType = findIteratorType(iterableTypeBinding);
								if (iteratorType != null) {
									replaceWithEnhancedFor(node, node.getBody(), iterableNode, indexVisitor, iteratorType);
								}
							}
						}
					}

				} else if (ASTNode.QUALIFIED_NAME == rhs.getNodeType()) {
					// iterating over arrays
					QualifiedName condition = (QualifiedName) rhs;
					Name qualifier = condition.getQualifier();
					SimpleName name = condition.getName();

					if (LENGTH.equals(name.getIdentifier()) && qualifier.isSimpleName()) {
						SimpleName iterableNode = (SimpleName) qualifier;
						ITypeBinding iterableTypeBinding = qualifier.resolveTypeBinding();
						if (iterableTypeBinding != null && iterableTypeBinding.isArray()) {

							Block outerBlock = ASTNodeUtil.getSpecificAncestor(node, Block.class);
							LoopIteratingIndexASTVisitor indexVisitor = new WhileLoopOverArraysASTVisitor(index,
									iterableNode, node, outerBlock);
							outerBlock.accept(indexVisitor);

							if (indexVisitor.checkTransformPrecondition()) {
								Type iteratorType = findIteratorType(iterableTypeBinding);
								if (iteratorType != null) {
									replaceWithEnhancedFor(node, node.getBody(), iterableNode, indexVisitor, iteratorType);
								}
							}

						}
					}
				}
			}
		}
		return true;
	}

	@Override
	public void endVisit(WhileStatement node) {
		// Do the replacement
		if (replaceInformationASTVisitorList.containsKey(node)) {
			LoopOptimizationASTVisior iteratorDefinitionAstVisior = replaceInformationASTVisitorList.remove(node);
			iteratorDefinitionAstVisior.replaceLoop(node, node.getBody(), multipleIteratorUse);

			// clear the variableIterator if no other loop is present
			if (replaceInformationASTVisitorList.isEmpty()) {
				multipleIteratorUse.clear();
			}
		}
		
		clearTempItroducedNames(node);
	}

}
