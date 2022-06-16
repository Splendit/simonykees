package eu.jsparrow.core.visitor.loop.stream;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import eu.jsparrow.core.markers.common.EnhancedForLoopToStreamEvent;
import eu.jsparrow.core.visitor.lambdaforeach.AbstractLambdaForEachASTVisitor;
import eu.jsparrow.core.visitor.sub.EffectivelyFinalVisitor;
import eu.jsparrow.core.visitor.sub.UnhandledExceptionVisitor;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;

/**
 * An abstract class to be extended by the visitors that convert an
 * {@link EnhancedForStatement} to a stream.
 * 
 * @author Matthias Webhofer, Ardit Ymeri
 * @since 2.1.1
 *
 */
public abstract class AbstractEnhancedForLoopToStreamASTVisitor extends AbstractLambdaForEachASTVisitor implements EnhancedForLoopToStreamEvent {

	/**
	 * Checks whether the type binding is a raw type, capture, wildcard or a
	 * parameterized type having any of the above as a parameter.
	 * 
	 * @param typeBinding
	 * @return {@code false} if any of the aforementioned types, or {@link true}
	 *         otherwise.
	 */
	protected boolean isTypeSafe(ITypeBinding typeBinding) {
		if (typeBinding.isRawType()) {
			return false;
		}

		if (typeBinding.isCapture()) {
			return false;
		}

		if (typeBinding.isWildcardType()) {
			return false;
		}

		if (typeBinding.isParameterizedType()) {
			for (ITypeBinding argument : typeBinding.getTypeArguments()) {
				if (!isTypeSafe(argument)) {
					return false;
				}
			}
		}

		return true;
	}

	protected boolean isConditionalExpression(Expression expression) {
		return expression.getNodeType() == ASTNode.CONDITIONAL_EXPRESSION;
	}

	/**
	 * creates a copy target for the expression on the left of the stream()
	 * method invocation. if the expression itself is a cast expression, then it
	 * will be wrapped in a parenthesized expression.
	 * 
	 * @param expression
	 *            the expression, which will be on the left of the stream method
	 *            invocation
	 * @return a copy target of the given expression, or a parenthesized
	 *         expression (if expression is of type CastExpression.
	 */
	protected Expression createExpressionForStreamMethodInvocation(Expression expression) {
		Expression expressionCopy = (Expression) astRewrite.createCopyTarget(expression);
		if (expression.getNodeType() == ASTNode.CAST_EXPRESSION) {
			ParenthesizedExpression parenthesizedExpression = astRewrite.getAST()
				.newParenthesizedExpression();
			parenthesizedExpression.setExpression(expressionCopy);
			return parenthesizedExpression;
		}
		return expressionCopy;
	}

	/**
	 * Checks whether the given expression contains a method invocation which
	 * throws an unchecked exception. Makes use of
	 * {@link UnhandledExceptionVisitor}.
	 * 
	 * @param expression
	 *            the expression to be checked
	 * 
	 * @return {@code true} if an invocation which throws an exception is found,
	 *         or {@code false} otherwise.
	 */
	protected boolean throwsException(ASTNode expression) {
		UnhandledExceptionVisitor visitor = new UnhandledExceptionVisitor(expression);
		expression.accept(visitor);
		return visitor.containsUnhandledException();
	}

	/**
	 * Checks whether a reference of a non-final or non-effectively final
	 * variable is made on the code represented by the given node. Makes use of
	 * {@link EffectivelyFinalVisitor}.
	 * 
	 * @param astNode
	 *            a node representing a code snippet.
	 * @return {@code true} if the code references an non effectively final
	 *         variable or {@code false} otherwise.
	 */
	protected boolean containsNonEffectivelyFinalVariable(ASTNode astNode) {
		EffectivelyFinalVisitor analyzer = new EffectivelyFinalVisitor();
		astNode.accept(analyzer);
		return analyzer.containsNonEffectivelyFinalVariable();
	}

