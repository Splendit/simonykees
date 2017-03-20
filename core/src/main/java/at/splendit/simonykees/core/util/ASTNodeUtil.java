package at.splendit.simonykees.core.util;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WildcardType;

/**
 * TODO SIM-103 add class description
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
public class ASTNodeUtil {

	/**
	 * Finds the surrounding Block node if there is one, otherwise returns null
	 * 
	 * @param node
	 *            ASTNode where the backward search is started
	 * @return surrounding {@link Block}, null if non exists
	 */
	public static Block getSurroundingBlock(ASTNode node) {
		if (node == null) {
			return null;
		}
		if (node.getParent() instanceof Block) {
			return (Block) node.getParent();
		} else {
			return getSurroundingBlock(node.getParent());
		}
	}

	/**
	 * Removes all surrounding parenthesizes
	 * 
	 * @param expression
	 *            is unwrapped, if it is a {@link ParenthesizedExpression}
	 * @return unwrapped expression
	 */
	public static Expression unwrapParenthesizedExpression(Expression expression) {
		if (expression instanceof ParenthesizedExpression) {
			return unwrapParenthesizedExpression(((ParenthesizedExpression) expression).getExpression());
		}
		return expression;
	}

	/**
	 * Returns the type parameter of a variableDeclaration if it only contains
	 * exactly one
	 * 
	 * @param variableDeclaration
	 *            {@link VariableDeclarationStatement} or
	 *            {@link VariableDeclarationExpression} that holds exactly on
	 *            type parameter
	 * @return
	 */
	public static Type getSingleTypeParameterOfVariableDeclaration(ASTNode variableDeclaration) {
		if (null == variableDeclaration) {
			return null;
		}

		Type tempType = null;
		if (ASTNode.VARIABLE_DECLARATION_STATEMENT == variableDeclaration.getNodeType()) {
			tempType = ((VariableDeclarationStatement) variableDeclaration).getType();
		}

		if (ASTNode.VARIABLE_DECLARATION_EXPRESSION == variableDeclaration.getNodeType()) {
			tempType = ((VariableDeclarationExpression) variableDeclaration).getType();
		}

		if (ASTNode.PARAMETERIZED_TYPE == tempType.getNodeType()) {
			ParameterizedType parameterizedType = (ParameterizedType) tempType;
			if (1 == parameterizedType.typeArguments().size()) {
				Type parameterType = (Type) parameterizedType.typeArguments().get(0);
				if (parameterType.isWildcardType()) {
					WildcardType wildcardType = (WildcardType) parameterType;
					if (wildcardType.isUpperBound()) {
						return wildcardType.getBound();
					}
				} else if (parameterType.isSimpleType()) {
					return parameterType;
				}
			}
		}

		return null;
	}

	/**
	 * Returns the SimpleName of an {@link MethodInvocation#getExpression()} if
	 * it is one and the {@link MethodInvocation} is "hasNext"
	 * 
	 * @param node
	 *            Assumed MethodInvocation of "hasNext"
	 * @return {@link SimpleName} of the
	 *         {@link MethodInvocation#getExpression()}
	 */
	public static SimpleName replaceableIteratorCondition(Expression node) {
		if (node instanceof MethodInvocation) {
			MethodInvocation methodInvocation = (MethodInvocation) node;
			// check for hasNext operation on Iterator
			if (StringUtils.equals("hasNext", methodInvocation.getName().getFullyQualifiedName()) //$NON-NLS-1$
					&& methodInvocation.getExpression() instanceof SimpleName) {
				return (SimpleName) methodInvocation.getExpression();
			}
		}
		return null;
	}

	/**
	 * Resolves the type of the position in an AST where a
	 * {@link ClassInstanceCreation} is used
	 * 
	 * @param parentNode
	 *            {@link ClassInstanceCreation} that is used to find its
	 *            environment
	 * @return the least required {@link ITypeBinding} from the position where
	 *         the {@link ClassInstanceCreation} is used
	 */
	public static ITypeBinding getTypeBindingOfNodeUsage(ClassInstanceCreation parentNode) {
		// Find the type of the executing environment
		ITypeBinding variableTypeBinding = null;

		if (null != parentNode.getParent()) {
			ASTNode classInstanceExecuter = parentNode.getParent();
			int classInstanceExecuterType = classInstanceExecuter.getNodeType();

			// Possible scenarios for the ClassInstanceCreation
			if (ASTNode.METHOD_INVOCATION == classInstanceExecuterType) {
				MethodInvocation mI = ((MethodInvocation) classInstanceExecuter);
				int indexOfClassInstance = mI.arguments().indexOf(parentNode);
				IMethodBinding methodBinding = mI.resolveMethodBinding();
				if (null != methodBinding) {
					ITypeBinding[] parameters = methodBinding.getParameterTypes();
					if (-1 != indexOfClassInstance && parameters.length > indexOfClassInstance) {
						variableTypeBinding = parameters[indexOfClassInstance];
					}
				}
			}

			if (ASTNode.CLASS_INSTANCE_CREATION == classInstanceExecuterType) {
				ClassInstanceCreation ciI = ((ClassInstanceCreation) classInstanceExecuter);
				int indexOfClassInstance = ciI.arguments().indexOf(parentNode);
				IMethodBinding methodBinding = ciI.resolveConstructorBinding();
				if (null != methodBinding) {
					ITypeBinding[] parameters = methodBinding.getParameterTypes();
					if (-1 != indexOfClassInstance && parameters.length > indexOfClassInstance) {
						variableTypeBinding = parameters[indexOfClassInstance];
					}
				}
			}

			if (ASTNode.ASSIGNMENT == classInstanceExecuterType) {
				variableTypeBinding = ((Assignment) classInstanceExecuter).getLeftHandSide().resolveTypeBinding();
			}

			if (ASTNode.VARIABLE_DECLARATION_FRAGMENT == classInstanceExecuterType
					&& null != classInstanceExecuter.getParent()) {
				if (ASTNode.VARIABLE_DECLARATION_STATEMENT == classInstanceExecuter.getParent().getNodeType()) {
					VariableDeclarationStatement vds = (VariableDeclarationStatement) classInstanceExecuter.getParent();
					variableTypeBinding = vds.getType().resolveBinding();
				}
				if (ASTNode.VARIABLE_DECLARATION_EXPRESSION == classInstanceExecuter.getParent().getNodeType()) {
					VariableDeclarationExpression vds = (VariableDeclarationExpression) classInstanceExecuter
							.getParent();
					variableTypeBinding = vds.getType().resolveBinding();
				}
				if (ASTNode.FIELD_DECLARATION == classInstanceExecuter.getParent().getNodeType()) {
					FieldDeclaration vds = (FieldDeclaration) classInstanceExecuter.getParent();
					variableTypeBinding = vds.getType().resolveBinding();
				}

			}

			if (ASTNode.SINGLE_VARIABLE_DECLARATION == classInstanceExecuterType) {
				SingleVariableDeclaration svd = (SingleVariableDeclaration) classInstanceExecuter;
				variableTypeBinding = svd.getType().resolveBinding();
			}

		}
		return variableTypeBinding;
	}
}
