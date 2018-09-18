package eu.jsparrow.rules.common.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;

/**
 * A utility class for computing information related with the ancestors of a
 * type. Makes use of {@link ITypeBinding} for finding finding super types and
 * their properties. Furthermore, finds the corresponding boxed types for each
 * java primitive type.
 * 
 * @author Martin Huter, Ardit Ymeri
 * @since 0.9.2
 */
public class ClassRelationUtil {

	private static final String INT = "int"; //$NON-NLS-1$
	private static final String DOUBLE = "double"; //$NON-NLS-1$
	private static final String FLOAT = "float"; //$NON-NLS-1$
	private static final String LONG = "long"; //$NON-NLS-1$
	private static final String SHORT = "short"; //$NON-NLS-1$
	private static final String BOOLEAN = "boolean"; //$NON-NLS-1$
	private static final String BYTE = "byte"; //$NON-NLS-1$
	private static final String CHAR = "char"; //$NON-NLS-1$

	private ClassRelationUtil() {
	}

	/**
	 * Checks whether the given type binding is a subtype of any of the types
	 * having the qualified name in the given list.
	 * 
	 * @param iTypeBinding
	 *            Is an {@link ITypeBinding} that is compared to the list of
	 *            injected java-classes if it is related to it by polymorphism
	 * @param fullyQuallifiedTargetnames
	 *            list of qualified names of the super types to look for.
	 * @return if the {@link ITypeBinding} is part of the registered types the
	 *         return value is true
	 */
	public static boolean isInheritingContentOfTypes(ITypeBinding iTypeBinding,
			List<String> fullyQuallifiedTargetnames) {

		if (iTypeBinding == null) {
			return false;
		}

		return findAncestors(iTypeBinding).stream()
			.map(ITypeBinding::getErasure)
			.map(ITypeBinding::getQualifiedName)
			.anyMatch(fullyQuallifiedTargetnames::contains);

	}

	/**
	 * Checks whether the erasure of the given type binding coincides with any
	 * of the given registered type.
	 * 
	 * @param iTypeBinding
	 *            type binding to be checked
	 * @param registeredIType
	 *            list of qualified names of target types
	 * 
	 * @return if the erasure of the type binding equals the registeredType
	 */
	public static boolean isContentOfType(ITypeBinding iTypeBinding, String registeredIType) {

		if (iTypeBinding == null) {
			return false;
		}

		return registeredIType.equals(iTypeBinding.getErasure()
			.getQualifiedName());
	}

	/**
	 * Checks whether the erasure of the given type binding coincides with any
	 * of the given list of registered types.
	 * 
	 * @param iTypeBinding
	 *            type binding to be checked
	 * @param registeredITypes
	 *            list of qualified names of target types
	 * 
	 * @return if the erasure the type binding occurs in the list of types
	 */
	public static boolean isContentOfTypes(ITypeBinding iTypeBinding, List<String> registeredITypes) {

		if (iTypeBinding == null) {
			return false;
		}

		return registeredITypes.contains(iTypeBinding.getErasure()
			.getQualifiedName());
	}

	/**
	 * Compares the given lists by getting the qualified name of corresponding
	 * elements on same positions.
	 * 
	 * @param firstTypeBindings
	 *            the first {@link ITypeBinding} array to be compared
	 * @param secondTypeBindings
	 *            the second {@link ITypeBinding} array to be compared
	 * @return if both lists have the same size and all corresponding elements
	 *         have the same qualified name.
	 */
	public static boolean compareITypeBinding(ITypeBinding[] firstTypeBindings, ITypeBinding[] secondTypeBindings) {
		if (!compareSizes(firstTypeBindings, secondTypeBindings)) {
			return false;
		}

		for (int i = 0; i < firstTypeBindings.length; i++) {
			if (!compareITypeBinding(firstTypeBindings[i], secondTypeBindings[i])) {
				return false;
			}
		}

		return true;
	}

	private static boolean compareSizes(ITypeBinding[] firstTypeBindings, ITypeBinding[] secondTypeBindings) {
		if (firstTypeBindings == null || secondTypeBindings == null) {
			return false;
		}

		int lhsSize = firstTypeBindings.length;
		int rhsSize = secondTypeBindings.length;

		return lhsSize == rhsSize;
	}

