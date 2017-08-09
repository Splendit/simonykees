package at.splendit.simonykees.core.visitor.enhancedForLoopToStreamForEach;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import at.splendit.simonykees.core.util.ASTNodeUtil;
import at.splendit.simonykees.core.util.ClassRelationUtil;
import at.splendit.simonykees.core.visitor.lambdaForEach.AbstractLambdaForEachASTVisitor;

/**
 * An abstract class to be extended by the visitors that convert an
 * {@link EnhancedForStatement} to a stream.
 * 
 * @author Matthias Webhofer, Ardit Ymeri
 * @since 2.0.2
 *
 */
public abstract class AbstractEnhancedForLoopToStreamASTVisitor extends AbstractLambdaForEachASTVisitor {

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
				return isTypeSafe(argument);
			}
		}

		return true;
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
			ParenthesizedExpression parenthesizedExpression = astRewrite.getAST().newParenthesizedExpression();
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
	protected boolean throwsException(Expression expression) {
		UnhandledExceptionVisitor visitor = new UnhandledExceptionVisitor();
		expression.accept(visitor);
		return visitor.throwsException();
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
					if (ASTNode.ASSIGNMENT == expressionStatement.getExpression().getNodeType()) {
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

		ITypeBinding parameterTypeBinding = loopParameter.getType().resolveBinding();
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
		 * the condition expression should not contain non effectively final
		 * variables and should not throw any exception
		 */
		Expression condition = ifStatement.getExpression();
		if (containsNonEffectivelyFinalVariable(condition) || throwsException(condition)) {
			return null;
		}

		return ifStatement;
	}
}
