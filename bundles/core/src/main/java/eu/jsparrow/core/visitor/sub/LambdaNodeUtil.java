package eu.jsparrow.core.visitor.sub;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.visitor.helper.CommentRewriter;

/**
 * A helper class for extracting information from {@link LambdaExpression}
 * nodes.
 * 
 * @since 3.13.0
 *
 */
public class LambdaNodeUtil {

	private LambdaNodeUtil() {
		/*
		 * Hide default constructor.
		 */
	}

	/**
	 * Extracts the {@link Type} of the parameter of the lambda expression, if
	 * the given lambda expression has exactly one parameter whose type is given
	 * explicitly. e.g. {@code (User user) -> consume(user)}.
	 * 
	 * @param lambdaExpression
	 *            lambda expression to be checked
	 * 
	 * @return the {@link Type} of the parameter if the lambda expression has
	 *         only one parameter expressed as a
	 *         {@link SingleVariableDeclaration}, or {@code null} the lambda
	 *         expression does not have exactly one parameter or if the
	 *         parameter type is implicit.
	 */
	public static Type extractSingleParameterType(LambdaExpression lambdaExpression) {
		Type parameterType = null;

		List<SingleVariableDeclaration> declarations = ASTNodeUtil.returnTypedList(lambdaExpression.parameters(),
				SingleVariableDeclaration.class);
		if (declarations.size() == 1) {
			SingleVariableDeclaration declaration = declarations.get(0);
			parameterType = declaration.getType();
		}

		return parameterType;
	}

	/**
	 * Inserts the modifier to the parameter of the lambda expression if it has
	 * only one parameter represented with a {@link SingleVariableDeclaration}.
	 * 
	 * @param lambdaExpression
	 *            a node representing a lambda expression
	 * @param modifier
	 *            the modifier to be inserted
	 * @param astRewrite
	 *            rewriter for the lambda expression
	 */
	public static void insertModifier(LambdaExpression lambdaExpression, Modifier modifier, ASTRewrite astRewrite) {
		if (modifier != null) {
			List<SingleVariableDeclaration> params = ASTNodeUtil.convertToTypedList(lambdaExpression.parameters(),
					SingleVariableDeclaration.class);
			if (params.size() == 1) {
				SingleVariableDeclaration param = params.get(0);
				ListRewrite paramRewriter = astRewrite.getListRewrite(param,
						SingleVariableDeclaration.MODIFIERS2_PROPERTY);
				paramRewriter.insertFirst(astRewrite.createCopyTarget(modifier), null);
			}
		}
	}

	/**
	 * Save comments after extracting a map out of a lambda expression body.
	 * 
	 * @param helper
	 *            helper object for rewriting comments
	 * @param analyzer
	 *            analyzer of a lambda expression body
	 * @param parentStatement
	 *            parent statement to save the comments to.
	 */
	public static void saveComments(CommentRewriter helper, LambdaExpressionBodyAnalyzer analyzer,
			Statement parentStatement) {
		helper.saveRelatedComments(analyzer.getMapVariableDeclaration(), parentStatement);
		List<Statement> remainingStatements = analyzer.getRemainingStatements();
		if (remainingStatements.size() == 1 && ASTNode.EXPRESSION_STATEMENT == remainingStatements.get(0)
			.getNodeType()) {
			Statement rs = remainingStatements.get(0);
			List<Comment> rsComments = new ArrayList<>();
			rsComments.addAll(helper.findLeadingComments(rs));
			rsComments.addAll(helper.findTrailingComments(rs));
			helper.saveBeforeStatement(parentStatement, rsComments);
		}
	}

}
