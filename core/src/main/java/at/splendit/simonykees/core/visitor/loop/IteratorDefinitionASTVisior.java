package at.splendit.simonykees.core.visitor.loop;

import java.util.Iterator;

import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import at.splendit.simonykees.core.constants.ReservedNames;
import at.splendit.simonykees.core.util.ASTNodeUtil;
import at.splendit.simonykees.core.util.ClassRelationUtil;
import at.splendit.simonykees.core.visitor.AbstractASTRewriteASTVisitor;

/**
 * Finds the definition of the given {@link Iterator}
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
class IteratorDefinitionASTVisior extends AbstractASTRewriteASTVisitor {

	// is initialized in constructor and set to null again if condition is
	// broken
	private SimpleName iteratorName;
	private WhileStatement whileStatement;
	private Name listName = null;
	private VariableDeclarationStatement iteratorDeclarationStatement = null;
	private MethodInvocation iteratorNextCall = null;
	private boolean outsideWhile = true;

	public IteratorDefinitionASTVisior(SimpleName iteratorName, WhileStatement whileStatement) {
		this.iteratorName = iteratorName;
		this.whileStatement = whileStatement;
	}
	
	@Override
	public boolean visit(WhileStatement node) {
		if(whileStatement == node){
			outsideWhile = false;
		}
		return true;
	}
	
	@Override
	public void endVisit(WhileStatement node) {
		if(whileStatement == node){
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

	@Override
	public void endVisit(VariableDeclarationStatement node) {
		if (null != iteratorName  && null != listName && null == iteratorDeclarationStatement) {
			iteratorDeclarationStatement = node;
		}
	}

	@Override
	public boolean visit(SimpleName node) {
		if (null != iteratorName && null != iteratorDeclarationStatement && new ASTMatcher().match(node, iteratorName)
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
						&& methodInvocation.getParent() == whileStatement) {
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

	/*@Override
	public boolean visit(MethodInvocation node) {
		if (null != iteratorName && new ASTMatcher().match(iteratorName, node.getExpression())) {
			if ("next".equals(node.getName().getFullyQualifiedName())) { //$NON-NLS-1$
				if (null != iteratorNextCall) {
					setNodesToNull();
					return false;
				}
				iteratorNextCall = node;
				return true;
			} else {
				setNodesToNull();
				return false;
			}
		}
		return true;
	}*/

	public VariableDeclarationStatement getIteratorDeclarationStatement() {
		return iteratorDeclarationStatement;
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

	/**
	 * Sets all remembered tree nodes to null, as an indicator that the
	 * transformation is not allowed
	 */
	public void setNodesToNull() {
		iteratorName = null;
		whileStatement = null;
		listName = null;
		iteratorDeclarationStatement = null;
		iteratorNextCall = null;
	}
}