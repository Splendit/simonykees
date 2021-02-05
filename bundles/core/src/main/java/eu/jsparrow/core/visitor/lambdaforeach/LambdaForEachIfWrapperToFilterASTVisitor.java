package eu.jsparrow.core.visitor.lambdaforeach;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import eu.jsparrow.core.visitor.utils.LambdaNodeUtil;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.visitor.helper.CommentRewriter;
import eu.jsparrow.rules.common.visitor.helper.LocalVariableUsagesVisitor;

/**
 * {@link IfStatement}s, which wrap the whole execution block of a
 * {@link Stream#forEach(Consumer)} method, can be transformed to a call to
 * {@link Stream#filter(Predicate)}
 * 
 * @author Matthias Webhofer
 * @since 1.2
 */
public class LambdaForEachIfWrapperToFilterASTVisitor extends AbstractLambdaForEachASTVisitor {

	@Override
	public boolean visit(MethodInvocation methodInvocationNode) {
		boolean toStreamNeeded = false;

		// only forEach method is interesting
		if (isCollectionForEachInvocation(methodInvocationNode)) {
			toStreamNeeded = true;
		} else if (!isStreamForEachInvocation(methodInvocationNode)) {
			return true;
		}

		// get arguments from forEach method, check for size and type
		List<Expression> methodArgs = ASTNodeUtil.convertToTypedList(methodInvocationNode.arguments(),
				Expression.class);

		if (methodArgs.size() == 1 && methodArgs.get(0) instanceof LambdaExpression) {

			/*
			 * get lambda expression and its parameters and check for size
			 */
			LambdaExpression lambdaExpression = (LambdaExpression) methodArgs.get(0);
			List<VariableDeclaration> lambdaExpressionParams = ASTNodeUtil
				.convertToTypedList(lambdaExpression.parameters(), VariableDeclaration.class);

			// if statement can only be in a block
			if (lambdaExpressionParams.size() == 1 && lambdaExpression.getBody() instanceof Block) {

				Block block = (Block) lambdaExpression.getBody();

				/*
				 * block should contain a single if statement and nothing before
				 * or after it
				 */
				if (block.statements()
					.size() == 1
						&& block.statements()
							.get(0) instanceof IfStatement) {
					IfStatement ifStatement = (IfStatement) block.statements()
						.get(0);
					Expression ifStatementExpression = ifStatement.getExpression();

					VariableDeclaration variableDeclaration = lambdaExpressionParams.get(0);
					SimpleName paramName = variableDeclaration.getName();

					/*
					 * an else statement must not be present and the parameter
					 * passed to the forEach lambda must be used for filtering
					 * in the containing if statement
					 */
					if (isElseStatementNullOrEmpty(ifStatement.getElseStatement())
							&& this.isParameterUsedInExpression(paramName, ifStatementExpression)) {

						/*
						 * create lambda expression for the filter() method
						 */
						VariableDeclaration variableDeclarationCopy = (VariableDeclaration) ASTNode
							.copySubtree(astRewrite.getAST(), variableDeclaration);

						LambdaExpression filterLambda = LambdaNodeUtil.createLambdaExpression(astRewrite,
								variableDeclarationCopy, ifStatementExpression);

						/*
						 * create filter() method invocation with filter lambda
						 * as argument
						 */
						Expression streamExpressionCopy = (Expression) astRewrite
							.createCopyTarget(methodInvocationNode.getExpression());

						if (toStreamNeeded) {
							SimpleName streamName = astRewrite.getAST()
								.newSimpleName(STREAM);
							MethodInvocation streamMethodInvocation = astRewrite.getAST()
								.newMethodInvocation();
							streamMethodInvocation.setName(streamName);
							streamMethodInvocation.setExpression(streamExpressionCopy);
							streamExpressionCopy = streamMethodInvocation;
						}

						SimpleName filterName = astRewrite.getAST()
							.newSimpleName("filter"); //$NON-NLS-1$

						MethodInvocation filterMethodInvocation = createMethodInvocation(streamExpressionCopy,
								filterName, filterLambda);

						/*
						 * create lambda expression for the new forEach() method
						 */

						LambdaExpression forEachLambda = LambdaNodeUtil.createLambdaExpression(astRewrite,
								variableDeclarationCopy, ifStatement.getThenStatement());

						if (forEachLambda != null) {
							/*
							 * create new forEach() method with forEach lambda
							 * as argument
							 */
							SimpleName forEachMethodName = astRewrite.getAST()
								.newSimpleName("forEach"); //$NON-NLS-1$
							MethodInvocation forEachMethodInvocation = createMethodInvocation(filterMethodInvocation,
									forEachMethodName, forEachLambda);

							// rewrite the AST
							astRewrite.replace(methodInvocationNode, forEachMethodInvocation, null);
							saveComments(methodInvocationNode, lambdaExpression, block, ifStatement);
							onRewrite();
						}
					}
				}
			}

		}

		return true;
	}

