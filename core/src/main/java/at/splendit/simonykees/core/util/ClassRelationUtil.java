package at.splendit.simonykees.core.util;

import java.util.List;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ITypeBinding;

/**
 * TODO SIM-103 add class description
 * 
 * @author Martin Huter
 * @since 0.9.2
 * 
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
	public static boolean isInheritingContentOfRegistertITypes(ITypeBinding iTypeBinding, List<IType> registeredITypes) {
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
			result = result || isInheritingContentOfRegistertITypes(interfaceBind.getSuperclass(), registeredITypes);
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
}
