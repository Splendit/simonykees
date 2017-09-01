package at.splendit.simonykees.core.util;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WildcardType;

/**
 * A utility class for computing different properties of {@link ASTNode}s.
 * 
 * @author Martin Huter, Ardit Ymeri, Matthias Webhofer
 * @since 0.9.2
 */
public class ASTNodeUtil {

	private static final String STREAM_MAP_METHOD_NAME = "map"; //$NON-NLS-1$
	private static final String STREAM_MAP_TO_INT_METHOD_NAME = "mapToInt"; //$NON-NLS-1$
	private static final String STREAM_MAP_TO_LONG_METHOD_NAME = "mapToLong"; //$NON-NLS-1$
	private static final String STREAM_MAP_TO_DOUBLE_METHOD_NAME = "mapToDouble"; //$NON-NLS-1$

	/**
	 * Finds the surrounding Block node if there is one, otherwise returns null
	 * 
	 * @param node
	 *            ASTNode where the backward search is started
	 * @return surrounding {@link Block}, null if non exists
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getSpecificAncestor(ASTNode node, Class<T> nodeType) {
		if (node == null) {
			return null;
		}

		if (nodeType.isInstance(node.getParent())) {
			return (T) node.getParent();
		} else {
			return getSpecificAncestor(node.getParent(), nodeType);
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
		if (ASTNode.METHOD_INVOCATION == node.getNodeType()) {
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

			if (ASTNode.RETURN_STATEMENT == classInstanceExecuterType) {
				MethodDeclaration mD = ASTNodeUtil.getSpecificAncestor(classInstanceExecuter, MethodDeclaration.class);
				variableTypeBinding = mD.getReturnType2().resolveBinding();
			}

		}
		return variableTypeBinding;
	}

	/**
	 * Returns list with generic type if all elements are instance of listType
	 * 
	 * @param rawList
	 *            the raw list
	 * @param listType
	 *            the generic type of the list
	 * @return emptyList if not all objects are from the listType, otherwise the
	 *         list with generic parameter.
	 */
	public static <T> List<T> returnTypedList(@SuppressWarnings("rawtypes") List rawList, Class<T> listType) {
		if (rawList == null || listType == null) {
			return Collections.emptyList();
		}

		List<T> returnList = convertToTypedList(rawList, listType);

		if (returnList.size() != rawList.size()) {
			return Collections.emptyList();
		}
		return returnList;
	}

	/**
	 * Converts the raw list to a typed list. Filters out the elements that are
	 * not instances of the given type.
	 * 
	 * @param rawlist
	 * @param type
	 * @return list of the given type.
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> convertToTypedList(@SuppressWarnings("rawtypes") List rawlist, Class<T> type) {
		return ((List<Object>) rawlist).stream().filter(type::isInstance).map(type::cast).collect(Collectors.toList());
	}

	/**
	 * Filters a list of modifiers if specific modifiers are present defined by
	 * the predicate
	 * 
	 * @param modifiers
	 *            List of Assuming to be modifiers, can't use type because JDT
	 *            doesn't support those
	 * @param predicate
	 *            is definition which modifiers have to be present
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static boolean hasModifier(List modifiers, Predicate<? super Modifier> predicate) {
		return ASTNodeUtil.convertToTypedList(modifiers, Modifier.class).stream().anyMatch(predicate);
	}

	/**
	 * Checks if the given type binding corresponds to either of the primitives:
	 * {@code int}, {@code long} or {@code double}, and if yes returns the
	 * corresponding method name which returns the respective stream type.
	 * 
	 * @param initializerBinding
	 *            type binding of the resulting stream type.
	 * 
	 * @return {@value #STREAM_MAP_METHOD_NAME} if the given type is not any of
	 *         the aforementioned types, or any of the following:
	 *         {@value #STREAM_MAP_TO_INT_METHOD_NAME},
	 *         {@value #STREAM_MAP_TO_DOUBLE_METHOD_NAME} or
	 *         {@value #STREAM_MAP_TO_LONG_METHOD_NAME} respectively for
	 *         {@code int}, {@code double} or {@code long} primitves.
	 */
	public static String calcMappingMethodName(ITypeBinding initializerBinding) {
		if (initializerBinding.isPrimitive()) {
			String typeName = initializerBinding.getQualifiedName();
			switch (typeName) {
			case "int": //$NON-NLS-1$
				return STREAM_MAP_TO_INT_METHOD_NAME;
			case "double": //$NON-NLS-1$
				return STREAM_MAP_TO_DOUBLE_METHOD_NAME;
			case "long": //$NON-NLS-1$
				return STREAM_MAP_TO_LONG_METHOD_NAME;
			default:
				return STREAM_MAP_METHOD_NAME;
			}
		}
		return STREAM_MAP_METHOD_NAME;
	}

	/**
	 * This method extracts the left most {@link Expression} of a
	 * {@link MethodInvocation} by recursively walking the
	 * {@link MethodInvocation}.
	 * 
	 * @param methodInvocation
	 * @return the left most expression of the given {@link MethodInvocation}
	 */
	public static Expression getLeftMostExpressionOfMethodInvocation(MethodInvocation methodInvocation) {
		Expression result = null;
		if (methodInvocation != null) {
			Expression expression = methodInvocation.getExpression();
			if (expression != null) {
				if (ASTNode.METHOD_INVOCATION == expression.getNodeType()) {
					MethodInvocation methodInvocationExpression = (MethodInvocation) expression;
					result = getLeftMostExpressionOfMethodInvocation(methodInvocationExpression);
				} else if (ASTNode.SIMPLE_NAME == expression.getNodeType()) {
					return expression;
				}
			}
		}

		return result;
	}
}