	/**
	 * Replaces an {@link EnhancedForStatement} with a
	 * {@link VariableDeclarationStatement} containing a single
	 * {@link VariableDeclarationFragment}. The rest of the declaration
	 * fragments which may occur in the same statement as the given declaration
	 * fragment, are not changed.
	 * 
	 * @param enhancedForStatement
	 *            the statement to be replaced
	 * @param declFragment
	 *            the fragment of the declaration statement to be used as a
	 *            replacer.
	 * @param type
	 *            the type of the declaration fragment
	 */
	protected void replaceLoopWithFragment(EnhancedForStatement enhancedForStatement,
			VariableDeclarationFragment declFragment) {
		ASTNode fragmentParent = declFragment.getParent();
		if (ASTNode.VARIABLE_DECLARATION_STATEMENT == fragmentParent.getNodeType()) {
			VariableDeclarationStatement declStatement = (VariableDeclarationStatement) fragmentParent;
			List<VariableDeclarationFragment> fragments = ASTNodeUtil.returnTypedList(declStatement.fragments(),
					VariableDeclarationFragment.class);
			if (fragments.size() == 1) {
				astRewrite.replace(enhancedForStatement, astRewrite.createMoveTarget(declStatement), null);
			} else {
				AST ast = astRewrite.getAST();
				VariableDeclarationStatement newDeclStatement = ast.newVariableDeclarationStatement(
						(VariableDeclarationFragment) astRewrite.createMoveTarget(declFragment));
				newDeclStatement.setType((Type) astRewrite.createCopyTarget(declStatement.getType()));
				astRewrite.replace(enhancedForStatement, newDeclStatement, null);
			}
		}
	}

	/**
	 * Checks whether the given statement is either a block consisting of a
	 * single {@link ReturnStatement} or is a {@link ReturnStatement} itself.
	 * 
	 * @param thenStatement
	 *            the statement to be checked.
	 * 
	 * @return the found {@link ReturnStatement} or {@code null} if the given
	 *         statement doesn't match with afrementioned description.
	 */
	protected ReturnStatement isReturnBlock(Statement thenStatement) {
		List<Statement> thenBody = new ArrayList<>();

		if (ASTNode.BLOCK == thenStatement.getNodeType()) {
			thenBody = ASTNodeUtil.convertToTypedList(((Block) thenStatement).statements(), Statement.class);
		} else if (ASTNode.RETURN_STATEMENT == thenStatement.getNodeType()) {
			thenBody.add(thenStatement);
		}

		if (thenBody.size() == 1) {
			Statement stStatement = thenBody.get(0);
			if (ASTNode.RETURN_STATEMENT == stStatement.getNodeType()) {
				return (ReturnStatement) stStatement;
			}
		}
		return null;
	}

