package eu.jsparrow.core.visitor.optional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

import eu.jsparrow.core.markers.common.OptionalFilterEvent;
import eu.jsparrow.core.visitor.utils.LambdaNodeUtil;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.visitor.helper.CommentRewriter;
import eu.jsparrow.rules.common.visitor.helper.LocalVariableUsagesVisitor;

/**
 * Extracts an {@link Optional#filter(Predicate)} from the consumer used in
 * {@link Optional#ifPresent(Consumer)}. For example:
 * 
 * <pre>
 * oUser.ifPresent(user -> {
 * 	if (isSpecial(user)) {
 * 		sendMail(user.getMail());
 * 	}
 * });
 * </pre>
 * 
 * is transformed to:
 * 
 * <pre>
 * oUser.filter(user -> isSpecial(user))
 * 	.ifPresent(user -> {
 * 		sendMail(user.getMail());
 * 	});
 * </pre>
 * 
 * This transformation is feasible only if the entire consumer's body is wrapped
 * into an if-statement.
 * 
 * @since 3.14.0
 *
 */
public class OptionalFilterASTVisitor extends AbstractOptionalASTVisitor implements OptionalFilterEvent {

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
		IfStatement singleIfStatement = ASTNodeUtil.findSingleBlockStatement(body, IfStatement.class)
			.orElse(null);
		if (singleIfStatement == null) {
			return true;
		}

		VariableDeclaration variableDeclaration = ASTNodeUtil
			.findSingletonListElement(lambdaExpression.parameters(), VariableDeclaration.class)
			.orElse(null);
		if (variableDeclaration == null) {
			return true;
		}
		SimpleName paramName = variableDeclaration.getName();

		Expression ifExpression = singleIfStatement.getExpression();
		// check if the condition expression is using the lambda parameter
		boolean usesParameter = usesSimpleName(ifExpression, paramName);
		if (!usesParameter) {
			return true;
		}

		// check if there is an else statement
		if (singleIfStatement.getElseStatement() != null) {
			return true;
		}

		// replace
		replace(lambdaExpression, methodInvocation, variableDeclaration, singleIfStatement);
		saveComments(methodInvocation, variableDeclaration, singleIfStatement);

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
		addMarkerEvent(lambdaExpression);
	}

	private boolean usesSimpleName(Expression expression, SimpleName simpleName) {
		LocalVariableUsagesVisitor visitor = new LocalVariableUsagesVisitor(simpleName);
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
