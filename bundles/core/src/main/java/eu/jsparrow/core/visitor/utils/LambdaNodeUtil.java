package eu.jsparrow.core.visitor.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import eu.jsparrow.core.visitor.sub.LambdaExpressionBodyAnalyzer;
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

	/**
	 * Creates a new instance of {@link LambdaExpression} with a single
	 * parameter and the given body
	 * 
	 * @param astRewrite
	 *            a rewriter for creating the copies of the given data
	 * @param parameter
	 *            the only parameter of the new lambda expression
	 * @param body
	 *            the body of the new lambda expression, which must either be an
	 *            {@link Expression} or a {@link Block}
	 * @return the newly created {@link LambdaExpression} or null, if the body
	 *         is not of type {@link Expression}, {@link ExpressionStatement} or
	 *         {@link Block}.
	 */
	public static LambdaExpression createLambdaExpression(ASTRewrite astRewrite, VariableDeclaration parameter,
			ASTNode body) {

		LambdaExpression lambda = astRewrite.getAST()
			.newLambdaExpression();
		lambda.setParentheses(false);
		ListRewrite lambdaParamsListRewrite = astRewrite.getListRewrite(lambda, LambdaExpression.PARAMETERS_PROPERTY);
		lambdaParamsListRewrite.insertFirst(parameter, null);

		if (body.getNodeType() == ASTNode.BLOCK || body instanceof Expression) {
			lambda.setBody(astRewrite.createCopyTarget(body));
			return lambda;
		} else if (body.getNodeType() == ASTNode.EXPRESSION_STATEMENT) {
			Expression expression = ((ExpressionStatement) body).getExpression();
			lambda.setBody(astRewrite.createCopyTarget(expression));
			return lambda;
		}

		return null;
	}

	/**
	 * The context of a lambda can be:
	 * <ul>
	 * <li>an assignment</li>
	 * <li>a declaration fragment</li>
	 * <li>a parameter of a method invocation</li>
	 * <li>a cast expression</li>
	 * <li>an expression of a return statement</li>
	 * <ul>
	 * 
	 * @param lambdaExpression
	 *            the lambda expression to find the context for
	 * @return the {@link ITypeBinding} of the context. In case of a return
	 *         statement, the context type is the return type of the method. In
	 *         case of a method invoctioan parameter, the context type is the
	 *         type of the formal parameter in the corresponding position.
	 *         Otherwise, the context type is obvious.
	 */
	public static Optional<ITypeBinding> findContextType(LambdaExpression lambdaExpression) {
		StructuralPropertyDescriptor locationInParent = lambdaExpression.getLocationInParent();
		ITypeBinding contextTypeBinding = null;
		if (locationInParent == Assignment.RIGHT_HAND_SIDE_PROPERTY) {
			Assignment assignment = (Assignment) lambdaExpression.getParent();
			Expression lhs = assignment.getLeftHandSide();
			contextTypeBinding = lhs.resolveTypeBinding();
		} else if (locationInParent == VariableDeclarationFragment.INITIALIZER_PROPERTY) {
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) lambdaExpression.getParent();
			IVariableBinding variableBinding = fragment.resolveBinding();
			contextTypeBinding = variableBinding.getType();
		} else if (locationInParent == CastExpression.EXPRESSION_PROPERTY) {
			CastExpression cast = (CastExpression) lambdaExpression.getParent();
			contextTypeBinding = cast.resolveTypeBinding();
		} else if (locationInParent == MethodInvocation.ARGUMENTS_PROPERTY) {
			MethodInvocation methodInvocation = (MethodInvocation) lambdaExpression.getParent();
			@SuppressWarnings("unchecked")
			List<Expression> arguments = methodInvocation.arguments();
			int index = arguments.indexOf(lambdaExpression);
			contextTypeBinding = MethodDeclarationUtils.findFormalParameterType(methodInvocation, index).orElse(null);
		} else if (locationInParent == ReturnStatement.EXPRESSION_PROPERTY) {
			ReturnStatement returnStatement = (ReturnStatement) lambdaExpression.getParent();
			contextTypeBinding = MethodDeclarationUtils.findExpectedReturnType(returnStatement);

		}
		return Optional.ofNullable(contextTypeBinding);
	}

	/**
	 * 
	 * @param lambdaParameter
	 *            a {@link VariableDeclaration} of a lambda expression
	 *            parameter.
	 * @return the explicit type of a {@link VariableDeclaration} or an empty
	 *         {@link Optional} if the type of a is implicit.
	 */
	public static Optional<Type> findExplicitLambdaParameterType(
			VariableDeclaration lambdaParameter) {
		if (lambdaParameter.getNodeType() == ASTNode.SINGLE_VARIABLE_DECLARATION) {
			SingleVariableDeclaration declarationWithType = (SingleVariableDeclaration) lambdaParameter;
			Type explicitType = declarationWithType.getType();
			return Optional.of(explicitType);
		}
		return Optional.empty();
	}

}
