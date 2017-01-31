package at.splendit.simonykees.core.visitor.loop;

import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import at.splendit.simonykees.core.constants.ReservedNames;
import at.splendit.simonykees.core.visitor.AbstractASTRewriteASTVisitor;

/**
 * Finds the definition of the given {@link Iterator} and it next calls.
 * Handles the replacement of the While or For Loop
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
class LoopOptimizationASTVisior extends AbstractASTRewriteASTVisitor {

	// is initialized in constructor and set to null again if condition is
	// broken
	private SimpleName iteratorName;
	private Statement loopStatement;
	private Name listName = null;
	private ASTNode iteratorDeclaration = null;
	private MethodInvocation iteratorNextCall = null;
	private boolean outsideWhile = true;

	public LoopOptimizationASTVisior(SimpleName iteratorName, Statement loopStatement) {
		this.iteratorName = iteratorName;
		this.loopStatement = loopStatement;
	}
	
	@Override
	public boolean visit(ForStatement node) {
		if(loopStatement == node){
			outsideWhile = false;
		}
		return true;
	}
	
	@Override
	public void endVisit(ForStatement node) {
		if(loopStatement == node){
			outsideWhile = true;
		}
	}
	
	@Override
	public boolean visit(WhileStatement node) {
		if(loopStatement == node){
			outsideWhile = false;
		}
		return true;
	}
	
	@Override
	public void endVisit(WhileStatement node) {
		if(loopStatement == node){
			outsideWhile = true;
		}
	}

	@Override
	public boolean visit(VariableDeclarationFragment node) {
		if (null != iteratorName && node.getName().getIdentifier().equals(iteratorName.getIdentifier())) {
			if (node.getInitializer() instanceof MethodInvocation) {
				MethodInvocation nodeInitializer = (MethodInvocation) node.getInitializer();
				if (ReservedNames.MI_Iterator.equals(nodeInitializer.getName().getFullyQualifiedName())
						&& nodeInitializer.arguments().isEmpty() && null != nodeInitializer.getExpression()
						&& nodeInitializer.getExpression() instanceof Name) {
					listName = (Name) nodeInitializer.getExpression();
					return false;
				}
			}
		}
		return true;
	}

	//While Definition 
	@Override
	public void endVisit(VariableDeclarationStatement node) {
		if (preconditionForVariableDeclaration(node.fragments())) {
			
			iteratorDeclaration = node;
		}
	}

	//For Definition 
	@Override
	public void endVisit(VariableDeclarationExpression node) {
		if (preconditionForVariableDeclaration(node.fragments())) {
			iteratorDeclaration = node;
		}
	}
	
	private boolean preconditionForVariableDeclaration(@SuppressWarnings("rawtypes") List list){
		return null != iteratorName  && null != listName && null == iteratorDeclaration && 1 == list.size();
	}

	@Override
	public boolean visit(SimpleName node) {
		if (null != iteratorName && null != iteratorDeclaration && new ASTMatcher().match(node, iteratorName)
				&& MethodInvocation.NAME_PROPERTY != node.getLocationInParent()) {
			
			if(outsideWhile){
				//iterator used out of the scope of its while loop
				setNodesToNull();
				return false;
			}
			
			if (MethodInvocation.EXPRESSION_PROPERTY == node.getLocationInParent()) {
				MethodInvocation methodInvocation = (MethodInvocation) node.getParent();
				if (ReservedNames.MI_NEXT.equals(methodInvocation.getName().getFullyQualifiedName())) {
					//next was already called on this iterator
					if (null != iteratorNextCall) {
						setNodesToNull();
						return false;
					}
					iteratorNextCall = methodInvocation;
					return true;
				} else if (ReservedNames.MI_HAS_NEXT.equals(methodInvocation.getName().getFullyQualifiedName())
						&& methodInvocation.getParent() == loopStatement) {
					//allowed hasNext in while head
					return true;
				}else {
					//other not allowed iterator access
					setNodesToNull();
					return false;
				}
			}else {
				//other not allowed iterator access
				setNodesToNull();
				return false;
			}
		}
		return true;
	}
	
	

	/**
	 * Sets all remembered tree nodes to null, as an indicator that the
	 * transformation is not allowed
	 */
	public void setNodesToNull() {
		iteratorName = null;
		loopStatement = null;
		listName = null;
		iteratorDeclaration = null;
		iteratorNextCall = null;
	}
	
	public boolean allParametersFound(){
		return null != listName && null != iteratorDeclaration && null != iteratorNextCall;
	}
	
	public ASTNode getIteratorDeclaration() {
		return iteratorDeclaration;
	}

	public SimpleName getIteratorName() {
		return iteratorName;
	}

	public Name getListName() {
		return listName;
	}

	public MethodInvocation getIteratorNextCall() {
		return iteratorNextCall;
	}
}