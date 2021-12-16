package eu.jsparrow.core.visitor.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;

import eu.jsparrow.core.markers.common.IndexOfToContainsEvent;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * Transforms calls to {@link String#indexOf(String)} and
 * {@link Collection#indexOf(Object)} into calls to
 * {@link String#contains(CharSequence)} and {@link Collection#contains(Object)}
 * respectively.
 * 
 * The transformation will only take place, if the return value is checked and
 * according to the following tables. If the method invocation to indexOf is on
 * the left of the {@link InfixExpression} (i.e. s.indexOf("l") == -1):
 * 
 * <table border="1">
 * <th>Operator</th>
 * <th>Return Value</th>
 * <th>Transformation</th>
 * <tr>
 * <td>==</td>
 * <td>-1</td>
 * <td>!contains</td>
 * </tr>
 * <tr>
 * <td>!=</td>
 * <td>-1</td>
 * <td>contains</td>
 * </tr>
 * <tr>
 * <td>&gt;</td>
 * <td>-1</td>
 * <td>contains</td>
 * </tr>
 * <tr>
 * <td>&gt;=</td>
 * <td>0</td>
 * <td>contains</td>
 * </tr>
 * <tr>
 * <td>&lt;</td>
 * <td>0</td>
 * <td>!contains</td>
 * </tr>
 * <tr>
 * <td>&lt;=</td>
 * <td>-1</td>
 * <td>!contains</td>
 * </tr>
 * </table>
 * 
 * If it is on the right (i.e. -1 == s.indexOf("l")):
 * 
 * <table border="1">
 * <th>Operator</th>
 * <th>Return Value</th>
 * <th>Transformation</th>
 * <tr>
 * <td>==</td>
 * <td>-1</td>
 * <td>!contains</td>
 * </tr>
 * <tr>
 * <td>!=</td>
 * <td>-1</td>
 * <td>contains</td>
 * </tr>
 * <tr>
 * <td>&gt;</td>
 * <td>0</td>
 * <td>!contains</td>
 * </tr>
 * <tr>
 * <td>&gt;=</td>
 * <td>-1</td>
 * <td>!contains</td>
 * </tr>
 * <tr>
 * <td>&lt;</td>
 * <td>-1</td>
 * <td>contains</td>
 * </tr>
 * <tr>
 * <td>&lt;=</td>
 * <td>0</td>
 * <td>contains</td>
 * </tr>
 * </table>
 * 
 * @author Matthias Webhofer
 * @since 2.1.1
 *
 */
public class IndexOfToContainsASTVisitor extends AbstractASTRewriteASTVisitor implements IndexOfToContainsEvent {

	private static final String PRIMITIVE_INT_QUALIFIED_NAME = "int"; //$NON-NLS-1$
	private static final String INTEGER_QUALIFIED_NAME = java.lang.Integer.class.getName();

	private static final String STRING_QUALIFIED_NAME = java.lang.String.class.getName();
	private static final String COLLECTION_QUALIFIED_NAME = java.util.Collection.class.getName();

	private static final List<String> INTEGER_TYPE_BINDING_CHECK_LIST = Arrays.asList(INTEGER_QUALIFIED_NAME,
			PRIMITIVE_INT_QUALIFIED_NAME);
	private static final List<String> STRING_TYPE_BINDING_CHECK_LIST = Collections.singletonList(STRING_QUALIFIED_NAME);
	private static final List<String> COLLECTION_TYPE_BINDING_CHECK_LIST = Collections
		.singletonList(COLLECTION_QUALIFIED_NAME);

	@Override
	public boolean visit(MethodInvocation methodInvocationNode) {
		if ("indexOf".equals(methodInvocationNode.getName() //$NON-NLS-1$
			.getIdentifier())) {
			Expression methodInvocationExpression = methodInvocationNode.getExpression();
			if (methodInvocationExpression != null) {
				ITypeBinding expressionBinding = methodInvocationExpression.resolveTypeBinding();
				if (expressionBinding != null) {
					if (isStringType(methodInvocationExpression)) {
						convertToContains(methodInvocationNode, TransformationType.STRING);
					} else if (isCollectionType(methodInvocationExpression)) {
						convertToContains(methodInvocationNode, TransformationType.COLLECTION);
					}
				}
			}
		}

		return true;
	}

	/**
	 * prepares the transformation from indexOf to contains
	 * 
	 * @param methodInvocationNode
	 * @param type
	 *            {@link TransformationType#STRING} or
	 *            {@link TransformationType#COLLECTION}
	 */
	private void convertToContains(MethodInvocation methodInvocationNode, TransformationType type) {
		ASTNode parentNode = methodInvocationNode.getParent();
		if (parentNode != null && parentNode.getNodeType() == ASTNode.INFIX_EXPRESSION) {
			InfixExpression parent = (InfixExpression) parentNode;
			IndexOfMethodPosition position = this.getPosition(parent);
			TransformationOption option = this.getTransformationOption(parent, position);
			if (option != null) {
				List<Expression> methodArguments = ASTNodeUtil.convertToTypedList(methodInvocationNode.arguments(),
						Expression.class);
				if (methodArguments.size() == 1) {
					Expression methodArgumentExpression = methodArguments.get(0);
					boolean doTransformation = true;

					/*
					 * for strings, the argument of the contains method must be
					 * a string itself. char-Variables or char literals will be
					 * ignored.
					 */
					if (type == TransformationType.STRING && !isStringType(methodArgumentExpression)) {
						doTransformation = false;
					}

					if (doTransformation) {
						this.transform(methodInvocationNode.getExpression(), methodArgumentExpression, parent, option);
						getCommentRewriter().saveRelatedComments(methodInvocationNode.getParent(),
								ASTNodeUtil.getSpecificAncestor(methodInvocationNode, Statement.class));
						onRewrite();
					}
				}
			}

		}
	}

	/**
	 * evaluates whether the method call to indexOf will be replaced by contains
	 * or !contains or if it will be ignored.
	 * 
	 * @param parent
	 * @param position
	 *            {@link #getPosition(InfixExpression)}
	 * @return {@link TransformationOption#CONTAINS},
	 *         {@link TransformationOption#NOT_CONTAINS} or null, if the
	 *         transformation will be ignored.
	 */
	private TransformationOption getTransformationOption(InfixExpression parent, IndexOfMethodPosition position) {
		TransformationOption option = null;

		if (parent != null && position != null) {
			InfixExpression.Operator operator = parent.getOperator();
			Expression comparisonValueExpression = null;

			if (position == IndexOfMethodPosition.LEFT) {
				comparisonValueExpression = parent.getRightOperand();
			} else if (position == IndexOfMethodPosition.RIGHT) {
				comparisonValueExpression = parent.getLeftOperand();
			}

			if (comparisonValueExpression != null) {
				Integer comparisonValue = (Integer) comparisonValueExpression.resolveConstantExpressionValue();

				if (comparisonValue != null) {
					if (comparisonValue == -1) {
						if (InfixExpression.Operator.EQUALS.equals(operator)) {
							option = TransformationOption.NOT_CONTAINS;
						} else if (InfixExpression.Operator.NOT_EQUALS.equals(operator)) {
							option = TransformationOption.CONTAINS;
						} else if (position == IndexOfMethodPosition.LEFT) {
							if (InfixExpression.Operator.GREATER.equals(operator)) {
								option = TransformationOption.CONTAINS;
							} else if (InfixExpression.Operator.LESS_EQUALS.equals(operator)) {
								option = TransformationOption.NOT_CONTAINS;
							}
						} else if (position == IndexOfMethodPosition.RIGHT) {
							if (InfixExpression.Operator.LESS.equals(operator)) {
								option = TransformationOption.CONTAINS;
							} else if (InfixExpression.Operator.GREATER_EQUALS.equals(operator)) {
								option = TransformationOption.NOT_CONTAINS;
							}
						}
					} else if (comparisonValue == 0) {
						if (position == IndexOfMethodPosition.LEFT) {
							if (InfixExpression.Operator.GREATER_EQUALS.equals(operator)) {
								option = TransformationOption.CONTAINS;
							} else if (InfixExpression.Operator.LESS.equals(operator)) {
								option = TransformationOption.NOT_CONTAINS;
							}
						} else if (position == IndexOfMethodPosition.RIGHT) {
							if (InfixExpression.Operator.LESS_EQUALS.equals(operator)) {
								option = TransformationOption.CONTAINS;
							} else if (InfixExpression.Operator.GREATER.equals(operator)) {
								option = TransformationOption.NOT_CONTAINS;
							}
						}
					}
				}
			}
		}

		return option;
	}

	/**
	 * this method does the actual transformation in the AST, as soon as the
	 * preparation is done and there have not been any errors
	 * 
	 * @param leftExpression
	 *            the expression on the left side of the method invocation to
	 *            indexOf
	 * @param methodArgument
	 *            the single argument of the method
	 * @param infixExpression
	 *            the parent infix expression
	 * @param option
	 *            {@link TransformationOption#CONTAINS} or
	 *            {@link TransformationOption#NOT_CONTAINS}
	 */
	@SuppressWarnings("unchecked")
	private void transform(Expression leftExpression, Expression methodArgument, InfixExpression infixExpression,
			TransformationOption option) {
		if (methodArgument != null && infixExpression != null && option != null) {

			Expression copyLeftExpression = (Expression) astRewrite.createMoveTarget(leftExpression);
			Expression copyMethodArgumentExpression = (Expression) astRewrite.createCopyTarget(methodArgument);
			SimpleName containsSimpleName = astRewrite.getAST()
				.newSimpleName("contains"); //$NON-NLS-1$
			MethodInvocation containsMethodInvocation = astRewrite.getAST()
				.newMethodInvocation();
			containsMethodInvocation.setExpression(copyLeftExpression);
			containsMethodInvocation.setName(containsSimpleName);
			containsMethodInvocation.arguments()
				.add(copyMethodArgumentExpression);

			if (option == TransformationOption.NOT_CONTAINS) {
				PrefixExpression notExpression = astRewrite.getAST()
					.newPrefixExpression();
				notExpression.setOperator(PrefixExpression.Operator.NOT);
				notExpression.setOperand(containsMethodInvocation);

				astRewrite.replace(infixExpression, notExpression, null);
				addMarkerEvent(infixExpression, leftExpression, methodArgument, PrefixExpression.Operator.NOT);
			} else {
				astRewrite.replace(infixExpression, containsMethodInvocation, null);
				addMarkerEvent(infixExpression, leftExpression, methodArgument);
			}

		}
	}

	/**
	 * evaluates the position of the method invocation to indexOf within the
	 * parent {@link InfixExpression}.
	 * 
	 * @param infixExpression
	 *            parent
	 * @return {@link IndexOfMethodPosition#LEFT} or
	 *         {@link IndexOfMethodPosition#RIGHT} according to the position in
	 *         the parent. null, if the position could not been determined.
	 */
	private IndexOfMethodPosition getPosition(InfixExpression infixExpression) {
		IndexOfMethodPosition position = null;

		Expression leftOperand = infixExpression.getLeftOperand();
		Expression rightOperand = infixExpression.getRightOperand();

		if (leftOperand.getNodeType() == ASTNode.METHOD_INVOCATION) {
			leftOperand = ((MethodInvocation) leftOperand).getExpression();
			if ((isStringType(leftOperand) || isCollectionType(leftOperand)) && isIntegerType(rightOperand)) {
				position = IndexOfMethodPosition.LEFT;
			}
		} else if (rightOperand.getNodeType() == ASTNode.METHOD_INVOCATION) {
			rightOperand = ((MethodInvocation) rightOperand).getExpression();
			if (isIntegerType(leftOperand) && (isStringType(rightOperand) || isCollectionType(rightOperand))) {
				position = IndexOfMethodPosition.RIGHT;
			}
		}

		return position;
	}

	/**
	 * checks if the type binding of the given expression is of type
	 * {@link Collection}
	 * 
	 * @param expression
	 * @return true, if the expression is of type {@link Collection}, false
	 *         otherwise (and if the expression is null).
	 */
	private boolean isCollectionType(Expression expression) {
		boolean result = false;

		if (expression != null) {
			ITypeBinding expressionTypeBinding = expression.resolveTypeBinding();
			if (expressionTypeBinding != null
					&& ClassRelationUtil.isContentOfTypes(expressionTypeBinding, COLLECTION_TYPE_BINDING_CHECK_LIST)
					|| ClassRelationUtil.isInheritingContentOfTypes(expressionTypeBinding,
							COLLECTION_TYPE_BINDING_CHECK_LIST)) {
				result = true;
			}
		}

		return result;
	}

	/**
	 * checks if the type binding of the given expression is of type
	 * {@link String}
	 * 
	 * @param expression
	 * @return true, if the expression is of type {@link String}, false
	 *         otherwise (and if the expression is null).
	 */
	private boolean isStringType(Expression expression) {
		boolean result = false;

		if (expression != null) {
			ITypeBinding expressionTypeBinding = expression.resolveTypeBinding();
			if (expressionTypeBinding != null
					&& (ClassRelationUtil.isContentOfTypes(expressionTypeBinding, STRING_TYPE_BINDING_CHECK_LIST)
							|| ClassRelationUtil.isInheritingContentOfTypes(expressionTypeBinding,
									STRING_TYPE_BINDING_CHECK_LIST))) {
				result = true;
			}
		}

		return result;
	}

	/**
	 * checks if the type binding of the given expression is of type
	 * {@link Integer}
	 * 
	 * @param expression
	 * @return true, if the expression is of type {@link Integer}, false
	 *         otherwise (and if the expression is null).
	 */
	private boolean isIntegerType(Expression expression) {
		boolean result = false;

		if (expression != null) {
			ITypeBinding expressionType = expression.resolveTypeBinding();
			if (expressionType != null && (ClassRelationUtil.isContentOfTypes(expressionType,
					INTEGER_TYPE_BINDING_CHECK_LIST)
					|| ClassRelationUtil.isInheritingContentOfTypes(expressionType, INTEGER_TYPE_BINDING_CHECK_LIST))) {
				result = true;
			}
		}

		return result;
	}

	private enum TransformationOption {
		CONTAINS,
		NOT_CONTAINS,
	}

	private enum TransformationType {
		STRING,
		COLLECTION,
	}

	private enum IndexOfMethodPosition {
		LEFT,
		RIGHT,
	}
}
