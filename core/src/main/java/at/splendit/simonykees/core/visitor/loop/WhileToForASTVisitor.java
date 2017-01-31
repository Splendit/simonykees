package at.splendit.simonykees.core.visitor.loop;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import at.splendit.simonykees.core.builder.NodeBuilder;
import at.splendit.simonykees.core.constants.ReservedNames;
import at.splendit.simonykees.core.util.ASTNodeUtil;
import at.splendit.simonykees.core.util.ClassRelationUtil;
import at.splendit.simonykees.core.visitor.AbstractCompilationUnitASTVisitor;

/**
 * While-loops over Iterators that could be expressed with a for-loop are
 * transformed to a equivalent for-loop.
 * 
 * @author Martin Huter
 * @since 0.9.2
 *
 */
public class WhileToForASTVisitor extends AbstractCompilationUnitASTVisitor {

	private static Integer ITERATOR_KEY = 1;
	private static String ITERATOR_FULLY_QUALLIFIED_NAME = "java.util.Iterator"; //$NON-NLS-1$

	// private SimpleName iterationVariable = null;

	private Map<WhileStatement, LoopOptimizationASTVisior> replaceInformationASTVisitorList;
	private Map<String, Integer> multipleIteratorUse;

	public WhileToForASTVisitor() {
		super();
		this.fullyQuallifiedNameMap.put(ITERATOR_KEY, generateFullyQuallifiedNameList(ITERATOR_FULLY_QUALLIFIED_NAME));
		this.replaceInformationASTVisitorList = new HashMap<>();
		this.multipleIteratorUse = new HashMap<>();
	}

	@Override
	public boolean visit(WhileStatement node) {
		SimpleName iteratorName = replaceAbleWhileCondition(node.getExpression());
		if (iteratorName != null) {
			if (ClassRelationUtil.isContentOfRegistertITypes(iteratorName.resolveTypeBinding(),
					iTypeMap.get(ITERATOR_KEY))) {
				Block parentNode = ASTNodeUtil.getSurroundingBlock(node);
				if (parentNode == null) {
					// No surrounding parent block found
					// should not happen, because the Iterator has to be
					// defined in an parent block.
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
		return null;
	}

	@Override
	public void endVisit(WhileStatement node) {
		// Do the replacement
		if (replaceInformationASTVisitorList.containsKey(node)) {
			LoopOptimizationASTVisior iteratorDefinitionAstVisior = replaceInformationASTVisitorList.remove(node);
			Type iteratorType = ASTNodeUtil.getSingleTypeParameterOfVariableDeclaration(iteratorDefinitionAstVisior.getIteratorDeclaration());

			// iterator has no type-parameter therefore a optimization is could
			// not be applied
			if (null == iteratorType) {
				return;
			}
			else {
				iteratorType = (Type) astRewrite.createMoveTarget(iteratorType);
			}

			// find LoopvariableName

			MethodInvocation nextCall = iteratorDefinitionAstVisior.getIteratorNextCall();
			SingleVariableDeclaration singleVariableDeclaration = null;
			if (nextCall.getParent() instanceof SingleVariableDeclaration) {
				singleVariableDeclaration = (SingleVariableDeclaration) astRewrite
						.createMoveTarget(nextCall.getParent());
				astRewrite.remove(nextCall.getParent(), null);
			} else if (nextCall.getParent() instanceof VariableDeclarationFragment
					&& nextCall.getParent().getParent() instanceof VariableDeclarationStatement) {
				VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) nextCall
						.getParent();
				VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) variableDeclarationFragment
						.getParent();
				if (1 == variableDeclarationStatement.fragments().size()) {
					singleVariableDeclaration = NodeBuilder.newSingleVariableDeclaration(node.getAST(),
							(SimpleName) astRewrite.createMoveTarget(variableDeclarationFragment.getName()),
							iteratorType);
					astRewrite.remove(variableDeclarationStatement, null);
				}
			}

			if (null == singleVariableDeclaration) {
				// Solution for Iteration over the same List without variables
				String iteratorName = iteratorDefinitionAstVisior.getListName().getFullyQualifiedName()
						+ ReservedNames.CLASS_ITERATOR;
				if(null == multipleIteratorUse.get(iteratorName)){
					multipleIteratorUse.put(iteratorName, 2);
				}
				else{
					Integer i = multipleIteratorUse.get(iteratorName);
					multipleIteratorUse.put(iteratorName, i + 1);
					iteratorName = iteratorName + i;
				}
				
				
				singleVariableDeclaration = NodeBuilder.newSingleVariableDeclaration(node.getAST(),
						NodeBuilder.newSimpleName(node.getAST(),
								iteratorDefinitionAstVisior.getListName().getFullyQualifiedName()
										+ ReservedNames.CLASS_ITERATOR),
						iteratorType);
				//if the next call is used only as an ExpressionStatement just remove it.
				if(nextCall.getParent() instanceof ExpressionStatement){
					astRewrite.remove(nextCall.getParent(), null);
				}
				else{
					astRewrite.replace(nextCall,
							NodeBuilder.newSimpleName(node.getAST(),
									iteratorDefinitionAstVisior.getListName().getFullyQualifiedName()
											+ ReservedNames.CLASS_ITERATOR),
							null);
				}
			}

			EnhancedForStatement newFor = NodeBuilder.newEnhancedForStatement(node.getAST(),
					(Statement) astRewrite.createMoveTarget(node.getBody()),
					(Expression) astRewrite.createMoveTarget(iteratorDefinitionAstVisior.getListName()),
					singleVariableDeclaration);
			astRewrite.replace(node, newFor, null);

			astRewrite.remove(iteratorDefinitionAstVisior.getIteratorDeclaration(), null);
			
			//clear the variableIterator if no other loop is present
			if(replaceInformationASTVisitorList.isEmpty()){
				multipleIteratorUse.clear();
			}
		}
	}
}
