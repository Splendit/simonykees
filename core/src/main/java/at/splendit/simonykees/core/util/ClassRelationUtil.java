package at.splendit.simonykees.core.util;

import java.util.List;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ITypeBinding;

/**
 * TODO SIM-103 add class description
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
public class ClassRelationUtil {
	/**
	 * 
	 * @param iTypeBinding
	 *            Is an {@link ITypeBinding} that is compared to the list of
	 *            injected java-classes if it is related to it by polymorphism
	 * @return if the {@link ITypeBinding} is part of the registered types the
	 *         return value is true
	 */
	public static boolean isInheritingContentOfRegistertITypes(ITypeBinding iTypeBinding,
			List<IType> registeredITypes) {
		boolean result = false;
		if (iTypeBinding == null) {
			return false;
		}

		if (registeredITypes.contains(iTypeBinding.getJavaElement())) {
			return true;
		}

		for (ITypeBinding interfaceBind : iTypeBinding.getInterfaces()) {
			if (registeredITypes.contains(interfaceBind.getJavaElement())) {
				return true;
			}
			result = result || isInheritingContentOfRegistertITypes(interfaceBind.getSuperclass(), registeredITypes)
					|| isInheritingContentOfRegistertITypes(interfaceBind, registeredITypes);
		}
		return result || isInheritingContentOfRegistertITypes(iTypeBinding.getSuperclass(), registeredITypes);
	}

	public static boolean isContentOfRegistertITypes(ITypeBinding iTypeBinding, List<IType> registeredITypes) {
		if (iTypeBinding == null) {
			return false;
		}

		if (registeredITypes.contains(iTypeBinding.getJavaElement())) {
			return true;
		}
		return false;
	}

	/**
	 * Compares the given lists by getting the qualified name of corresponding
	 * elements on same positions.
	 * 
	 * @return if both lists have the same size and all corresponding elements
	 *         have the same qualified name.
	 */
	public static boolean compareITypeBinding(ITypeBinding[] lhsTypeArguments, ITypeBinding[] rhsTypeBindingArguments) {
		if (lhsTypeArguments == null || rhsTypeBindingArguments == null) {
			return false;
		}

		int lhsSize = lhsTypeArguments.length;
		int rhsSize = rhsTypeBindingArguments.length;

		if (lhsSize != rhsSize) {
			return false;
		}
		
		
		
		for (int i = 0; i < lhsSize; i++) {
			if(!compareITypeBinding(lhsTypeArguments[i], rhsTypeBindingArguments[i])){
				return false;
			}
		}

		return true;
	}

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

}
