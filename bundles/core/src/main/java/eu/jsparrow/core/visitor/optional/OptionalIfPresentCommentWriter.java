package eu.jsparrow.core.visitor.optional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;

import eu.jsparrow.rules.common.visitor.helper.CommentRewriter;

/**
 * A class for managing the {@link Comment}s after refactoring a
 * {@link java.util.Optional#isPresent} to a {@link java.util.Optinal#ifPresent}
 * 
 * @since 2.6.0
 *
 */
public class OptionalIfPresentCommentWriter {

	private CommentRewriter commentRewriter;

	public OptionalIfPresentCommentWriter(CommentRewriter commentRewriter) {
		this.commentRewriter = commentRewriter;
	}

	public void saveComments(MethodInvocation methodInvocation, IfStatement ifStatement, ASTNode lambdaBody,
			List<ASTNode> removedNodes) {

		// Remember to save comments at each step
		if (lambdaBody instanceof Expression) {
			List<Comment> lostComments = findRelatedSingleBodyExpressionComments(ifStatement, lambdaBody);
			commentRewriter.saveBeforeStatement(ifStatement, lostComments);
		} else {
			commentRewriter.saveLeadingComment(ifStatement);

			// comments occurring between if keyword and the condition
			List<Comment> ifKeywordRelatedComments = findIfKeywordRelatedComments(ifStatement);
			commentRewriter.saveBeforeStatement(ifStatement, ifKeywordRelatedComments);

			// comments occurring in optional.isPresent() invocation
			List<Comment> conditionComments = findConditionComments(methodInvocation);
			commentRewriter.saveBeforeStatement(ifStatement, conditionComments);

			// comments occurring in the removed nodes
			List<Comment> removedComments = removedNodes.stream()
				.map(commentRewriter::findInternalComments)
				.flatMap(List::stream)
				.collect(Collectors.toList());

			commentRewriter.saveBeforeStatement(ifStatement, removedComments);

		}
	}

	private List<Comment> findRelatedSingleBodyExpressionComments(IfStatement ifStatement, ASTNode lambdaBody) {
		List<Comment> allComments = commentRewriter.findRelatedComments(ifStatement);
		List<Comment> remainingComments = commentRewriter.findRelatedComments(lambdaBody);
		allComments.removeAll(remainingComments);
		return allComments;
	}

	private List<Comment> findConditionComments(MethodInvocation methodInvocation) {
		List<Comment> allConditionComments = commentRewriter.findRelatedComments(methodInvocation);
		Expression expression = methodInvocation.getExpression();
		List<Comment> leadingExpressionComments = commentRewriter.findRelatedComments(expression);
		allConditionComments.removeAll(leadingExpressionComments);

		return allConditionComments;
	}

	private List<Comment> findIfKeywordRelatedComments(IfStatement ifStatement) {
		List<Comment> allComments = commentRewriter.findInternalComments(ifStatement);
		List<Comment> thenStatementComments = commentRewriter.findRelatedComments(ifStatement.getThenStatement());
		List<Comment> expressionComments = commentRewriter.findRelatedComments(ifStatement.getExpression());
		List<Comment> elseExpressionComments = new ArrayList<>();
		Statement elseStatement = ifStatement.getElseStatement();
		if (elseStatement != null) {
			elseExpressionComments.addAll(commentRewriter.findRelatedComments(elseStatement));
		}

		List<Comment> comments = new ArrayList<>();

		comments.addAll(allComments);
		comments.removeAll(thenStatementComments);
		comments.removeAll(expressionComments);
		comments.removeAll(elseExpressionComments);

		return comments;
	}

}
