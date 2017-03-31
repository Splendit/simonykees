package at.splendit.simonykees.core.visitor.loop;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.SimpleName;

import at.splendit.simonykees.core.util.ASTNodeUtil;
import at.splendit.simonykees.core.util.ClassRelationUtil;
import at.splendit.simonykees.core.visitor.AbstractCompilationUnitASTVisitor;

/**
 * For loops with an iterator can be replaced with a forEach loop since 1.7
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
public class ForToForEachASTVisitor extends AbstractCompilationUnitASTVisitor {

	private static final Integer ITERATOR_KEY = 1;
	private static final String ITERATOR_FULLY_QUALLIFIED_NAME = "java.util.Iterator"; //$NON-NLS-1$

	private static final Integer LIST_KEY = 2;
	private static final String LIST_FULLY_QUALLIFIED_NAME = "java.util.List"; //$NON-NLS-1$
	
	private Map<ForStatement, LoopOptimizationASTVisior> replaceInformationASTVisitorList;
	private Map<String, Integer> multipleIteratorUse;

	public ForToForEachASTVisitor() {
		super();
		this.fullyQuallifiedNameMap.put(ITERATOR_KEY, generateFullyQuallifiedNameList(ITERATOR_FULLY_QUALLIFIED_NAME));
		this.fullyQuallifiedNameMap.put(LIST_KEY, generateFullyQuallifiedNameList(LIST_FULLY_QUALLIFIED_NAME));
		this.replaceInformationASTVisitorList = new HashMap<>();
		this.multipleIteratorUse = new HashMap<>();
	}

	public boolean visit(ForStatement node) {
		//Defined updaters are note allowed 
		if(!node.updaters().isEmpty()){
			return true;
		}
		SimpleName iteratorName = ASTNodeUtil.replaceableIteratorCondition(node.getExpression());
		if (iteratorName != null) {
			if (ClassRelationUtil.isContentOfRegistertITypes(iteratorName.resolveTypeBinding(),
					iTypeMap.get(ITERATOR_KEY))) {
				Block parentNode = ASTNodeUtil.getSpecificAncestor(node, Block.class);
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
	
	@Override
	public void endVisit(ForStatement node) {
		// Do the replacement
		if (replaceInformationASTVisitorList.containsKey(node)) {
			LoopOptimizationASTVisior iteratorDefinitionAstVisior = replaceInformationASTVisitorList.remove(node);
			iteratorDefinitionAstVisior.replaceLoop(node, node.getBody(), multipleIteratorUse);
			
			//clear the variableIterator if no other loop is present
			if(replaceInformationASTVisitorList.isEmpty()){
				multipleIteratorUse.clear();
			}
		}
	}
}
