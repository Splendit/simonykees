package eu.jsparrow.core.visitor.junit.junit3;

import static eu.jsparrow.core.visitor.junit.jupiter.RegexJUnitQualifiedName.isJUnitName;

import java.util.List;

import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

import eu.jsparrow.rules.common.util.ClassRelationUtil;

/**
 * Utility class for detecting unexpected references to JUnit which prohibit
 * transformation.
 * 
 * @since 4.1.0
 *
 */
public class UnexpectedJunit3References {

	private UnexpectedJunit3References() {
		// private constructor of utility class in hiding implicit public one
	}

	static boolean analyzeNameBinding(IBinding binding) {

		if (binding.getKind() == IBinding.PACKAGE) {
			IPackageBinding packageBinding = (IPackageBinding) binding;
			return !isJUnitName(packageBinding.getName());
		}

		if (binding.getKind() == IBinding.TYPE) {
			return !isUnexpectedJUnitReference((ITypeBinding) binding);
		}

		if (binding.getKind() == IBinding.METHOD) {
			return !isUnexpectedJUnitReference(((IMethodBinding) binding).getDeclaringClass());
		}

		if (binding.getKind() == IBinding.ANNOTATION) {
			// Not covered, but anyway a name is not expected to have a binding
			// of the kind IBinding.ANNOTATION
			return false;
		}

		if (binding.getKind() == IBinding.MEMBER_VALUE_PAIR) {
			// Not covered, but anyway a name is not expected to have a binding
			// of the kind IBinding.MEMBER_VALUE_PAIR
			return false;
		}

		if (binding.getKind() == IBinding.VARIABLE) {
			IVariableBinding variableBinding = (IVariableBinding) binding;
			ITypeBinding variableTypeBinding = variableBinding.getVariableDeclaration()
				.getType();
			if (isUnexpectedJUnitReference(variableTypeBinding)) {
				return false;
			}
			if (variableBinding.isField()) {
				ITypeBinding fieldDeclaringClass = variableBinding.getDeclaringClass();
				if (fieldDeclaringClass != null
						&& isUnexpectedJUnitReference(fieldDeclaringClass)) {
					return false;
				}
			}
			return true;
		}
		// Not covered: any other binding which is not expected for a name in
		// connection with the migration of JUnit3
		return false;
	}

	static boolean isUnexpectedJUnitReference(ITypeBinding typeBinding) {
		if (typeBinding.isPrimitive()) {
			return false;
		}
		if (typeBinding.isArray()) {
			return isUnexpectedJUnitReference(typeBinding.getComponentType());
		}
		if (isJUnitName(typeBinding.getQualifiedName())) {
			return true;
		}

		List<ITypeBinding> ancestors = ClassRelationUtil.findAncestors(typeBinding);
		for (ITypeBinding ancestor : ancestors) {
			if (isJUnitName(ancestor.getQualifiedName())) {
				return true;
			}
		}
		return false;
	}
}
