package eu.jsparrow.core.visitor.impl;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * This visitor looks for the cases where a string literal is used as a
 * parameter in {@link String#equals(Object)} or
 * {@link String#equalsIgnoreCase(String)}} and swaps the literal with the
 * string expression where the method is called. For example, the following
 * code:
 * 
 * <pre>
 * {@code
 * 		getSomeStringVal().equals("my-val");
 * }
 * </pre>
 * 
 * will be replaced with:
 * 
 * <pre>
 * {@code
 * 		"my-val".equals(getSomeStringVal());
 * }
 * </pre>
 * 
 * Skips the cases where the type of the expression is not {@link String} or if
 * the expression is already a string literal.
 * 
 * @author Ardit Ymeri
 * @since 1.2
 */
public class StringLiteralEqualityCheckASTVisitor extends AbstractASTRewriteASTVisitor {

	private static final String EQUALS = "equals"; //$NON-NLS-1$
	private static final String EQUALS_IGNORE_CASE = "equalsIgnoreCase"; //$NON-NLS-1$
	private List<Comment> comments = new ArrayList<>();

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		comments = ASTNodeUtil.convertToTypedList(compilationUnit.getCommentList(), Comment.class);
		super.visit(compilationUnit);
		return true;
	}

	@Override
	public boolean visit(StringLiteral stringLiteral) {
		// checks if the parenth is a MethodInvoation and the StringLiteral is
		// part of the arguments
		if (stringLiteral.getLocationInParent() == MethodInvocation.ARGUMENTS_PROPERTY) {
			MethodInvocation methodInvocation = (MethodInvocation) stringLiteral.getParent();
			// if the method invocation's name is 'equals' or 'equalsIgnoreCase'
			String methodIdentifier = methodInvocation.getName()
				.getIdentifier();
			if ((EQUALS.equals(methodIdentifier) || EQUALS_IGNORE_CASE.equals(methodIdentifier))
					&& methodInvocation.arguments()
						.size() == 1) {
				Expression expression = methodInvocation.getExpression();
				// if the LHS is not already a literal
				if (expression != null && expression.getNodeType() != ASTNode.STRING_LITERAL) {
					// if the data-type of the expression is String
					ITypeBinding expressionTypeBinding = expression.resolveTypeBinding();
					if (expressionTypeBinding != null) {
						boolean isString = expressionTypeBinding.getQualifiedName()
							.equals(String.class.getName());
						if (isString) {

							/*
							 * Comments may break the code!!! If the expression
							 * is followed by a line comment, when putting it in
							 * the arguments of the equals(), it may comment out
							 * the closing bracket.
							 */
							boolean isCommentFree = isCommentFree(methodInvocation);
							if (isCommentFree) {
								// use the rewriter for swapping
								ASTRewrite astRewrite = getASTRewrite();
								ASTNode newExpression = astRewrite.createCopyTarget(expression);
								ASTNode newArgument = astRewrite.createCopyTarget(stringLiteral);
								astRewrite.replace(expression, newArgument, null);
								astRewrite.replace(stringLiteral, newExpression, null);
								onRewrite();
								addMarkerEvent(stringLiteral, newArgument);
							}
						}
					}
				}
			}
		}
		return true;
	}

	/**
	 * Returns if there is no comment nested with the code represented by the
	 * given {@link ASTNode}. For example, if a {@link MethodInvocation} node
	 * representing the following code is given as a parameter, then this method
	 * returns {@code false}.
	 * 
	 * <pre>
	 * {@code
	 * "val" // line comment
	 * .equals(val);
	 * }
	 * </pre>
	 * 
	 * @param node
	 *            {@link ASTNode} representing a method invocation
	 * @return true if there is no comment nested in the method invocation
	 */
	private boolean isCommentFree(ASTNode node) {
		int startPos = node.getStartPosition();
		int endPos = startPos + node.getLength();
		boolean hasComment = comments.stream()
			.anyMatch(comment -> (comment.getStartPosition() > startPos)
					&& ((comment.getStartPosition() + comment.getLength()) < endPos));
		return !hasComment;

	}
}