	public static boolean compareBoxedITypeBinding(ITypeBinding[] firstTypeBindings,
			ITypeBinding[] secondTypeBindings) {
		if (!compareSizes(firstTypeBindings, secondTypeBindings)) {
			return false;
		}

		for (int i = 0; i < firstTypeBindings.length; i++) {
			ITypeBinding firstType = firstTypeBindings[i];
			ITypeBinding secondType = secondTypeBindings[i];
			if (firstType.isPrimitive() || secondType.isPrimitive()) {
				String firstTypeName = findBoxedTypeOfPrimitive(firstType);
				String secondTypeName = findBoxedTypeOfPrimitive(secondType);
				if (!firstTypeName.equals(secondTypeName)) {
					return false;
				}
			} else if (!compareITypeBinding(firstType, secondType)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * @see ClassRelationUtil#compareITypeBinding(ITypeBinding[],
	 *      ITypeBinding[])
	 * 
	 * @param firstTypeBinding
	 *            the first {@link ITypeBinding} to be compared
	 * @param secondTypeBinging
	 *            the second {@link ITypeBinding} to be compared
	 * @return whether or not the {@link ITypeBinding}s have the same qualified
	 *         name
	 */
	public static boolean compareITypeBinding(ITypeBinding firstTypeBinding, ITypeBinding secondTypeBinging) {
		if (null == firstTypeBinding || null == secondTypeBinging) {
			return false;
		}

		String lhsTypeName = firstTypeBinding.getQualifiedName();
		String rhsTypeName = secondTypeBinging.getQualifiedName();
		return lhsTypeName.equals(rhsTypeName);
	}

	/**
	 * Finds the list of type bindings of the supper classes and interfaces
	 * inherited by the given type binding.
	 * 
	 * @param typeBinding
	 *            the resolved type binding of the class to find the ancestors
	 *            for.
	 * @return the list of type bindings of all ancestors
	 */
	public static List<ITypeBinding> findAncestors(ITypeBinding typeBinding) {
		List<ITypeBinding> ancesotrs = new ArrayList<>();

		if (typeBinding != null) {
			// get the type binding of super class
			ITypeBinding parentClass = typeBinding.getSuperclass();
			if (parentClass != null) {
				ancesotrs.add(parentClass);
				ancesotrs.addAll(findAncestors(parentClass));
			}

			// get type bindings of the implemented interfaces
			for (ITypeBinding iTypeBinding : typeBinding.getInterfaces()) {
				if (iTypeBinding != null) {
					ancesotrs.add(iTypeBinding);
					ancesotrs.addAll(findAncestors(iTypeBinding));
				}
			}
		}

		return ancesotrs;
	}

	/**
	 * Finds the names of the non-private fields that are inherited from the
	 * given type binding.
	 * 
	 * @param typeBinding
	 *            a type binding
	 * @return list of inherited field names
	 */
	public static List<String> findInheretedFields(ITypeBinding typeBinding) {
		List<ITypeBinding> ancestors = findAncestors(typeBinding);

		return ancestors.stream()
			.flatMap(ancestor -> Arrays.asList(ancestor.getDeclaredFields())
				.stream()
				.filter(field -> !Modifier.isPrivate(field.getModifiers())))
			.map(IVariableBinding::getName)
			.collect(Collectors.toList());
	}

	public static List<IMethodBinding> findInheretedMethods(ITypeBinding typeBinding) {
		List<ITypeBinding> ancestors = findAncestors(typeBinding);

		return ancestors.stream()
			.flatMap(ancestor -> Arrays.asList(ancestor.getDeclaredMethods())
				.stream()
				.filter(method -> !Modifier.isPrivate(method.getModifiers())))
			.collect(Collectors.toList());
	}

	/**
	 * Returns the name of the corresponding boxed type for the given primitive
	 * type.
	 * 
	 * @param primitiveType
	 *            the binding of a primitive type the name of a primitive
	 * 
	 * @return
	 *         <ul>
	 *         <li>the name of the corresponding boxed type if the given type is
	 *         primitive</li>
	 *         <li>the type name it is not a primitive</li>
	 *         <li>or an empty string if the given type is {@code null}</li>
	 *         </ul>
	 */
	public static String findBoxedTypeOfPrimitive(ITypeBinding primitiveType) {

		if (primitiveType == null) {
			return ""; //$NON-NLS-1$
		}

		String primitiveTypeName = primitiveType.getName();
		if (!primitiveType.isPrimitive()) {
			return primitiveTypeName;
		}

		String expressionName;
		switch (primitiveTypeName) {
		case INT:
			expressionName = Integer.class.getSimpleName();
			break;
		case DOUBLE:
			expressionName = Double.class.getSimpleName();
			break;
		case FLOAT:
			expressionName = Float.class.getSimpleName();
			break;
		case LONG:
			expressionName = Long.class.getSimpleName();
			break;
		case SHORT:
			expressionName = Short.class.getSimpleName();
			break;
		case BOOLEAN:
			expressionName = Boolean.class.getSimpleName();
			break;
		case BYTE:
			expressionName = Byte.class.getSimpleName();
			break;
		case CHAR:
			expressionName = Character.class.getSimpleName();
			break;
		default:
			expressionName = primitiveTypeName;
			break;
		}

		return expressionName;
	}

	/**
	 * Checks if a method is overloaded on the i-th parameter (expected to be a
	 * parameterized type). The search is based on type of the expression of the
	 * given method invocation, or in the type where the method is declared if
	 * the expression of the method invocation is {@code null}.
	 * 
	 * @param methodInvocation
	 *            a node representing the occurrence of the method invocation
	 * @param methodBinding
	 *            a method to be checked for overloading
	 * @param i
	 *            the position of the parameterized type parameter
	 * @return {@code true} if the method is overloaded based only on the i-th
	 *         parameter and {@code false} otherwise.
	 */
	public static boolean isOverloadedWithParameterizedTypes(MethodInvocation methodInvocation,
			IMethodBinding methodBinding, int i) {
		Expression expression = methodInvocation.getExpression();
		ITypeBinding expressionBinding;
		if (expression != null && (expressionBinding = expression.resolveTypeBinding()) != null) {
			return isOverloadedWithParameterizedTypes(expressionBinding, methodBinding, i);
		} else {
			ITypeBinding declaringClass = methodBinding.getDeclaringClass();
			return isOverloadedWithParameterizedTypes(declaringClass, methodBinding, i);
		}
	}

	/**
	 * Checks if a method is overloaded on the i-th parameter (expected to be a
	 * parameterized type). The search is based on the methods declared on the
	 * given {@link ITypeBinding} and its parents.
	 * 
	 * @param declaringClass
	 *            a type to get the overloading candidates from.
	 * @param methodBinding
	 *            a method to be checked for overloading
	 * @param i
	 *            the position of the parameterized type parameter
	 * @return {@code true} if the method is overloaded based only on the i-th
	 *         parameter and {@code false} otherwise.
	 */
	public static boolean isOverloadedWithParameterizedTypes(ITypeBinding declaringClass, IMethodBinding methodBinding,
			int i) {

		List<IMethodBinding> methods = new ArrayList<>();
		methods.addAll(Arrays.asList(declaringClass.getDeclaredMethods()));
		List<ITypeBinding> ancestors = findAncestors(declaringClass);
		methods.addAll(ancestors.stream()
			.flatMap(ancestor -> Arrays.stream(ancestor.getDeclaredMethods())
				.filter(method -> !Modifier.isPrivate(method.getModifiers())))
			.collect(Collectors.toList()));
		return methods.stream()
			.filter(method -> method.getName()
				.equals(methodBinding.getName()) && (method.getParameterTypes().length > i)
					&& method.getParameterTypes()[i].isParameterizedType()
					&& matchingNonParameterizedTypes(methodBinding.getParameterTypes(), method.getParameterTypes()))
			.count() > 1;
	}

	/**
	 * Checks if the given arrays of types are matching with each-other. The
	 * parameterized types are excluded.
	 * 
	 * @param parameterTypes
	 *            array of types
	 * @param parameterTypes2
	 *            array of types.
	 * 
	 * @return {@code true} if the length of the arrays are equal and all
	 *         corresponding types except the parameterized ones are matching
	 *         and {@code false} otherwise.
	 */
	private static boolean matchingNonParameterizedTypes(ITypeBinding[] parameterTypes,
			ITypeBinding[] parameterTypes2) {
		if (parameterTypes.length != parameterTypes2.length) {
			return false;
		}
		boolean allButParamMatching = true;
		for (int j = 0; j < parameterTypes.length; j++) {
			ITypeBinding type1 = parameterTypes[j];
			ITypeBinding type2 = parameterTypes2[j];
			if (!type1.isParameterizedType() && !compareITypeBinding(type1, type2)) {
				allButParamMatching = false;
				break;
			}
		}
		return allButParamMatching;
	}

}