	protected void saveComments(MethodInvocation methodInvocationNode, LambdaExpression lambdaExpression, Block block,
			IfStatement ifStatement) {
		CommentRewriter helper = getCommentRewriter();
		List<Comment> comments = findSurroundingComments(lambdaExpression);
		comments.addAll(helper.findLeadingComments(block));
		comments.addAll(findSurroundingComments(ifStatement));
		comments.addAll(helper.findTrailingComments(block));
		helper.saveBeforeStatement(ASTNodeUtil.getSpecificAncestor(methodInvocationNode, Statement.class), comments);
	}

	private List<Comment> findSurroundingComments(ASTNode node) {
		CommentRewriter helper = getCommentRewriter();
		return helper.findSurroundingComments(node)
			.stream()
			.filter(comment -> !helper.isTrailing(comment, node))
			.collect(Collectors.toList());
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
	private MethodInvocation createMethodInvocation(Expression methodExpression, SimpleName methodName,
			LambdaExpression methodParam) {
		MethodInvocation methodInvocation = astRewrite.getAST()
			.newMethodInvocation();
		methodInvocation.setExpression(methodExpression);
		methodInvocation.setName(methodName);
		ListRewrite forEachMethodArgsListRewrite = astRewrite.getListRewrite(methodInvocation,
				MethodInvocation.ARGUMENTS_PROPERTY);
		forEachMethodArgsListRewrite.insertFirst(methodParam, null);
		return methodInvocation;
	}

	/**
	 * checks, if a {@link SimpleName} is used in the specified
	 * {@link Expression}
	 * 
	 * @param parameter
	 * @param expression
	 * @return true, if the {@link SimpleName} is used in the
	 *         {@link Expression}, false otherwise
	 */
	private boolean isParameterUsedInExpression(SimpleName parameter, Expression expression) {
		LocalVariableUsagesVisitor visitor = new LocalVariableUsagesVisitor(parameter);
		expression.accept(visitor);
		return !visitor.getUsages()
			.isEmpty();
	}

	/**
	 * checks if the given else statement is null or empty, where empty means
	 * either one empty statement or a block, which only contains empty
	 * statements
	 * 
	 * @param elseStatement
	 * @return true, if the else statement is null or empty, false otherwise
	 */
	private boolean isElseStatementNullOrEmpty(Statement elseStatement) {
		if (elseStatement == null) {
			return true;
		} else {
			if (elseStatement instanceof Block) {
				Block elseStatementBlock = (Block) elseStatement;
				List<Statement> statements = ASTNodeUtil.convertToTypedList(elseStatementBlock.statements(),
						Statement.class);
				boolean onlyEmptyStatementsInBlock = statements.stream()
					.filter(statement -> !(statement instanceof EmptyStatement))
					.findFirst()
					.map(statement -> false)
					.orElse(true);
				return onlyEmptyStatementsInBlock;
			} else if (elseStatement instanceof EmptyStatement) {
				return true;
			}
		}
		return false;
	}
}
