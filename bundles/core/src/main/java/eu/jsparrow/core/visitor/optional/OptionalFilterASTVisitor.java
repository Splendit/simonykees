package eu.jsparrow.core.visitor.optional;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclaration;

import eu.jsparrow.core.visitor.sub.LambdaNodeUtil;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.visitor.helper.CommentRewriter;
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

		replace(lambdaExpression, methodInvocation, variableDeclaration, ifStatement);
		saveComments(methodInvocation, variableDeclaration, ifStatement);
		
		return true;
	}

	private void saveComments(MethodInvocation methodInvocation, VariableDeclaration variableDeclaration,
			IfStatement ifStatement) {
		CommentRewriter commentRewriter = getCommentRewriter();
		List<Comment> droppedComments = new ArrayList<>();
		droppedComments.addAll(commentRewriter.findRelatedComments(methodInvocation.getName()));
		droppedComments.addAll(commentRewriter.findRelatedComments(variableDeclaration));
		droppedComments.addAll(commentRewriter.findSurroundingComments(ifStatement));
		Statement enclosingStatement = ASTNodeUtil.getSpecificAncestor(methodInvocation, Statement.class);
		commentRewriter.saveBeforeStatement(enclosingStatement, droppedComments);
	}

	private void replace(LambdaExpression lambdaExpression, MethodInvocation methodInvocation,
			VariableDeclaration variableDeclaration, IfStatement ifStatement) {
		AST ast = lambdaExpression.getAST();
		VariableDeclaration variableDeclarationCopy = (VariableDeclaration) ASTNode.copySubtree(ast,
				variableDeclaration);
		Expression ifExpression = ifStatement.getExpression();
		LambdaExpression filterLambda = LambdaNodeUtil.createLambdaExpression(astRewrite, variableDeclarationCopy,
				ifExpression);
		Expression optionalExpressionCopy = (Expression) astRewrite.createCopyTarget(methodInvocation.getExpression());
		SimpleName filterName = ast.newSimpleName("filter"); //$NON-NLS-1$
		MethodInvocation filterMethodInvocation = createMethodInvocation(ast, optionalExpressionCopy, filterName,
				filterLambda);
		LambdaExpression ifPresentLambda = LambdaNodeUtil.createLambdaExpression(astRewrite, variableDeclarationCopy,
				ifStatement.getThenStatement());
		SimpleName ifPresentMethodName = ast.newSimpleName("ifPresent"); //$NON-NLS-1$
		MethodInvocation ifPresentMethodInvocation = createMethodInvocation(ast, filterMethodInvocation,
				ifPresentMethodName, ifPresentLambda);

		astRewrite.replace(methodInvocation, ifPresentMethodInvocation, null);
		onRewrite();
	}

	private boolean usesSimpleName(Expression expression, SimpleName simpleName) {
		LocalVariableUsagesASTVisitor visitor = new LocalVariableUsagesASTVisitor(simpleName);
		expression.accept(visitor);
		return !visitor.getUsages()
			.isEmpty();
	}

	/**
	 * Creates a new instance of {@link MethodInvocation} with a single lambda
	 * expression as parameter
	 * 
	 * @param ast
	 *            the ast where the new invocation belongs to
	 * @param methodExpression
	 *            expression for the new method
	 * @param methodName
	 *            new method name
	 * @param methodParam
	 *            new method parameter
	 * @return new method instance.
	 */
	@SuppressWarnings("unchecked")
	private MethodInvocation createMethodInvocation(AST ast, Expression methodExpression, SimpleName methodName,
			LambdaExpression methodParam) {
		MethodInvocation methodInvocation = ast.newMethodInvocation();
		methodInvocation.setExpression(methodExpression);
		methodInvocation.setName(methodName);
		methodInvocation.arguments()
			.add(methodParam);
		return methodInvocation;
	}

}
