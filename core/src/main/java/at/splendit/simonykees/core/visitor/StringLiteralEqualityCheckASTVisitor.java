package at.splendit.simonykees.core.visitor;

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

import at.splendit.simonykees.core.util.ASTNodeUtil;

/**
 * To avoid NullPointerExceptions , it is recommended to use string 
 * literals in the left-hand-side of equals() or equalsIgnoreCase() when
 *  checking for equality.
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
		return true;
	}

	@Override
	public boolean visit(StringLiteral stringLiteral) {
		ASTNode parent = stringLiteral.getParent();
		if (parent != null && parent.getNodeType() == ASTNode.METHOD_INVOCATION) {
			MethodInvocation methodInvocation = (MethodInvocation) parent;
			// if method invocation name is 'equals' or 'equalsIgnoreCase'
			String methodIdentifier = methodInvocation.getName().getIdentifier();
			if (EQUALS.equals(methodIdentifier) || EQUALS_IGNORE_CASE.equals(methodIdentifier)) {
				Expression expression = methodInvocation.getExpression();
				// if LHS is not already a literal
				if (expression != null && expression.getNodeType() != ASTNode.STRING_LITERAL) {
					// if the data-type of the expression is String
					ITypeBinding expressionTypeBinding = expression.resolveTypeBinding();
					if (expressionTypeBinding != null) {
						boolean isString = expressionTypeBinding.getQualifiedName().equals(String.class.getName());
						if (isString) {

							/**
							 * Comments may break the code!!! If the expression
							 * is followed by a line comment, when putting in in
							 * the arguments of equals, it may comment out the
							 * closing bracket.
							 */
							boolean isCommentFree = isCommentFree(methodInvocation);
							if (isCommentFree) {
								// use the rewriter for swapping
								ASTRewrite astRewrite = getAstRewrite();
								ASTNode newExpression = astRewrite.createCopyTarget(expression);
								ASTNode newArgument = astRewrite.createCopyTarget(stringLiteral);
								astRewrite.replace(expression, newArgument, null);
								astRewrite.replace(stringLiteral, newExpression, null);
							}
						}
					}
				}
			}
		}
		return true;
	}

	/**
	 * Returns if there is no comment nested with the method invocation.
	 * For example:
	 * <pre>
	 * {@code
	 * "val" // line comment
	 * .equals(val);
	 * }
	 * </pre>
	 * 
	 * @param methodInvocation
	 * 			{@link ASTNode} representing a method invocation
	 * @return
	 * 		true if there is no comment nested in the method invocation
	 */
	private boolean isCommentFree(MethodInvocation methodInvocation) {
		int startPos = methodInvocation.getStartPosition();
		int endPos = startPos + methodInvocation.getLength();
		boolean hasComment = comments.stream().filter(comment -> (comment.getStartPosition() > startPos)
				&& ((comment.getStartPosition() + comment.getLength()) < endPos)).findAny().isPresent();
		return !hasComment;

	}
}
