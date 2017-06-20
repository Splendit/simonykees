package at.splendit.simonykees.core.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ITypeBinding;

/**
 * TODO SIM-103 add class description
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
public class ClassRelationUtil {
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
				.filter(fullyQuallifiedTargetnames::contains).findAny().isPresent();

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

}
