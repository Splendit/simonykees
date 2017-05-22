package at.splendit.simonykees.core.visitor;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import at.splendit.simonykees.core.util.ASTNodeUtil;
import at.splendit.simonykees.core.util.ClassRelationUtil;
import at.splendit.simonykees.core.visitor.sub.LocalVariableUsagesASTVisitor;

/**
 * {@link IfStatement}s, which wrap the whole execution block of a
 * {@link Stream#forEach(Consumer)} method, can be transformed to a call to
 * {@link Stream#filter(Predicate)}
 * 
 * @author Matthias Webhofer
 * @since 1.2
 */
public class LambdaForEachIfWrapperToFilterASTVisitor extends AbstractASTRewriteASTVisitor {

	private static final String STREAM_QUALIFIED_NAME = java.util.stream.Stream.class.getName();

	@Override
	public boolean visit(MethodInvocation methodInvocationNode) {

		// only forEach method is interesting
		if ("forEach".equals(methodInvocationNode.getName().getIdentifier())) { //$NON-NLS-1$

			/*
			 * check if forEach is called on an instance of
			 * java.util.stream.Stream
			 */
			ITypeBinding streamTypeBinding = methodInvocationNode.getExpression().resolveTypeBinding();

			if (ClassRelationUtil.isContentOfTypes(streamTypeBinding.getErasure(),
					Collections.singletonList(STREAM_QUALIFIED_NAME))
					|| ClassRelationUtil.isInheritingContentOfTypes(streamTypeBinding,
							Collections.singletonList(STREAM_QUALIFIED_NAME))) {

				// get arguments from forEach method, check for size and type
				List<Expression> methodArgs = ASTNodeUtil.convertToTypedList(methodInvocationNode.arguments(),
						Expression.class);

				if (methodArgs.size() == 1 && methodArgs.get(0) instanceof LambdaExpression) {

					// get lambda expression and its parameters and check for
					// size
					LambdaExpression lambdaExpression = (LambdaExpression) methodArgs.get(0);
					List<VariableDeclaration> lambdaExpressionParams = ASTNodeUtil
							.convertToTypedList(lambdaExpression.parameters(), VariableDeclaration.class);

					if (lambdaExpressionParams.size() == 1) {

						// if statement can only be in a block
						if (lambdaExpression.getBody() instanceof Block) {
							Block block = (Block) lambdaExpression.getBody();

							/*
							 * block should contain a single if statement and
							 * nothing before or after it
							 */
							if (block.statements().size() == 1 && block.statements().get(0) instanceof IfStatement) {
								IfStatement ifStatement = (IfStatement) block.statements().get(0);
								Expression ifStatementExpression = ifStatement.getExpression();

								VariableDeclaration variableDeclaration = lambdaExpressionParams.get(0);
								SimpleName paramName = variableDeclaration.getName();

								/*
								 * the parameter passed to the forEach lambda
								 * must be used for filtering in the containing
								 * if statement
								 */
								if (this.isParameterUsedInExpression(paramName, ifStatementExpression)) {

									/*
									 * create lambda expression for the filter()
									 * method
									 */
									Expression ifStatementExpressionCopy = (Expression) astRewrite
											.createCopyTarget(ifStatementExpression);
									VariableDeclaration variableDeclarationCopy = (VariableDeclaration) ASTNode
											.copySubtree(astRewrite.getAST(), variableDeclaration);

									LambdaExpression filterLambda = createLambdaExpression(variableDeclarationCopy,
											ifStatementExpressionCopy);

									/*
									 * create filter() method invocation with
									 * filter lambda as argument
									 */
									Expression streamExpressionCopy = (Expression) astRewrite
											.createCopyTarget(methodInvocationNode.getExpression());
									SimpleName filterName = astRewrite.getAST().newSimpleName("filter"); //$NON-NLS-1$

									MethodInvocation filterMethodInvocation = createMethodInvocation(
											streamExpressionCopy, filterName, filterLambda);

									/*
									 * create lambda expression for the new
									 * forEach() method
									 */
									Statement thenStatementCopy = (Statement) astRewrite
											.createCopyTarget(ifStatement.getThenStatement());

									LambdaExpression forEachLambda = createLambdaExpression(variableDeclarationCopy,
											thenStatementCopy);

									/*
									 * create new forEach() method with forEach
									 * lambda as argument
									 */
									SimpleName forEachMethodName = astRewrite.getAST().newSimpleName("forEach"); //$NON-NLS-1$
									MethodInvocation forEachMethodInvocation = createMethodInvocation(
											filterMethodInvocation, forEachMethodName, forEachLambda);

									// rewrite the AST
									astRewrite.replace(methodInvocationNode, forEachMethodInvocation, null);
								}
							}
						}
					}
				}
			}
		}

		return true;
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
	 *         is not of type {@link Expression} or {@link Block}
	 */
	private LambdaExpression createLambdaExpression(VariableDeclaration parameter, ASTNode body) {
		if (body instanceof Expression || body instanceof Block) {
			LambdaExpression lambda = astRewrite.getAST().newLambdaExpression();

			ListRewrite lambdaParamsListRewrite = astRewrite.getListRewrite(lambda,
					LambdaExpression.PARAMETERS_PROPERTY);
			lambdaParamsListRewrite.insertFirst(parameter, null);
			lambda.setBody(body);

			return lambda;
		}

		return null;
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
		MethodInvocation methodInvocation = astRewrite.getAST().newMethodInvocation();
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
		LocalVariableUsagesASTVisitor visitor = new LocalVariableUsagesASTVisitor(parameter);
		expression.accept(visitor);
		return !visitor.getUsages().isEmpty();
	}
}
