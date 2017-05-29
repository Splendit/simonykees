package at.splendit.simonykees.core.visitor.enhancedForLoopToStreamForEach;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.splendit.simonykees.core.util.ASTNodeUtil;
import at.splendit.simonykees.core.util.ClassRelationUtil;
import at.splendit.simonykees.core.visitor.AbstractASTRewriteASTVisitor;

public class EnhancedForLoopToStreamForEachASTVisitor extends AbstractASTRewriteASTVisitor {

	private static final Logger logger = LoggerFactory.getLogger(EnhancedForLoopToStreamForEachASTVisitor.class);

	private static final String COLLECTION_QUALIFIED_NAME = java.util.Collection.class.getName();
	private static final List<String> TYPE_BINDING_CHECK_LIST = Collections.singletonList(COLLECTION_QUALIFIED_NAME);

	private List<SimpleName> fieldNames = new LinkedList<>();

	@Override
	public boolean visit(EnhancedForStatement enhancedForStatementNode) {
		SingleVariableDeclaration parameter = enhancedForStatementNode.getParameter();
		Expression expression = enhancedForStatementNode.getExpression();
		Statement statement = enhancedForStatementNode.getBody();

		// expression must be of type java.util.Collection
		ITypeBinding expressionTypeBinding = expression.resolveTypeBinding();
		if (expressionTypeBinding != null
				&& (ClassRelationUtil.isInheritingContentOfTypes(expressionTypeBinding, TYPE_BINDING_CHECK_LIST)
						|| ClassRelationUtil.isContentOfTypes(expressionTypeBinding, TYPE_BINDING_CHECK_LIST))) { // TODO
																													// probably
			ASTNode approvedStatement = getApprovedStatement(statement);
			if (approvedStatement != null) {

				/*
				 * create method invocation java.util.Collection::stream on the
				 * expression of the enhanced for loop with no parameters
				 */
				Expression expressionCopy = (Expression) astRewrite.createCopyTarget(expression);
				SimpleName streamMethodName = astRewrite.getAST().newSimpleName("stream"); //$NON-NLS-1$

				MethodInvocation streamMethodInvocation = astRewrite.getAST().newMethodInvocation();
				streamMethodInvocation.setExpression(expressionCopy);
				streamMethodInvocation.setName(streamMethodName);

				/*
				 * create lambda expression, which will be used as the only
				 * parameter of the forEach method. The parameter and the body
				 * of the enhanced for loop will be used for the corresponding
				 * parts of the lambda expression.
				 */
				SingleVariableDeclaration parameterCopy = (SingleVariableDeclaration) astRewrite
						.createCopyTarget(parameter);
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
				forEachMethodInvocation.setExpression(streamMethodInvocation);
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
			} else {
				StringBuffer sb = new StringBuffer();
				sb.append("Unable to transform enhanced for-loop to Stream::forEach\n");
				sb.append(enhancedForStatementNode.toString());
				logger.info(sb.toString());
			}
		}

		return true;
	}

	@Override
	public boolean visit(FieldDeclaration fieldDeclarationNode) {
		List<VariableDeclarationFragment> fragments = ASTNodeUtil.convertToTypedList(fieldDeclarationNode.fragments(),
				VariableDeclarationFragment.class);
		fragments.stream().forEach(fragment -> fieldNames.add(fragment.getName()));
		return true;
	}

	/**
	 * 
	 * @param statement
	 * @return
	 */
	private ASTNode getApprovedStatement(Statement statement) {
		if (statement instanceof Block) {
			Block body = (Block) statement;

			StreamForEachCheckValidStatementASTVisitor statementVisitor = new StreamForEachCheckValidStatementASTVisitor(
					fieldNames);
			body.accept(statementVisitor);

			if (statementVisitor.isStatementsValid()) {
				return body;
			}
		} else if (statement instanceof ExpressionStatement) {
			if (isStatementValid(statement)) {
				ExpressionStatement body = (ExpressionStatement) statement;
				return body.getExpression();
			}
		}

		return null;
	}

	private boolean isStatementValid(Statement statement) {

		return true;
	}
}
