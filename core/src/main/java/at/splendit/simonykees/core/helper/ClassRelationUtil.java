package at.splendit.simonykees.core.helper;

import java.util.List;

import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Type;

public class ClassRelationUtil {
	public boolean inheritsContentofRegistertITypes(ITypeBinding iTypeBinding, List<Type> registeredITypes) {
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
			result = result || inheritsContentofRegistertITypes(interfaceBind.getSuperclass(), registeredITypes);
		}
		return result || inheritsContentofRegistertITypes(iTypeBinding.getSuperclass(), registeredITypes);
	}
	
	protected boolean isContentofRegistertITypes(ITypeBinding iTypeBinding, List<Type> registeredITypes) {
		if (iTypeBinding == null) {
			return false;
		}

		if (registeredITypes.contains(iTypeBinding.getJavaElement())) {
			return true;
		}
		return false;
	}
}