	/**
	 * Checks whether the given {@link EnhancedForStatement} is immediately
	 * followed by a {@link ReturnStatement}.
	 * 
	 * @param forLoop
	 *            the loop to be checked
	 * @return the found {@link ReturnStatement} or {@code null} otherwise.
	 */
	protected ReturnStatement isFollowedByReturnStatement(EnhancedForStatement forLoop) {
		ASTNode forNodeParent = forLoop.getParent();
		if (ASTNode.BLOCK == forNodeParent.getNodeType()) {
			Block parentBlock = (Block) forNodeParent;
			List<Statement> statements = ASTNodeUtil.returnTypedList(parentBlock.statements(), Statement.class);
			for (int i = 0; i < statements.size(); i++) {
				Statement statement = statements.get(i);
				if (statement == forLoop && i + 1 < statements.size()) {
					Statement nextStatement = statements.get(i + 1);
					if (ASTNode.RETURN_STATEMENT == nextStatement.getNodeType()) {
						return (ReturnStatement) nextStatement;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Checks whether the given statement is a {@link Block} consisting exactly
	 * of an {@link Assignment} and a {@link BreakStatement}.
	 * 
	 * @param thenStatement
	 *            the statement to be checked.
	 * 
	 * @return the found {@link Assignment} or {@code null} otherwise.
	 */
	protected Assignment isAssignmentAndBreak(Statement thenStatement) {
		if (ASTNode.BLOCK == thenStatement.getNodeType()) {
			List<Statement> thenBody = ASTNodeUtil.convertToTypedList(((Block) thenStatement).statements(),
					Statement.class);
			if (thenBody.size() == 2) {
				Statement stStatement = thenBody.get(0);
				Statement ndStatement = thenBody.get(1);
				if (ASTNode.BREAK_STATEMENT == ndStatement.getNodeType()
						&& ASTNode.EXPRESSION_STATEMENT == stStatement.getNodeType()) {
					ExpressionStatement expressionStatement = (ExpressionStatement) stStatement;
					if (ASTNode.ASSIGNMENT == expressionStatement.getExpression()
						.getNodeType()) {
						return (Assignment) expressionStatement.getExpression();
					}
				}
			}
		}
		return null;
	}

	/**
	 * Checks whether the given {@link EnhancedForStatement} is only used for
	 * computing a value after a certain condition is met, and then it is
	 * interrupted.
	 * 
	 * @param forLoop
	 *            loop to be analyzed
	 * 
	 * @return the {@link IfStatement} that represents the condition which has
	 *         to be met before computing the value and interrupting the loop,
	 *         or {@code null} if the loop does not comply with the
	 *         aforementioned description.
	 */
	protected IfStatement isConvertableInterruptedLoop(EnhancedForStatement forLoop) {
		Expression loopExpression = forLoop.getExpression();
		SingleVariableDeclaration loopParameter = forLoop.getParameter();
		ITypeBinding expressionBinding = loopExpression.resolveTypeBinding();
		List<String> expressionBindingList = Collections.singletonList(Collection.class.getName());
		// the expression of the loop should be a subtype of a collection
		if (expressionBinding == null
				|| (!ClassRelationUtil.isInheritingContentOfTypes(expressionBinding, expressionBindingList)
						&& !ClassRelationUtil.isContentOfTypes(expressionBinding, expressionBindingList))) {
			return null;
		}

		ITypeBinding parameterTypeBinding = loopParameter.getType()
			.resolveBinding();
		if (!isTypeSafe(parameterTypeBinding) || !isTypeSafe(expressionBinding)) {
			return null;
		}

		List<Statement> bodyStatements = new ArrayList<>();

		/*
		 * the body of the loop should either be a block or a single if
		 * statement
		 */
		Statement body = forLoop.getBody();
		if (ASTNode.BLOCK == body.getNodeType()) {
			bodyStatements = ASTNodeUtil.returnTypedList(((Block) body).statements(), Statement.class);
		} else if (ASTNode.IF_STATEMENT == body.getNodeType()) {
			bodyStatements.add(body);
		} else {
			return null;
		}

		// the loop body should consist of only one 'if' statement.
		if (bodyStatements.size() != 1) {
			return null;
		}

		Statement statement = bodyStatements.get(0);
		if (ASTNode.IF_STATEMENT != statement.getNodeType()) {
			return null;
		}

		IfStatement ifStatement = (IfStatement) statement;

		// the if statement should have no else branch
		if (ifStatement.getElseStatement() != null) {
			return null;
		}

		/*
		 * the if statement should not contain non effectively final variables
		 * and should not throw any exception
		 */
		if (containsNonEffectivelyFinalVariable(ifStatement.getExpression()) || throwsException(ifStatement)) {
			return null;
		}

		return ifStatement;
	}

	/**
	 * Checks whether the body of the given loop consists of a single statement.
	 * Considers the case where the body is a block with a single statement or a
	 * single body statement which is not being enclosed in curly brackets.
	 * 
	 * @param loopNode
	 *            the loop to be checked
	 * @return an optional of the single body statement or an empty optional if
	 *         the body doesn't consist of a single statement.
	 */
	protected Optional<ExpressionStatement> getSingleBodyStatement(EnhancedForStatement loopNode) {
		Statement loopBody = loopNode.getBody();
		if (ASTNode.BLOCK == loopBody.getNodeType()) {
			Block blockBody = (Block) loopBody;
			List<Statement> statemetns = ASTNodeUtil.convertToTypedList(blockBody.statements(), Statement.class);
			if (statemetns.size() == 1) {
				Statement singleStatement = statemetns.get(0);
				if (singleStatement.getNodeType() == ASTNode.EXPRESSION_STATEMENT) {
					return Optional.of((ExpressionStatement) singleStatement);
				}
			}
		} else if (ASTNode.EXPRESSION_STATEMENT == loopBody.getNodeType()) {
			return Optional.of((ExpressionStatement) loopBody);
		}
		return Optional.empty();
	}

	/**
	 * Checks whether the given expression statement represents an expression of
	 * either of the forms:
	 * 
	 * <ul>
	 * <li>{@code sum = sum + parameter;}</li>
	 * <li>or {@code sum += parameter;}</li>
	 * </ul>
	 * 
	 * @param parameter
	 *            a node representing one operand
	 * @param expressionStatement
	 *            the statement to be checked
	 * @return an optional of the name of the variable which stores the result
	 *         if the expression has the described form, or an empty optional
	 *         otherwise.
	 */
	protected Optional<SimpleName> findResultVariableName(SingleVariableDeclaration parameter,
			ExpressionStatement expressionStatement) {
		Expression expression = expressionStatement.getExpression();
		if (ASTNode.ASSIGNMENT == expression.getNodeType()) {
			Assignment assignment = (Assignment) expression;
			Expression lhs = assignment.getLeftHandSide();
			if (ASTNode.SIMPLE_NAME == lhs.getNodeType()) {
				SimpleName parameterName = parameter.getName();
				SimpleName sumVariableName = (SimpleName) lhs;
				Assignment.Operator assignmetnOperator = assignment.getOperator();
				if (Assignment.Operator.PLUS_ASSIGN.equals(assignmetnOperator)) {
					/*
					 * sum += parameter
					 */
					Expression rhs = assignment.getRightHandSide();
					if (ASTNode.SIMPLE_NAME == rhs.getNodeType()) {
						String rhsIdentifier = ((SimpleName) rhs).getIdentifier();
						if (rhsIdentifier.equals(parameterName.getIdentifier())) {
							return Optional.of(sumVariableName);
						}
					}
				} else if (Assignment.Operator.ASSIGN.equals(assignmetnOperator)) {
					/*
					 * sum = sum + parameter
					 */
					Expression rhs = assignment.getRightHandSide();
					if (ASTNode.INFIX_EXPRESSION == rhs.getNodeType()
							&& isSumOfOperands((InfixExpression) rhs, sumVariableName, parameterName)) {
						return Optional.of(sumVariableName);
					}
				}
			}
		}
		return Optional.empty();
	}

	/**
	 * Checks whether the given expression represents an addition operation of
	 * the given simple names. Considers both cases
	 * {@code sumVariableName + parameterName} and
	 * {@code parameterName + sumVariableName}.
	 * 
	 * @param expression
	 *            the expression to be checked
	 * @param sumVariableName
	 *            expected operand
	 * @param parameterName
	 *            expected operand
	 * @return {@code true} if the expression is an addition of the given
	 *         operands or {@code false} otherwise.
	 */
	protected boolean isSumOfOperands(InfixExpression expression, SimpleName sumVariableName,
			SimpleName parameterName) {
		InfixExpression.Operator operator = expression.getOperator();
		if (!expression.extendedOperands()
			.isEmpty()) {
			/*
			 * There are more than two operands
			 */
			return false;
		}
		if (InfixExpression.Operator.PLUS.equals(operator)) {
			Expression lefOperand = expression.getLeftOperand();
			Expression rightOperand = expression.getRightOperand();

			if ((matches(lefOperand, sumVariableName) && matches(rightOperand, parameterName))
					|| (matches(rightOperand, sumVariableName) && matches(lefOperand, parameterName))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks whether the given expression is a simple having the same
	 * identifier as the given simple name.
	 * 
	 * @param expression
	 *            expression to be checked
	 * @param variableName
	 *            the name to be compared to
	 * @return {@code true} if the identifiers match or {@code false} otherwise
	 */
	protected boolean matches(Expression expression, SimpleName variableName) {
		if (ASTNode.SIMPLE_NAME == expression.getNodeType()) {
			SimpleName operandName = (SimpleName) expression;
			String operandIdentifier = operandName.getIdentifier();
			String variableIdentifier = variableName.getIdentifier();
			return operandIdentifier.equals(variableIdentifier);
		}
		return false;
	}

	/**
	 * Removes the given declaration fragment. If it is the only fragment of the
	 * declaration, then it removes the whole declaration statement.
	 * 
	 * @param declStatement
	 *            the statement containing the declaration fragment.
	 * @param fragment
	 *            the declaration fragment to be removed
	 */
	protected void removeOldSumDeclaration(VariableDeclarationStatement declStatement,
			VariableDeclarationFragment fragment) {
		List<VariableDeclarationFragment> fragmetns = ASTNodeUtil.convertToTypedList(declStatement.fragments(),
				VariableDeclarationFragment.class);
		if (fragmetns.size() == 1) {
			astRewrite.remove(declStatement, null);
			getCommentRewriter().saveRelatedComments(declStatement);
		} else {
			astRewrite.remove(fragment, null);
			getCommentRewriter().saveCommentsInParentStatement(fragment);
		}
	}
}
