package at.splendit.simonykees.core.visitor.enhancedForLoopToStreamForEach;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionMethodReference;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import at.splendit.simonykees.core.util.ClassRelationUtil;

/**
 * this rule visits all enhanced for loops and checks if the corresponding loop
 * body is applicable for lambda expressions as parameter for
 * {@link java.util.stream.Stream#forEach(java.util.function.Consumer)}. Each
 * loop body is checked separately by the
 * {@link StreamForEachCheckValidStatementASTVisitor}
 * 
 * @author Matthias Webhofer, Ardit Ymeri
 * @since 1.2
 */
public class EnhancedForLoopToStreamForEachASTVisitor extends AbstractEnhancedForLoopToStreamASTVisitor {

	private static final List<String> TYPE_BINDING_CHECK_LIST = Collections.singletonList(JAVA_UTIL_COLLECTION);
	
	private CompilationUnit compilationUnit;
	
	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		this.compilationUnit = compilationUnit;
		return true;
	}

	@Override
	public void endVisit(EnhancedForStatement enhancedForStatementNode) {
		SingleVariableDeclaration parameter = enhancedForStatementNode.getParameter();
		Type parameterType = parameter.getType();
		Expression expression = enhancedForStatementNode.getExpression();
		Statement statement = enhancedForStatementNode.getBody();
		SimpleName parameterName = parameter.getName();
		ITypeBinding parameterTypeBinding = parameterName.resolveTypeBinding();

		// expression must be of type java.util.Collection
		ITypeBinding expressionTypeBinding = expression.resolveTypeBinding();
		if (expressionTypeBinding != null
				&& (ClassRelationUtil.isInheritingContentOfTypes(expressionTypeBinding, TYPE_BINDING_CHECK_LIST)
						|| ClassRelationUtil.isContentOfTypes(expressionTypeBinding, TYPE_BINDING_CHECK_LIST))
				&& isTypeSafe(parameterTypeBinding)) {

			ASTNode approvedStatement = getApprovedStatement(statement, parameterName);

			if (approvedStatement != null) {

				/*
				 * create method invocation java.util.Collection::stream on the
				 * expression of the enhanced for loop with no parameters
				 */
				Expression expressionCopy = createExpressionForStreamMethodInvocation(expression);
				
				if(parameterType.isPrimitiveType()) {
					MethodInvocation mapToPrimitive = calcMappingMethod((PrimitiveType)parameterType, expressionCopy);
					if(mapToPrimitive != null) {
						expressionCopy = mapToPrimitive;
					}
				}

				/*
				 * create lambda expression, which will be used as the only
				 * parameter of the forEach method. The parameter and the body
				 * of the enhanced for loop will be used for the corresponding
				 * parts of the lambda expression.
				 */
				SimpleName parameterCopy = (SimpleName) astRewrite.createCopyTarget(parameterName);
				ASTNode statementCopy = astRewrite.createCopyTarget(approvedStatement);

				LambdaExpression lambdaExpression = astRewrite.getAST().newLambdaExpression();
				ListRewrite lambdaExpressionParameterListRewrite = astRewrite.getListRewrite(lambdaExpression,
						LambdaExpression.PARAMETERS_PROPERTY);
				lambdaExpressionParameterListRewrite.insertFirst(parameterCopy, null);
				lambdaExpression.setBody(statementCopy);

				/*
				 * create method invocation java.util.stream.Stream::forEach on
				 * the previously created stream method invocation with a single
				 * lambda expression as parameter
				 */
				SimpleName forEachMethodName = astRewrite.getAST().newSimpleName("forEach"); //$NON-NLS-1$

				MethodInvocation forEachMethodInvocation = astRewrite.getAST().newMethodInvocation();
				forEachMethodInvocation.setExpression(expressionCopy);
				forEachMethodInvocation.setName(forEachMethodName);
				ListRewrite forEachMethodInvocationArgumentsListRewrite = astRewrite
						.getListRewrite(forEachMethodInvocation, MethodInvocation.ARGUMENTS_PROPERTY);
				forEachMethodInvocationArgumentsListRewrite.insertFirst(lambdaExpression, null);

				/*
				 * replace enhanced for loop with newly created forEach method
				 * call, wrapped in an expression statement
				 */
				ExpressionStatement expressionStatement = astRewrite.getAST()
						.newExpressionStatement(forEachMethodInvocation);
				astRewrite.replace(enhancedForStatementNode, expressionStatement, null);
			}
		}
	}

	/**
	 * this method starts an instance of
	 * {@link StreamForEachCheckValidStatementASTVisitor} on the loop block and
	 * checks its validity.
	 * 
	 * @param statement
	 *            the body of the enhanced for loop
	 * @param parameter
	 *            the parameter of the enhanced for loop
	 * @return an {@link ASTNode} if the block is valid, null otherwise
	 */
	private ASTNode getApprovedStatement(Statement statement, SimpleName parameter) {
		if (ASTNode.BLOCK == statement.getNodeType()) {
			if (isStatementValid(statement, parameter)) {
				return statement;
			}
		} else if (ASTNode.EXPRESSION_STATEMENT == statement.getNodeType()) {
			if (isStatementValid(statement, parameter)) {
				return ((ExpressionStatement) statement).getExpression();
			}
		}

		return null;
	}

	/**
	 * @see {@link EnhancedForLoopToStreamForEachASTVisitor#getApprovedStatement(Statement, SimpleName)}
	 * 
	 * @param statement
	 * @param parameter
	 * @return
	 */
	private boolean isStatementValid(Statement statement, SimpleName parameter) {
		StreamForEachCheckValidStatementASTVisitor statementVisitor = new StreamForEachCheckValidStatementASTVisitor(
				parameter);
		statement.accept(statementVisitor);
		return statementVisitor.isStatementsValid();
	}
}
