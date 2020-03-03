package eu.jsparrow.core.visitor.loop.fortoforeach;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;

import eu.jsparrow.core.visitor.loop.LoopToForEachASTVisitor;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.helper.CommentRewriter;

/**
 * For loops with an iterator can be replaced with a forEach loop since 1.5
 * 
 * @author Martin Huter, Ardit Ymeri
 * @since 0.9.2
 */
public class ForToForEachASTVisitor extends LoopToForEachASTVisitor<ForStatement> {

	public ForToForEachASTVisitor() {
		this.replaceInformationASTVisitorList = new HashMap<>();
		this.multipleIteratorUse = new HashMap<>();
	}

	@Override
	public boolean visit(ForStatement node) {
		if (isSingleStatementBodyOfOuterLoop(node)) {
			return true;
		}

		// skip loops with empty condition
		Expression nodeExpression = node.getExpression();
		if (nodeExpression == null) {
			return true;
		}

		SimpleName iteratorName = ASTNodeUtil.replaceableIteratorCondition(nodeExpression);
		if (iteratorName != null) {
			// Defined updaters are not allowed
			if (!node.updaters()
				.isEmpty()) {
				return true;
			}
			if (ClassRelationUtil.isContentOfTypes(iteratorName.resolveTypeBinding(),
					generateFullyQualifiedNameList(ITERATOR_FULLY_QUALIFIED_NAME))) {
				handleLoopWithIterator(node, iteratorName);
			}

		} else if (ASTNode.INFIX_EXPRESSION == nodeExpression.getNodeType()) {
			// if the condition of the for loop is an infix expression....
			InfixExpression infixExpression = (InfixExpression) nodeExpression;
			Expression rhs = infixExpression.getRightOperand();
			Expression lhs = infixExpression.getLeftOperand();

			// if the expression operator is '<' and lhs is a simple name...
			if (InfixExpression.Operator.LESS.equals(infixExpression.getOperator())
					&& ASTNode.SIMPLE_NAME == lhs.getNodeType()) {
				SimpleName index = (SimpleName) lhs;

				if (ASTNode.METHOD_INVOCATION == rhs.getNodeType()) {
					// iterating over Lists
					MethodInvocation condition = (MethodInvocation) rhs;
					analyzeLoopOverList(node, node.getBody(), condition, index, ForLoopOverListsASTVisitor::new);

				} else if (ASTNode.QUALIFIED_NAME == rhs.getNodeType()) {
					// iterating over arrays
					QualifiedName condition = (QualifiedName) rhs;
					analyzeLoopOverArray(node, node.getBody(), condition, index, ForLoopOverArraysASTVisitor::new);
				}
			}
		}
		return true;
	}

	@Override
	public void endVisit(ForStatement node) {
		handleLoopWithIteratorReplacement(node, node.getBody());
	}

	@Override
	protected List<Comment> getHeaderComments(ForStatement loop) {
		CommentRewriter commRewriter = getCommentRewriter();
		List<Comment> headComments = new ArrayList<>();

		headComments.addAll(commRewriter.findRelatedComments(loop.getExpression()));

		List<Expression> initializers = ASTNodeUtil.convertToTypedList(loop.initializers(), Expression.class);
		headComments.addAll(initializers.stream()
			.flatMap(init -> commRewriter.findRelatedComments(init)
				.stream())
			.collect(Collectors.toList()));

		List<Expression> updaters = ASTNodeUtil.convertToTypedList(loop.updaters(), Expression.class);
		headComments.addAll(updaters.stream()
			.flatMap(updater -> commRewriter.findRelatedComments(updater)
				.stream())
			.collect(Collectors.toList()));

		return headComments;
	}

}