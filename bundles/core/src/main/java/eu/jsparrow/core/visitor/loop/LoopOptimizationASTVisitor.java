package eu.jsparrow.core.visitor.loop;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

import eu.jsparrow.core.constants.ReservedNames;
import eu.jsparrow.rules.common.builder.NodeBuilder;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * Finds the definition of the given {@link Iterator} and it next calls. Handles
 * the replacement of the While or For Loop
 * 
 * @author Martin Huter, Hans-Jörg Schrödl
 * @since 0.9.2
 */
public class LoopOptimizationASTVisitor extends AbstractASTRewriteASTVisitor {

	/*
	 * is initialized in constructor and set to null again if condition is
	 * broken
	 */
	private SimpleName iteratorName;
	private Statement loopStatement;
	private Name listName = null;
	private ASTNode iteratorDeclaration = null;
	private Type iteratorType = null;
	private MethodInvocation iteratorNextCall = null;
	private boolean outsideWhile = true;

	public LoopOptimizationASTVisitor(SimpleName iteratorName, Statement loopStatement) {
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
		if (null != iteratorName && node.getName()
			.getIdentifier()
			.equals(iteratorName.getIdentifier()) && node.getInitializer() instanceof MethodInvocation) {
			MethodInvocation nodeInitializer = (MethodInvocation) node.getInitializer();
			if (ReservedNames.MI_Iterator.equals(nodeInitializer.getName()
				.getFullyQualifiedName()) && nodeInitializer.arguments()
					.isEmpty() && null != nodeInitializer.getExpression()
					&& nodeInitializer.getExpression() instanceof Name) {

				Expression iterableExpression = nodeInitializer.getExpression();
				ITypeBinding iterableTypeBinding = iterableExpression.resolveTypeBinding();

				boolean isRaw = iterableTypeBinding.isRawType();

				String iterableFullyQualifiedName = Iterable.class.getName();
				// check if iterable object is compatible with java Iterable
				boolean isIterable = ClassRelationUtil.isInheritingContentOfTypes(iterableTypeBinding,
						Collections.singletonList(iterableFullyQualifiedName));

				if (isIterable && !isRaw) {
					listName = (Name) iterableExpression;
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * While Definition
	 */
	@Override
	public void endVisit(VariableDeclarationStatement node) {
		if (preconditionForVariableDeclaration(node.fragments())) {

			iteratorDeclaration = node;
			iteratorType = ASTNodeUtil.getSingleTypeParameterOfVariableDeclaration(getIteratorDeclaration());
		}
	}

	/**
	 * For Definition
	 */
	@Override
	public void endVisit(VariableDeclarationExpression node) {
		if (preconditionForVariableDeclaration(node.fragments())) {
			iteratorDeclaration = node;
			iteratorType = ASTNodeUtil.getSingleTypeParameterOfVariableDeclaration(getIteratorDeclaration());
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
				return handleExpressionProperty(node);
			} else {
				// other not allowed iterator access
				setNodesToNull();
				return false;
			}
		}
		return true;
	}

	private boolean handleExpressionProperty(SimpleName node) {
		MethodInvocation methodInvocation = (MethodInvocation) node.getParent();
		if (ReservedNames.MI_NEXT.equals(methodInvocation.getName()
			.getFullyQualifiedName())) {
			// next was already called on this iterator
			if (null != iteratorNextCall) {
				setNodesToNull();
				return false;
			}

			/*
			 * if 'next()' is called in a nested loop, the transformation cannot
			 * be done
			 */
			Statement eclosingLoopStatement = findEnclosingLoopStatement(node);
			if (eclosingLoopStatement != loopStatement) {
				setNodesToNull();
				return false;
			}

			iteratorNextCall = methodInvocation;
			return true;
		} else if (ReservedNames.MI_HAS_NEXT.equals(methodInvocation.getName()
			.getFullyQualifiedName()) && methodInvocation.getParent() == loopStatement) {
			// allowed hasNext in while head
			return true;
		} else {
			// other not allowed iterator access
			setNodesToNull();
			return false;
		}
	}

	/**
	 * Finds if any the enclosing loop statement for the given node.
	 * 
	 * @param node
	 *            a simple name in the body of a loop
	 * @return the closest enclosing loop
	 */
	private Statement findEnclosingLoopStatement(ASTNode node) {

		if (node == null) {
			return null;
		}

		ASTNode parent = node.getParent();

		if (ForStatement.class.isInstance(parent) || WhileStatement.class.isInstance(parent)
				|| EnhancedForStatement.class.isInstance(parent)) {
			return (Statement) node.getParent();
		} else {
			return findEnclosingLoopStatement(parent);
		}
	}

	public boolean replaceLoop(Statement loopStatement, Statement loopBody, Map<String, Integer> multipleIteratorUse,
			String iteratorName) {

		iteratorType = (Type) astRewrite.createMoveTarget(iteratorType);

		// find LoopvariableName
		MethodInvocation nextCall = getIteratorNextCall();
		SingleVariableDeclaration singleVariableDeclaration = null;
		if (nextCall.getParent() instanceof SingleVariableDeclaration) {
			singleVariableDeclaration = (SingleVariableDeclaration) astRewrite.createMoveTarget(nextCall.getParent());
			astRewrite.remove(nextCall.getParent(), null);
		} else if (nextCall.getParent() instanceof VariableDeclarationFragment && nextCall.getParent()
			.getParent() instanceof VariableDeclarationStatement) {
			VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) nextCall
				.getParent();
			VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) variableDeclarationFragment
				.getParent();
			if (1 == variableDeclarationStatement.fragments()
				.size()) {
				singleVariableDeclaration = NodeBuilder.newSingleVariableDeclaration(loopBody.getAST(),
						(SimpleName) astRewrite.createMoveTarget(variableDeclarationFragment.getName()), iteratorType);
				astRewrite.remove(variableDeclarationStatement, null);
			}
		}

		if (null == singleVariableDeclaration) {
			// Solution for Iteration over the same List without variables
			if (null == multipleIteratorUse.get(iteratorName)) {
				multipleIteratorUse.put(iteratorName, 2);
			} else {
				Integer i = multipleIteratorUse.get(iteratorName);
				multipleIteratorUse.put(iteratorName, i + 1);
				iteratorName += i;
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
		iteratorType = null;
		iteratorNextCall = null;
	}

	public boolean allParametersFound() {
		return null != listName && null != iteratorDeclaration && null != iteratorNextCall && null != iteratorType;
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
