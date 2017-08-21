package at.splendit.simonykees.core.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ITypeBinding;
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

		return findAncestors(iTypeBinding).stream().map(ITypeBinding::getErasure).map(ITypeBinding::getQualifiedName)
				.anyMatch(fullyQuallifiedTargetnames::contains);

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

		if (registeredITypes.contains(iTypeBinding.getErasure().getQualifiedName())) {
			return true;
		}

		return false;
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
		if (firstTypeBindings == null || secondTypeBindings == null) {
			return false;
		}

		int lhsSize = firstTypeBindings.length;
		int rhsSize = secondTypeBindings.length;

		if (lhsSize != rhsSize) {
			return false;
		}

		for (int i = 0; i < lhsSize; i++) {
			if (!compareITypeBinding(firstTypeBindings[i], secondTypeBindings[i])) {
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
		if (lhsTypeName.equals(rhsTypeName)) {
			return true;
		}

		return false;
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
				.flatMap(ancestor -> Arrays.asList(ancestor.getDeclaredFields()).stream()
						.filter(field -> !Modifier.isPrivate(field.getModifiers())))
				.map(varBinding -> varBinding.getName()).collect(Collectors.toList());
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

}
