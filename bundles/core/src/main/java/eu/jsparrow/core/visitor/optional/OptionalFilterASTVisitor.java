package eu.jsparrow.core.visitor.optional;

import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.visitor.helper.LocalVariableUsagesASTVisitor;

public class OptionalFilterASTVisitor extends AbstractOptionalASTVisitor {

	@Override
	public boolean visit(LambdaExpression lambdaExpression) {

		// Check if parent is Optional.ifPresent
		if (lambdaExpression.getLocationInParent() != MethodInvocation.ARGUMENTS_PROPERTY) {
			return true;
		}
		MethodInvocation methodInvocation = (MethodInvocation) lambdaExpression.getParent();

		if (methodInvocation.getExpression() == null) {
			return false;
		}
		boolean isOptionalIfPresent = hasRightTypeAndName(methodInvocation, java.util.Optional.class.getName(),
				IF_PRESENT);
		if (!isOptionalIfPresent) {
			return false;
		}

		// check if lambda body is a single if statement
		ASTNode lambdaBody = lambdaExpression.getBody();
		if (lambdaBody.getNodeType() != ASTNode.BLOCK) {
			return false;
		}

		Block body = (Block) lambdaBody;
		List<Statement> statements = ASTNodeUtil.convertToTypedList(body.statements(), Statement.class);
		if (statements.size() != 1) {
			return true;
		}

		Statement singleStatement = statements.get(0);
		if (singleStatement.getNodeType() != ASTNode.IF_STATEMENT) {
			return true;
		}

		List<VariableDeclaration> lambdaExpressionParams = ASTNodeUtil.convertToTypedList(lambdaExpression.parameters(),
				VariableDeclaration.class);
		if (lambdaExpressionParams.size() != 1) {
			return true;
		}

		VariableDeclaration variableDeclaration = lambdaExpressionParams.get(0);
		SimpleName paramName = variableDeclaration.getName();

		IfStatement ifStatement = (IfStatement) singleStatement;
		Expression ifExpression = ifStatement.getExpression();
		// check if the condition expression is using the lambda parameter
		boolean usesParameter = usesSimpleName(ifExpression, paramName);
		if (!usesParameter) {
			return true;
		}

		// check if there is an else statement
		if (ifStatement.getElseStatement() != null) {
			return true;
		}

		// replace

		/*
		 * create lambda expression for the filter() method
		 */
		AST ast = lambdaExpression.getAST();
		VariableDeclaration variableDeclarationCopy = (VariableDeclaration) ASTNode.copySubtree(ast,
				variableDeclaration);
		LambdaExpression filterLambda = createLambdaExpression(astRewrite, variableDeclarationCopy, ifExpression);

		Expression optionalExpressionCopy = (Expression) astRewrite.createCopyTarget(methodInvocation.getExpression());

		SimpleName filterName = ast.newSimpleName("filter"); //$NON-NLS-1$

		MethodInvocation filterMethodInvocation = createMethodInvocation(ast, optionalExpressionCopy, filterName,
				filterLambda);

		LambdaExpression ifPresentLambda = createLambdaExpression(astRewrite, variableDeclarationCopy,
				ifStatement.getThenStatement());

		SimpleName ifPresentMethodName = ast.newSimpleName("ifPresent"); //$NON-NLS-1$

		MethodInvocation ifPresentMethodInvocation = createMethodInvocation(ast, filterMethodInvocation, ifPresentMethodName,
				ifPresentLambda);

		astRewrite.replace(methodInvocation, ifPresentMethodInvocation, null);
		onRewrite();
		// TODO: save comments

		/*
		 * optional.ifPresent(a -> { if(a) { ... } });
		 * 
		 * optional.filter(a -> makesUseOf(a)).ifPresent(a -> { ... });
		 */

		return true;
	}

	private boolean usesSimpleName(Expression expression, SimpleName simpleName) {
		LocalVariableUsagesASTVisitor visitor = new LocalVariableUsagesASTVisitor(simpleName);
		expression.accept(visitor);
		return !visitor.getUsages()
			.isEmpty();
	}

	/**
	 * creates a new instance of {@link MethodInvocation} with a single lambda
	 * expression as parameter
	 * 
	 * @param methodExpression
	 * @param methodName
	 * @param methodParam
	 * @return the newly created {@link MethodInvocation}
	 */
	@SuppressWarnings("unchecked")
	public static MethodInvocation createMethodInvocation(AST ast, Expression methodExpression, SimpleName methodName,
			LambdaExpression methodParam) {
		MethodInvocation methodInvocation = ast.newMethodInvocation();
		methodInvocation.setExpression(methodExpression);
		methodInvocation.setName(methodName);
		methodInvocation.arguments().add(methodParam);
		return methodInvocation;
	}
	
	/**
	 * creates a new instance of {@link LambdaExpression} with a single
	 * parameter and the given body
	 * 
	 * @param parameter
	 *            the only parameter of the new lambda expression
	 * @param body
	 *            the body of the new lambda expression, which must either be an
	 *            {@link Expression} or a {@link Block}
	 * @return the newly created {@link LambdaExpression} or null, if the body
	 *         is not of type {@link Expression}, {@link ExpressionStatement} or
	 *         {@link Block}.
	 */
	private LambdaExpression createLambdaExpression(ASTRewrite astRewrite, VariableDeclaration parameter, ASTNode body) {

		LambdaExpression lambda = astRewrite.getAST().newLambdaExpression();
		lambda.setParentheses(false);
		ListRewrite lambdaParamsListRewrite = astRewrite.getListRewrite(lambda, LambdaExpression.PARAMETERS_PROPERTY);
		lambdaParamsListRewrite.insertFirst(parameter, null);

		if (body.getNodeType() == ASTNode.BLOCK) {
			lambda.setBody((Block) astRewrite.createCopyTarget(body));
			return lambda;
		} else if (body.getNodeType() == ASTNode.EXPRESSION_STATEMENT) {
			Expression expression = ((ExpressionStatement) body).getExpression();
			lambda.setBody((Expression) astRewrite.createCopyTarget(expression));
			return lambda;
		} else if (body instanceof Expression) {
			lambda.setBody((Expression) astRewrite.createCopyTarget(body));
			return lambda;
		}

		return null;
	}

}
