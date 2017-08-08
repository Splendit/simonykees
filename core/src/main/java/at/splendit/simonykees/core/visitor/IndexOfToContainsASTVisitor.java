package at.splendit.simonykees.core.visitor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.splendit.simonykees.core.util.ASTNodeUtil;
import at.splendit.simonykees.core.util.ClassRelationUtil;

/**
 * 
 * @author Matthias Webhofer
 * @since 2.0.4
 *
 */
public class IndexOfToContainsASTVisitor extends AbstractASTRewriteASTVisitor {

	private static final Logger logger = LoggerFactory.getLogger(IndexOfToContainsASTVisitor.class);

	private static final String PRIMITIVE_INT_QUALIFIED_NAME = "int"; //$NON-NLS-1$
	private static final String INTEGER_QUALIFIED_NAME = java.lang.Integer.class.getName();

	private static final String STRING_QUALIFIED_NAME = java.lang.String.class.getName();
	private static final String COLLECTION_QUALIFIED_NAME = java.util.Collection.class.getName();

	private static final List<String> INTEGER_TYPE_BINDING_CHECK_LIST = Arrays.asList(INTEGER_QUALIFIED_NAME,
			PRIMITIVE_INT_QUALIFIED_NAME);
	private static final List<String> STRING_TYPE_BINDING_CHECK_LIST = Collections.singletonList(STRING_QUALIFIED_NAME);
	private static final List<String> COLLECTION_TYPE_BINDING_CHECK_LIST = Collections
			.singletonList(COLLECTION_QUALIFIED_NAME);

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

	@Override
	public boolean visit(MethodInvocation methodInvocationNode) {
		if ("indexOf".equals(methodInvocationNode.getName().getIdentifier())) { //$NON-NLS-1$
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

	private void convertToContains(MethodInvocation methodInvocationNode, TransformationType type) {
		ASTNode parentNode = methodInvocationNode.getParent();
		if (parentNode.getNodeType() == ASTNode.INFIX_EXPRESSION) {
			InfixExpression parent = (InfixExpression) parentNode;
			if (parent != null) {
				IndexOfMethodPosition position = this.getPosition(parent);
				TransformationOption option = this.getTransformationOption(parent, position);
				if (option != null) {
					List<Expression> methodArguments = ASTNodeUtil.convertToTypedList(methodInvocationNode.arguments(),
							Expression.class);
					if (methodArguments != null && methodArguments.size() == 1) {
						Expression methodArgumentExpression = methodArguments.get(0);
						boolean doTransformation = true;

						if (type == TransformationType.STRING && !isStringType(methodArgumentExpression)) {
							doTransformation = false;
						}

						if (doTransformation) {
							this.transform(methodInvocationNode.getExpression(), methodArgumentExpression, parent,
									option);
						}
					}
				}
			}
		}
	}

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

		return option;
	}

	private void transform(Expression leftExpression, Expression methodArgument, InfixExpression infixExpression,
			TransformationOption option) {
		if (methodArgument != null && infixExpression != null && option != null) {

			Expression copyLeftExpression = (Expression) astRewrite.createMoveTarget(leftExpression);
			Expression copyMethodArgumentExpression = (Expression) astRewrite.createCopyTarget(methodArgument);
			SimpleName containsSimpleName = astRewrite.getAST().newSimpleName("contains"); //$NON-NLS-1$
			MethodInvocation containsMethodInvocation = astRewrite.getAST().newMethodInvocation();
			containsMethodInvocation.setExpression(copyLeftExpression);
			containsMethodInvocation.setName(containsSimpleName);
			containsMethodInvocation.arguments().add(copyMethodArgumentExpression);

			if (option == TransformationOption.NOT_CONTAINS) {
				PrefixExpression notExpression = astRewrite.getAST().newPrefixExpression();
				notExpression.setOperator(PrefixExpression.Operator.NOT);
				notExpression.setOperand(containsMethodInvocation);

				astRewrite.replace(infixExpression, notExpression, null);
			} else {
				astRewrite.replace(infixExpression, containsMethodInvocation, null);
			}

		}
	}

	private IndexOfMethodPosition getPosition(InfixExpression infixExpression) {
		IndexOfMethodPosition position = null;

		Expression leftOperand = infixExpression.getLeftOperand();
		Expression rightOperand = infixExpression.getRightOperand();
		
		if(leftOperand.getNodeType() == ASTNode.METHOD_INVOCATION) {
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

	private boolean isCollectionType(Expression expression) {
		boolean result = false;

		if (expression != null) {
			ITypeBinding expressionTypeBinding = expression.resolveTypeBinding();
			if (expressionTypeBinding != null) {
				if (ClassRelationUtil.isContentOfTypes(expressionTypeBinding, COLLECTION_TYPE_BINDING_CHECK_LIST)
						|| ClassRelationUtil.isInheritingContentOfTypes(expressionTypeBinding,
								COLLECTION_TYPE_BINDING_CHECK_LIST)) {
					result = true;
				}
			}
		}

		return result;
	}

	private boolean isStringType(Expression expression) {
		boolean result = false;

		if (expression != null) {
			ITypeBinding expressionTypeBinding = expression.resolveTypeBinding();
			if (expressionTypeBinding != null) {
				if (ClassRelationUtil.isContentOfTypes(expressionTypeBinding, STRING_TYPE_BINDING_CHECK_LIST)
						|| ClassRelationUtil.isInheritingContentOfTypes(expressionTypeBinding,
								STRING_TYPE_BINDING_CHECK_LIST)) {
					result = true;
				}
			}
		}

		return result;
	}

	private boolean isIntegerType(Expression expression) {
		boolean result = false;

		if (expression != null) {
			ITypeBinding expressionType = expression.resolveTypeBinding();
			if (expressionType != null) {
				if (ClassRelationUtil.isContentOfTypes(expressionType, INTEGER_TYPE_BINDING_CHECK_LIST)
						|| ClassRelationUtil.isInheritingContentOfTypes(expressionType,
								INTEGER_TYPE_BINDING_CHECK_LIST)) {
					result = true;
				}
			}
		}

		return result;
	}
}
