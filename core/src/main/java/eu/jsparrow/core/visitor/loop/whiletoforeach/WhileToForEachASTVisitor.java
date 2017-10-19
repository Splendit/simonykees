package eu.jsparrow.core.visitor.loop.whiletoforeach;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.WhileStatement;

import eu.jsparrow.core.util.ASTNodeUtil;
import eu.jsparrow.core.util.ClassRelationUtil;
import eu.jsparrow.core.visitor.loop.IteratingIndexVisitorFactory;
import eu.jsparrow.core.visitor.loop.LoopOptimizationASTVisior;
import eu.jsparrow.core.visitor.loop.LoopToForEachASTVisitor;

/**
 * While-loops over Iterators that could be expressed with a for-loop are
 * transformed to a equivalent for-loop.
 * 
 * @author Martin Huter, Ardit Ymeri
 * @since 0.9.2
 *
 */
public class WhileToForEachASTVisitor extends LoopToForEachASTVisitor<WhileStatement> {

	private Map<WhileStatement, LoopOptimizationASTVisior> replaceInformationASTVisitorList;
	private Map<String, Integer> multipleIteratorUse;

	public WhileToForEachASTVisitor() {
		this.replaceInformationASTVisitorList = new HashMap<>();
		this.multipleIteratorUse = new HashMap<>();
	}

	@Override
	public boolean visit(WhileStatement node) {
		if(isSingleStatementBodyOfOuterLoop(node)) {
			return true;
		}
		
		// skip loops with empty condition
		Expression loopCondition = node.getExpression();
		if(loopCondition == null) {
			return true;
		}
		
		SimpleName iteratorName = ASTNodeUtil.replaceableIteratorCondition(loopCondition);

		if (iteratorName != null) {
			if (ClassRelationUtil.isContentOfTypes(iteratorName.resolveTypeBinding(),
					generateFullyQualifiedNameList(ITERATOR_FULLY_QUALLIFIED_NAME))) {
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
				iteratorDefinitionAstVisior.setASTRewrite(this.astRewrite);
				parentNode.accept(iteratorDefinitionAstVisior);

				if (iteratorDefinitionAstVisior.allParametersFound()) {
					replaceInformationASTVisitorList.put(node, iteratorDefinitionAstVisior);
				}
			}
		} else if (ASTNode.INFIX_EXPRESSION == loopCondition.getNodeType()) {
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
					IteratingIndexVisitorFactory<WhileStatement> factory = WhileLoopOverListsASTVisitor::new;
					MethodInvocation condition = (MethodInvocation) rhs;
					analyzeLoopOverList(node, node.getBody(), condition, index, factory);

				} else if (ASTNode.QUALIFIED_NAME == rhs.getNodeType()) {
					// iterating over arrays
					IteratingIndexVisitorFactory<WhileStatement> factory = WhileLoopOverArraysASTVisitor::new;
					QualifiedName condition = (QualifiedName) rhs;
					analyzeLoopOverArray(node, node.getBody(), condition, index, factory);
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
			Map<String, Boolean> newNameMap = generateNewIteratorName(null, node, iteratorDefinitionAstVisior.getListName());
			String newName = newNameMap.keySet().iterator().next();
			iteratorDefinitionAstVisior.replaceLoop(node, node.getBody(), multipleIteratorUse, newName);

			// clear the variableIterator if no other loop is present
			if (replaceInformationASTVisitorList.isEmpty()) {
				multipleIteratorUse.clear();
			}
		}
		
		clearTempItroducedNames(node);
	}
}
