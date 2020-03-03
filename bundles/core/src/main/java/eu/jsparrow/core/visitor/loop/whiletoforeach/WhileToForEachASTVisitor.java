package eu.jsparrow.core.visitor.loop.whiletoforeach;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.WhileStatement;

import eu.jsparrow.core.visitor.loop.IteratingIndexVisitorFactory;
import eu.jsparrow.core.visitor.loop.LoopToForEachASTVisitor;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.helper.CommentRewriter;

/**
 * While-loops over Iterators that could be expressed with a for-loop are
 * transformed to a equivalent for-loop.
 * 
 * @author Martin Huter, Ardit Ymeri, Hans-Jörg Schrödl
 * @since 0.9.2
 *
 */
public class WhileToForEachASTVisitor extends LoopToForEachASTVisitor<WhileStatement> {

	public WhileToForEachASTVisitor() {
		this.replaceInformationASTVisitorList = new HashMap<>();
		this.multipleIteratorUse = new HashMap<>();
	}

	@Override
	public boolean visit(WhileStatement node) {
		if (isSingleStatementBodyOfOuterLoop(node)) {
			return true;
		}

		// skip loops with empty condition
		Expression loopCondition = node.getExpression();
		if (loopCondition == null) {
			return true;
		}

		SimpleName iteratorName = ASTNodeUtil.replaceableIteratorCondition(loopCondition);

		if (iteratorName != null && ClassRelationUtil.isContentOfTypes(iteratorName.resolveTypeBinding(),
				generateFullyQualifiedNameList(ITERATOR_FULLY_QUALIFIED_NAME))) {
			handleLoopWithIterator(node, iteratorName);
		} else if (ASTNode.INFIX_EXPRESSION == loopCondition.getNodeType()) {
			handleInfixExpression(node, loopCondition);
		}

		return true;
	}

	private void handleInfixExpression(WhileStatement node, Expression loopCondition) {
		// if the condition of the for loop is an infix expression....
		InfixExpression infixExpression = (InfixExpression) loopCondition;
		Expression rhs = infixExpression.getRightOperand();
		Expression lhs = infixExpression.getLeftOperand();

		// if the expression operator is '<' and lhs is a simple name...
		if (InfixExpression.Operator.LESS.equals(infixExpression.getOperator())
				&& ASTNode.SIMPLE_NAME == lhs.getNodeType()) {
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

	@Override
	public void endVisit(WhileStatement node) {
		handleLoopWithIteratorReplacement(node, node.getBody());
	}

	@Override
	protected List<Comment> getHeaderComments(WhileStatement loop) {
		CommentRewriter commRewriter = getCommentRewriter();
		List<Comment> headComments = new ArrayList<>();

		headComments.addAll(commRewriter.findRelatedComments(loop.getExpression()));

		return headComments;
	}
}
