package eu.jsparrow.core.visitor.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

public class UseStringBuilderAppendASTVisitor extends AbstractASTRewriteASTVisitor {

	private static final InfixExpression.Operator PLUS = InfixExpression.Operator.PLUS;
	private static final String TO_STRING = "toString"; //$NON-NLS-1$
	private static final String APPEND = "append"; //$NON-NLS-1$

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
		if(operands.size() <= 1) {
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

		return false;
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

		return operands;
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
