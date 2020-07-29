package eu.jsparrow.core.visitor.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.Statement;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;
import eu.jsparrow.rules.common.visitor.helper.CommentRewriter;

/**
 * Replaces the {@link String} concatenation using the
 * {@link InfixExpression.Operator#PLUS} by {@link StringBuilder#append}. When
 * possible, unwraps the parenthesized expressions.
 * 
 * @since 2.7.0
 *
 */
public class UseStringBuilderAppendASTVisitor extends AbstractASTRewriteASTVisitor {

	private static final InfixExpression.Operator PLUS = InfixExpression.Operator.PLUS;
	private static final String TO_STRING = "toString"; //$NON-NLS-1$
	private static final String APPEND = "append"; //$NON-NLS-1$

	private static final int MIN_OPERANDS = 3;

	/**
	 * If the number of concatenations is too big, the JDT will throw a
	 * StackOverflwoException while creating the chain of append invocations.
	 * For this reason, we limit the number of concatenations to a reasonably
	 * big upper-bound of concatenations. See SIM-1783.Note that 200 is not 
	 * the biggest number of invocations that JDT allows, but this number was 
	 * chosen after experimenting with different values. Whether this value 
	 * is still too high, is an open discussion. 
	 */
	private static final int MAX_OPERANDS = 200;

	@Override
	public boolean visit(NormalAnnotation annotation) {
		return false;
	}

	@Override
	public boolean visit(SingleMemberAnnotation annotation) {
		return false;
	}

	@Override
	public boolean visit(InfixExpression infixExpression) {

		InfixExpression.Operator operator = infixExpression.getOperator();
		if (!PLUS.equals(operator)) {
			return false;
		}

		ITypeBinding typeBinding = infixExpression.resolveTypeBinding();
		if (!ClassRelationUtil.isContentOfTypes(typeBinding,
				Collections.singletonList(java.lang.String.class.getName()))) {
			return false;
		}

		List<Expression> operands = findOperands(infixExpression);
		if (operands.size() < MIN_OPERANDS || operands.size() > MAX_OPERANDS) {
			/*
			 * If there are less than three operands, it does not make much
			 * sense to introduce an instance of a StringBuilder.
			 */
			return false;
		}

		AST ast = infixExpression.getAST();
		MethodInvocation toString = ast.newMethodInvocation();
		toString.setName(ast.newSimpleName(TO_STRING));

		ClassInstanceCreation stringBuilder = ast.newClassInstanceCreation();
		stringBuilder.setType(ast.newSimpleType(ast.newName(StringBuilder.class.getSimpleName())));

		Expression expression = stringBuilder;
		for (Expression operand : operands) {
			MethodInvocation append = createAppendInvocation(operand, ast, expression);
			expression = append;
		}

		toString.setExpression(expression);
		astRewrite.replace(infixExpression, toString, null);
		onRewrite();
		saveComments(infixExpression, operands);

		return false;
	}

	private void saveComments(InfixExpression infixExpression, List<Expression> operands) {
		CommentRewriter commentRewriter = getCommentRewriter();
		List<Comment> comments = commentRewriter.findRelatedComments(infixExpression);
		List<Comment> allreadySaved = operands.stream()
			.flatMap(expression -> commentRewriter.findRelatedComments(expression)
				.stream())
			.collect(Collectors.toList());
		comments.removeAll(allreadySaved);
		Statement parentStatement = ASTNodeUtil.getSpecificAncestor(infixExpression, Statement.class);
		commentRewriter.saveBeforeStatement(parentStatement, comments);
	}

	private List<Expression> findOperands(InfixExpression infixExpression) {

		InfixExpression.Operator operator = infixExpression.getOperator();
		if (!InfixExpression.Operator.PLUS.equals(operator)) {
			return Collections.singletonList(infixExpression);
		}

		if (!isStringExpression(infixExpression)) {
			return Collections.singletonList(infixExpression);
		}

		List<Expression> operands = new ArrayList<>();

		Expression left = infixExpression.getLeftOperand();
		operands.addAll(findIncludedOperands(left));
		Expression right = infixExpression.getRightOperand();
		operands.addAll(findIncludedOperands(right));

		List<Expression> extendedOperands = ASTNodeUtil.convertToTypedList(infixExpression.extendedOperands(),
				Expression.class);

		for (Expression extendedOperand : extendedOperands) {
			operands.addAll(findIncludedOperands(extendedOperand));
		}

		if (containsNullLiteral(operands)) {
			return Collections.singletonList(infixExpression);
		}

		return operands;
	}

	private boolean containsNullLiteral(List<Expression> operands) {
		return operands.stream()
			.anyMatch(expression -> ASTNode.NULL_LITERAL == expression.getNodeType());
	}

	private List<Expression> findIncludedOperands(Expression expression) {
		List<Expression> operands = new ArrayList<>();
		if (ASTNode.INFIX_EXPRESSION == expression.getNodeType()) {
			List<Expression> leftOperands = findOperands((InfixExpression) expression);
			operands.addAll(leftOperands);
		} else if (ASTNode.PARENTHESIZED_EXPRESSION == expression.getNodeType()) {
			ParenthesizedExpression parenthesizedExpression = (ParenthesizedExpression) expression;
			operands.addAll(findIncludedOperands(parenthesizedExpression.getExpression()));
		} else {
			operands.add(expression);
		}
		return operands;
	}

	private boolean isStringExpression(InfixExpression infixExpression) {
		Expression left = infixExpression.getLeftOperand();
		boolean isLeftExpressionString = isStringExpression(left);
		Expression right = infixExpression.getRightOperand();
		boolean isRightExpressionString = isStringExpression(right);
		return isLeftExpressionString || isRightExpressionString;
	}

	private boolean isStringExpression(Expression expression) {
		if (expression.getNodeType() == ASTNode.PARENTHESIZED_EXPRESSION) {
			return isStringExpression(((ParenthesizedExpression) expression).getExpression());
		}

		if (expression.getNodeType() == ASTNode.INFIX_EXPRESSION) {
			return isStringExpression((InfixExpression) expression);
		}

		ITypeBinding typeBinding = expression.resolveTypeBinding();
		return ClassRelationUtil.isContentOfTypes(typeBinding,
				Collections.singletonList(java.lang.String.class.getName()));
	}

	@SuppressWarnings("unchecked")
	private MethodInvocation createAppendInvocation(Expression operand, AST ast, Expression expression) {
		MethodInvocation append = ast.newMethodInvocation();
		append.setName(ast.newSimpleName(APPEND));
		append.arguments()
			.add(astRewrite.createCopyTarget(operand));
		append.setExpression(expression);
		return append;
	}
}
