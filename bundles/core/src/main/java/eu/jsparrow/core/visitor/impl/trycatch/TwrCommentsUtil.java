package eu.jsparrow.core.visitor.impl.trycatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TryStatement;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.visitor.helper.CommentRewriter;

/**
 * A utility class for finding the comments on a {@link TryStatement}. As a
 * workaround for manipulating resources in a {@link TryStatement}, we clone the
 * entire statement's body and alter the resources. The comments on the cloned
 * statement are completely lost, for this reason, we have to extract the
 * comments from the original {@link TryStatement} and insert them in the
 * correct positions in the new node. It's a little pain in the head.
 * 
 * @since 3.24.0
 *
 */
public class TwrCommentsUtil {

	private TwrCommentsUtil() {
		/*
		 * Hide default constructor.
		 */
	}

	public static Map<Integer, List<Comment>> findBodyComments(TryStatement node, CommentRewriter commentRewriter) {
		Block body = node.getBody();
		Map<Integer, List<Comment>> bodyComments = new HashMap<>();
		List<Statement> statements = ASTNodeUtil.convertToTypedList(body.statements(), Statement.class);
		List<Comment> connectedComments = new ArrayList<>();
		List<Comment> skippedComments = new ArrayList<>();
		int key = 0;
		for (Statement statement : statements) {
			List<Comment> ithStatementComments = commentRewriter.findRelatedComments(statement);
			if (bodyComments.containsKey(key)) {
				ithStatementComments.addAll(0, bodyComments.get(key));
			}
			if (!ithStatementComments.isEmpty()) {
				bodyComments.put(key, ithStatementComments);
				connectedComments.addAll(ithStatementComments);
			}
			key++;
		}

		/*
		 * comments in the body that are not related with any node.
		 */
		List<Comment> unconnected = commentRewriter.findRelatedComments(body);

		unconnected.removeAll(connectedComments);
		unconnected.removeAll(skippedComments);
		putUnconnectedComments(bodyComments, unconnected);
		return bodyComments;
	}

	/**
	 * Finds the position of the comments that are not connected to any node
	 * based on the position in the compilation unit of the connected and
	 * unconnected body comments.
	 * 
	 * @param bodyComments
	 *            a map of the connected body comments
	 * @param unconnectedComments
	 *            the list of unconnected body comments.
	 */
	public static void putUnconnectedComments(Map<Integer, List<Comment>> bodyComments,
			List<Comment> unconnectedComments) {
		List<Integer> keySet = new ArrayList<>(bodyComments.keySet());
		if (keySet.isEmpty()) {
			return;
		}

		if (keySet.size() == 1) {
			int key = keySet.get(0);
			List<Comment> commentList = bodyComments.get(key);
			commentList.addAll(unconnectedComments);
			unconnectedComments.clear();
			return;
		}
		int index = 0;

		for (Comment comment : unconnectedComments) {
			int startPos = comment.getStartPosition();
			Integer key = keySet.get(index);
			List<Comment> commentList = bodyComments.get(key);
			while (commentList.get(0)
				.getStartPosition() < startPos && index < keySet.size()) {
				index++;
				key = keySet.get(index);
				commentList = bodyComments.get(key);
			}

			if (index >= keySet.size()) {
				break;
			}

			commentList.add(0, comment);
			unconnectedComments.remove(comment);
		}

		if (unconnectedComments.isEmpty()) {
			return;
		}

		Integer newKey = keySet.get(keySet.size() - 1) + 1;
		bodyComments.put(newKey, unconnectedComments);
	}

}
