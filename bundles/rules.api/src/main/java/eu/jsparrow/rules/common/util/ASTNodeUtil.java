package eu.jsparrow.rules.common.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.LineComment;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WildcardType;

import eu.jsparrow.rules.common.visitor.helper.CommentRewriter;
import eu.jsparrow.rules.common.visitor.helper.WildCardTypeVisitor;

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

	private ASTNodeUtil() {
		// private constructor to hide the implicit public one
	}

	/**
	 * Finds the surrounding Node of the defined nodeType if there is one,
	 * otherwise returns null
	 * 
	 * @param node
	 *            ASTNode where the backward search is started
	 * @param nodeType
	 *            nodeType of the wanted node as class
	 * @return surrounding Node, null if non exists
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

		if (tempType != null && ASTNode.PARAMETERIZED_TYPE == tempType.getNodeType()) {
			ParameterizedType parameterizedType = (ParameterizedType) tempType;
			if (1 == parameterizedType.typeArguments()
				.size()) {
				Type parameterType = (Type) parameterizedType.typeArguments()
					.get(0);
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
			if (StringUtils.equals("hasNext", methodInvocation.getName() //$NON-NLS-1$
				.getFullyQualifiedName()) && methodInvocation.getExpression() instanceof SimpleName) {
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
				int indexOfClassInstance = mI.arguments()
					.indexOf(parentNode);
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
				int indexOfClassInstance = ciI.arguments()
					.indexOf(parentNode);
				IMethodBinding methodBinding = ciI.resolveConstructorBinding();
				if (null != methodBinding) {
					ITypeBinding[] parameters = methodBinding.getParameterTypes();
					if (-1 != indexOfClassInstance && parameters.length > indexOfClassInstance) {
						variableTypeBinding = parameters[indexOfClassInstance];
					}
				}
			}

			if (ASTNode.ASSIGNMENT == classInstanceExecuterType) {
				variableTypeBinding = ((Assignment) classInstanceExecuter).getLeftHandSide()
					.resolveTypeBinding();
			}

			if (ASTNode.VARIABLE_DECLARATION_FRAGMENT == classInstanceExecuterType
					&& null != classInstanceExecuter.getParent()) {
				if (ASTNode.VARIABLE_DECLARATION_STATEMENT == classInstanceExecuter.getParent()
					.getNodeType()) {
					VariableDeclarationStatement vds = (VariableDeclarationStatement) classInstanceExecuter.getParent();
					variableTypeBinding = vds.getType()
						.resolveBinding();
				}
				if (ASTNode.VARIABLE_DECLARATION_EXPRESSION == classInstanceExecuter.getParent()
					.getNodeType()) {
					VariableDeclarationExpression vds = (VariableDeclarationExpression) classInstanceExecuter
						.getParent();
					variableTypeBinding = vds.getType()
						.resolveBinding();
				}
				if (ASTNode.FIELD_DECLARATION == classInstanceExecuter.getParent()
					.getNodeType()) {
					FieldDeclaration vds = (FieldDeclaration) classInstanceExecuter.getParent();
					variableTypeBinding = vds.getType()
						.resolveBinding();
				}

			}

			if (ASTNode.SINGLE_VARIABLE_DECLARATION == classInstanceExecuterType) {
				SingleVariableDeclaration svd = (SingleVariableDeclaration) classInstanceExecuter;
				variableTypeBinding = svd.getType()
					.resolveBinding();
			}

			if (ASTNode.RETURN_STATEMENT == classInstanceExecuterType) {
				MethodDeclaration mD = ASTNodeUtil.getSpecificAncestor(classInstanceExecuter, MethodDeclaration.class);
				variableTypeBinding = mD.getReturnType2()
					.resolveBinding();
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
		return ((List<Object>) rawlist).stream()
			.filter(type::isInstance)
			.map(type::cast)
			.collect(Collectors.toList());
	}

	/**
	 * @return if the List specified by the first parameter contains exactly one
	 *         element which is an instance of the type specified by the 2nd
	 *         parameter, then an Optional is returned which stores the only one
	 *         element of the list. In all other cases an empty optional is
	 *         returned.
	 */
	public static <T extends ASTNode> Optional<T> findSingletonListElement(@SuppressWarnings("rawtypes") List rawlist,
			Class<T> type) {
		if (rawlist.size() != 1) {
			return Optional.empty();
		}

		return Optional.of(rawlist.get(0))
			.filter(type::isInstance)
			.map(type::cast);
	}

	/**
	 * 
	 * @return An Optional containing the element before the element specified
	 *         by the 2nd paramneter which is expected to be an instance of the
	 *         type specified by the 3rd parameter. In all other cases an empty
	 *         optional is returned.
	 * 
	 */
	public static <T extends ASTNode> Optional<T> findListElementBefore(@SuppressWarnings("rawtypes") List rawlist,
			ASTNode element,
			Class<T> type) {
		int indexBefore = rawlist.indexOf(element) - 1;
		if (indexBefore < 0) {
			return Optional.empty();
		}
		return Optional.of(rawlist.get(indexBefore))
			.filter(type::isInstance)
			.map(type::cast);
	}

	/**
	 * 
	 * @return An Optional containing the element after the element specified by
	 *         the 2nd paramneter which is expected to be an instance of the
	 *         type specified by the 3rd parameter. In all other cases an empty
	 *         optional is returned.
	 * 
	 */
	public static <T extends ASTNode> Optional<T> findListElementAfter(@SuppressWarnings("rawtypes") List rawlist,
			ASTNode element,
			Class<T> type) {
		int indexAfter = rawlist.indexOf(element) + 1;
		if (indexAfter >= rawlist.size()) {
			return Optional.empty();
		}
		return Optional.of(rawlist.get(indexAfter))
			.filter(type::isInstance)
			.map(type::cast);
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
		return ASTNodeUtil.convertToTypedList(modifiers, Modifier.class)
			.stream()
			.anyMatch(predicate);
	}

	/**
	 * Checks if the list of modifiers represents a package-private modifier.
	 * 
	 * @param modifiers
	 *            list of modifiers
	 * @return {@code true} if the given list has no {@code public},
	 *         {@code protected} or {@code private} modifiers, or {@code false}
	 *         otherwise.
	 */
	public static boolean isPackageProtected(@SuppressWarnings("rawtypes") List modifiers) {
		return modifiers.isEmpty() || (!hasModifier(modifiers, Modifier::isPublic)
				&& !hasModifier(modifiers, Modifier::isProtected) && !hasModifier(modifiers, Modifier::isPrivate));
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
	 * Finds the scope where the statement belongs to. A scope is either the
	 * body of:
	 * <ul>
	 * <li>a method</li>
	 * <li>an initializer</li>
	 * <li>a class/interface</li>
	 * <li>an enumeration</li>
	 * <li>an annotation declaration</li>
	 * </ul>
	 * 
	 * @param statement
	 *            a statement to look for the scope where it falls into.
	 * @return an {@link ASTNode} representing either of the above
	 */
	public static ASTNode findScope(Statement statement) {
		ASTNode parent = statement.getParent();
		while (parent != null && parent.getNodeType() != ASTNode.METHOD_DECLARATION
				&& parent.getNodeType() != ASTNode.INITIALIZER && parent.getNodeType() != ASTNode.TYPE_DECLARATION
				&& parent.getNodeType() != ASTNode.ENUM_DECLARATION
				&& parent.getNodeType() != ASTNode.ANNOTATION_TYPE_DECLARATION) {

			parent = parent.getParent();
		}
		return parent;
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

	/**
	 * Checks if a node has at least one occurrence of a {@link WildcardType}
	 * node.
	 * 
	 * @param node
	 *            the node to be checked
	 * @return {@code true} if the above condition is met and false otherwise.
	 */
	public static boolean containsWildCards(ASTNode node) {
		WildCardTypeVisitor wildCardsVisitor = new WildCardTypeVisitor();
		node.accept(wildCardsVisitor);
		return !wildCardsVisitor.getWildCardTypes()
			.isEmpty();
	}

	/**
	 * Sets the given name as the type property of the given {@link Type} node.
	 * Considers {@link SimpleType}s, {@link ArrayType}s and
	 * {@link ParameterizedType}s.
	 * 
	 * @param type
	 *            the type to be modified
	 * @param qualifiedName
	 *            new name of the type.
	 * 
	 * @return the type node having the new name property or the unmodified type
	 *         node if it doesn't fall in any of the aforementioned types.
	 */
	public static Type convertToQualifiedName(Type type, Name qualifiedName) {
		AST ast = type.getAST();
		if (type.isArrayType()) {
			ArrayType arrayType = (ArrayType) type;
			SimpleType simpleType = ast.newSimpleType(qualifiedName);
			arrayType.setStructuralProperty(ArrayType.ELEMENT_TYPE_PROPERTY, simpleType);
			return arrayType;
		} else if (type.isSimpleType()) {
			SimpleType simpleType = (SimpleType) type;
			simpleType.setName(qualifiedName);
			return simpleType;
		} else if (type.isParameterizedType()) {
			ParameterizedType parameterizedType = (ParameterizedType) type;
			SimpleType simpleType = ast.newSimpleType(qualifiedName);
			parameterizedType.setStructuralProperty(ParameterizedType.TYPE_PROPERTY, simpleType);
			return parameterizedType;
		}

		return type;
	}

	/**
	 * Converts {@link SimpleType}s, the {@link ArrayType}s and the
	 * {@link ParameterizedType}s to types with qualified name.
	 * 
	 * @param type
	 *            original type to be converted.
	 * @param typeBinding
	 *            a type binding to get the qualified name from.
	 * 
	 * @return the given type binding having a qualified name property.
	 */
	public static Type convertToQualifiedName(Type type, ITypeBinding typeBinding) {
		AST ast = type.getAST();
		Name qualifiedName = ast.newName(typeBinding.getQualifiedName());
		return convertToQualifiedName(type, qualifiedName);
	}

	/**
	 * Checks whether the given {@link ASTNode} and the declaration of the given
	 * type are enclosed in the same class.
	 * 
	 * @param loop
	 *            a node expected to represent a code snippet.
	 * @param typeBinding
	 *            a type binding expected to represent an inner class.
	 * @return {@code true} if node and the type declaration are wrapped by the
	 *         same class or {@code false} otherwise.
	 */
	public static boolean enclosedInSameType(ASTNode loop, ITypeBinding typeBinding) {
		AbstractTypeDeclaration enclosingType = getSpecificAncestor(loop, AbstractTypeDeclaration.class);
		if (enclosingType != null && typeBinding != null) {
			ITypeBinding enclosingTypeBinding = enclosingType.resolveBinding();
			if (enclosingTypeBinding != null
					&& (ClassRelationUtil.compareITypeBinding(enclosingTypeBinding.getErasure(),
							typeBinding.getErasure())
							|| ClassRelationUtil.compareITypeBinding(enclosingTypeBinding.getErasure(),
									typeBinding.getDeclaringClass()
										.getErasure()))) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Checks whether the last trailing comment of the given expression is a
	 * line comment.
	 * 
	 * @param expression
	 *            expression to be checked.
	 * @param commentRewriter
	 *            an instance of {@link CommentRewriter} initialized with the
	 *            AST which is currently being visited
	 * @return {@code true} if the last trailing comment is a
	 *         {@link LineComment} or {@code false} otherwise.
	 */
	public static boolean isFollowedByLineComment(Expression expression, CommentRewriter commentRewriter) {
		List<Comment> trailingComments = commentRewriter.findTrailingComments(expression);
		if (trailingComments.isEmpty()) {
			return false;
		}

		Comment lastTrailingComment = trailingComments.get(0);
		lastTrailingComment.getAlternateRoot()
			.delete();
		return lastTrailingComment.isLineComment();
	}

	/**
	 * Checks if any of the arguments of the given method invocation is followed
	 * by line comment.
	 * 
	 * @param node
	 *            an instance of a {@link MethodInvocation} to be checked.
	 * @param commentRewriter
	 *            an instance of {@link CommentRewriter} initialized with the
	 *            AST which is currently being visited
	 * @return {@code true} if the last trailing comment of any of the arguments
	 *         is a {@link LineComment} or {@code false} otherwise.
	 */
	public static boolean hasArgumentFollowedByLineComment(MethodInvocation node, CommentRewriter commentRewriter) {
		List<Expression> arguments = convertToTypedList(node.arguments(), Expression.class);

		return arguments.stream()
			.anyMatch(argument -> isFollowedByLineComment(argument, commentRewriter));
	}

	/**
	 * Checks if the given {@link ClassInstanceCreation} has a parameterized
	 * type and the list of the provided type arguments is empty. For example:
	 * {@code new HashMap<>()}.
	 * 
	 * @param classInstanceCreation
	 *            node representing a new object creation.
	 * @return if the above condition is met.
	 */
	public static boolean containsDiamondOperator(ClassInstanceCreation classInstanceCreation) {
		Type type = classInstanceCreation.getType();

		if (!type.isParameterizedType()) {
			return false;
		}

		ParameterizedType parameterizedType = (ParameterizedType) type;
		return parameterizedType.typeArguments()
			.isEmpty();
	}

	/**
	 * Finds the name of all fields declared in the provided type.
	 * 
	 * @param typeDeclaration
	 *            a type declaration to be searched.
	 * @return the list of identifiers of the declared fields.
	 */
	public static List<String> findFieldNames(TypeDeclaration typeDeclaration) {
		FieldDeclaration[] fields = typeDeclaration.getFields();
		List<String> names = new ArrayList<>();
		for (FieldDeclaration field : fields) {
			names.addAll(convertToTypedList(field.fragments(), VariableDeclarationFragment.class).stream()
				.map(VariableDeclarationFragment::getName)
				.map(SimpleName::getIdentifier)
				.collect(Collectors.toList()));
		}
		return names;
	}

	/**
	 * Checks if the given {@link Expression} represents an invocation of a
	 * generic method.
	 * 
	 * @param initializer
	 *            a node representing a method invocation
	 * @return if the above condition is met.
	 */
	public static boolean isGenericMethodInvocation(Expression initializer) {

		if (initializer.getNodeType() != ASTNode.METHOD_INVOCATION) {
			return false;
		}
		MethodInvocation initializerMethod = (MethodInvocation) initializer;
		IMethodBinding methodBinding = initializerMethod.resolveMethodBinding();
		if (methodBinding == null) {
			return true;
		}
		IMethodBinding methodDeclaration = methodBinding.getMethodDeclaration();
		return methodDeclaration != null && methodDeclaration.isGenericMethod();
	}

	/**
	 * Finds the outermost ancestor of type {@link ParenthesizedExpression}. If
	 * the given expression is not a child of a {@link ParenthesizedExpression}
	 * then it is returned immediately. Otherwise, the parents are traversed
	 * bottom-up until the first first node which is not a child of a
	 * {@link ParenthesizedExpression} is found.
	 * 
	 * @param expression
	 *            current expression
	 * @return the node computed as described above.
	 */
	public static ASTNode getOutermostParenthesizedExpression(Expression expression) {
		ASTNode child = expression;
		while (child.getLocationInParent() == ParenthesizedExpression.EXPRESSION_PROPERTY) {
			child = child.getParent();
		}
		return child;
	}

	/**
	 * 
	 * @param expression
	 *            an expression to check if it represents a literal.
	 * @return if the given expression is a literal.
	 */
	public static boolean isLiteral(Expression expression) {
		int nodeType = expression.getNodeType();
		return (nodeType == ASTNode.STRING_LITERAL
				|| nodeType == ASTNode.NUMBER_LITERAL
				|| nodeType == ASTNode.BOOLEAN_LITERAL
				|| nodeType == ASTNode.CHARACTER_LITERAL
				|| nodeType == ASTNode.NULL_LITERAL
				|| nodeType == ASTNode.TYPE_LITERAL);
	}

	/**
	 * 
	 * @param simpleName
	 *            a {@link SimpleName} to check if it represents a label.
	 * @return true if the given {@link SimpleName} is a label.
	 */
	public static boolean isLabel(SimpleName simpleName) {
		StructuralPropertyDescriptor locationInParent = simpleName.getLocationInParent();
		return locationInParent == LabeledStatement.LABEL_PROPERTY
				|| locationInParent == ContinueStatement.LABEL_PROPERTY
				|| locationInParent == BreakStatement.LABEL_PROPERTY;
	}
}
