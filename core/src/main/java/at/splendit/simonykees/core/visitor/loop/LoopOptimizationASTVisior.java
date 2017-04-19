package at.splendit.simonykees.core.visitor.loop;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import at.splendit.simonykees.core.Activator;
import at.splendit.simonykees.core.builder.NodeBuilder;
import at.splendit.simonykees.core.constants.ReservedNames;
import at.splendit.simonykees.core.exception.runtime.ITypeNotFoundRuntimeException;
import at.splendit.simonykees.core.util.ASTNodeUtil;
import at.splendit.simonykees.core.util.ClassRelationUtil;
import at.splendit.simonykees.core.visitor.AbstractASTRewriteASTVisitor;

/**
 * Finds the definition of the given {@link Iterator} and it next calls. Handles
 * the replacement of the While or For Loop
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
class LoopOptimizationASTVisior extends AbstractASTRewriteASTVisitor {

	/*
	 * is initialized in constructor and set to null again if condition is
	 * broken
	 */
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
		if (loopStatement == node) {
			outsideWhile = false;
		}
		return true;
	}

	@Override
	public void endVisit(ForStatement node) {
		if (loopStatement == node) {
			outsideWhile = true;
		}
	}

	@Override
	public boolean visit(WhileStatement node) {
		if (loopStatement == node) {
			outsideWhile = false;
		}
		return true;
	}

	@Override
	public void endVisit(WhileStatement node) {
		if (loopStatement == node) {
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
					
					Expression iterableExpression =  nodeInitializer.getExpression();
					ITypeBinding iterableTypeBinding = iterableExpression.resolveTypeBinding();

					try {
						String iterableFullyQualifiedName = Iterable.class.getName();
						// check if iterable object is compatible with java Iterable
						boolean isIterable = ClassRelationUtil.isInheritingContentOfTypes(iterableTypeBinding,
								Collections.singletonList(iterableFullyQualifiedName));
						
						if(isIterable) {
							listName = (Name) iterableExpression;
							return false;
						}

					} catch (Exception e) {
						Activator.log(Status.ERROR, e.getMessage() ,new ITypeNotFoundRuntimeException());
					}
				}
			}
		}
		
		return true;
	}

	/**
	 *  While Definition
	 */
	@Override
	public void endVisit(VariableDeclarationStatement node) {
		if (preconditionForVariableDeclaration(node.fragments())) {

			iteratorDeclaration = node;
		}
	}

	/**
	 *  For Definition
	 */
	@Override
	public void endVisit(VariableDeclarationExpression node) {
		if (preconditionForVariableDeclaration(node.fragments())) {
			iteratorDeclaration = node;
		}
	}

	private boolean preconditionForVariableDeclaration(@SuppressWarnings("rawtypes") List list) {
		return null != iteratorName && null != listName && null == iteratorDeclaration && 1 == list.size();
	}

	@Override
	public boolean visit(SimpleName node) {
		if (null != iteratorName && null != iteratorDeclaration && new ASTMatcher().match(node, iteratorName)
				&& MethodInvocation.NAME_PROPERTY != node.getLocationInParent()) {

			if (outsideWhile) {
				// iterator used out of the scope of its while loop
				setNodesToNull();
				return false;
			}

			if (MethodInvocation.EXPRESSION_PROPERTY == node.getLocationInParent()) {
				MethodInvocation methodInvocation = (MethodInvocation) node.getParent();
				if (ReservedNames.MI_NEXT.equals(methodInvocation.getName().getFullyQualifiedName())) {
					// next was already called on this iterator
					if (null != iteratorNextCall) {
						setNodesToNull();
						return false;
					}
					iteratorNextCall = methodInvocation;
					return true;
				} else if (ReservedNames.MI_HAS_NEXT.equals(methodInvocation.getName().getFullyQualifiedName())
						&& methodInvocation.getParent() == loopStatement) {
					// allowed hasNext in while head
					return true;
				} else {
					// other not allowed iterator access
					setNodesToNull();
					return false;
				}
			} else {
				// other not allowed iterator access
				setNodesToNull();
				return false;
			}
		}
		return true;
	}

	public void replaceLoop(Statement loopStatement, Statement loopBody, Map<String, Integer> multipleIteratorUse) {
		Type iteratorType = ASTNodeUtil.getSingleTypeParameterOfVariableDeclaration(getIteratorDeclaration());

		/*
		 * iterator has no type-parameter therefore a optimization is could not
		 * be applied
		 */
		if (null == iteratorType) {
			return;
		} else {
			iteratorType = (Type) astRewrite.createMoveTarget(iteratorType);
		}

		// find LoopvariableName
		MethodInvocation nextCall = getIteratorNextCall();
		SingleVariableDeclaration singleVariableDeclaration = null;
		if (nextCall.getParent() instanceof SingleVariableDeclaration) {
			singleVariableDeclaration = (SingleVariableDeclaration) astRewrite.createMoveTarget(nextCall.getParent());
			astRewrite.remove(nextCall.getParent(), null);
		} else if (nextCall.getParent() instanceof VariableDeclarationFragment
				&& nextCall.getParent().getParent() instanceof VariableDeclarationStatement) {
			VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) nextCall
					.getParent();
			VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) variableDeclarationFragment
					.getParent();
			if (1 == variableDeclarationStatement.fragments().size()) {
				singleVariableDeclaration = NodeBuilder.newSingleVariableDeclaration(loopBody.getAST(),
						(SimpleName) astRewrite.createMoveTarget(variableDeclarationFragment.getName()), iteratorType);
				astRewrite.remove(variableDeclarationStatement, null);
			}
		}

		if (null == singleVariableDeclaration) {
			// Solution for Iteration over the same List without variables
			String iteratorName = getListName().getFullyQualifiedName() + ReservedNames.CLASS_ITERATOR;
			if (null == multipleIteratorUse.get(iteratorName)) {
				multipleIteratorUse.put(iteratorName, 2);
			} else {
				Integer i = multipleIteratorUse.get(iteratorName);
				multipleIteratorUse.put(iteratorName, i + 1);
				iteratorName = iteratorName + i;
			}

			singleVariableDeclaration = NodeBuilder.newSingleVariableDeclaration(loopBody.getAST(),
					NodeBuilder.newSimpleName(loopBody.getAST(), iteratorName), iteratorType);
			/*
			 * if the next call is used only as an ExpressionStatement just
			 * remove it.
			 */
			if (nextCall.getParent() instanceof ExpressionStatement) {
				astRewrite.remove(nextCall.getParent(), null);
			} else {
				astRewrite.replace(nextCall, NodeBuilder.newSimpleName(loopBody.getAST(), iteratorName), null);
			}
		}

		EnhancedForStatement newFor = NodeBuilder.newEnhancedForStatement(loopBody.getAST(),
				(Statement) astRewrite.createMoveTarget(loopBody),
				(Expression) astRewrite.createMoveTarget(getListName()), singleVariableDeclaration);
		astRewrite.replace(loopStatement, newFor, null);

		astRewrite.remove(getIteratorDeclaration(), null);
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

	public boolean allParametersFound() {
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