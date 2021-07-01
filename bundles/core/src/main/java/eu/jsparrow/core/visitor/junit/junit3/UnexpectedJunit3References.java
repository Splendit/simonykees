package eu.jsparrow.core.visitor.junit.junit3;

import static eu.jsparrow.core.visitor.junit.jupiter.RegexJUnitQualifiedName.isJUnitName;

import java.util.List;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

import eu.jsparrow.rules.common.util.ClassRelationUtil;

public class UnexpectedJunit3References {

	private UnexpectedJunit3References() {
		// private constructor of utility class in hiding implicit public one
	}
	
	static boolean hasUnexpectedJUnitReference(IMethodBinding methodBinding) {
		return hasUnexpectedJUnitReference(methodBinding.getDeclaringClass());
	}

	static boolean hasUnexpectedJUnitReference(ITypeBinding typeBinding) {
		if (typeBinding.isPrimitive()) {
			return false;
		}
		if (typeBinding.isArray()) {
			return hasUnexpectedJUnitReference(typeBinding.getComponentType());
		}
		if (isUnexpectedJUnitQualifiedName(typeBinding.getQualifiedName())) {
			return true;
		}

		List<ITypeBinding> ancestors = ClassRelationUtil.findAncestors(typeBinding);
		for (ITypeBinding ancestor : ancestors) {
			if (isUnexpectedJUnitQualifiedName(ancestor.getQualifiedName())) {
				return true;
			}
		}
		return false;
	}

	private static boolean hasUnexpectedTypeArgument(ITypeBinding typeBinding) {
		if (typeBinding.isParameterizedType()) {
			ITypeBinding[] typeParameters = typeBinding.getTypeArguments();
			for (ITypeBinding parameterType : typeParameters) {
				if (hasUnexpectedJUnitReference(parameterType)) {
					return true;
				}
			}
		}
		return false;
	}

	static boolean isUnexpectedJUnitQualifiedName(String qualifiedName) {
		return isJUnitName(qualifiedName);
	}
}
