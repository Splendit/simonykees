package eu.jsparrow.core.visitor.junit.junit3;

import static eu.jsparrow.core.visitor.junit.jupiter.RegexJUnitQualifiedName.isJUnitName;

import java.util.List;

import org.eclipse.jdt.core.dom.ITypeBinding;

import eu.jsparrow.rules.common.util.ClassRelationUtil;

public class UnexpectedJunit3ReferencesAnalyzer {

	public boolean hasUnexpectedJUnitReference(ITypeBinding typeBinding) {
		if (typeBinding.isPrimitive()) {
			return false;
		}
		if (typeBinding.isArray()) {
			return hasUnexpectedJUnitReference(typeBinding.getComponentType());
		}
		return isUnexpectedJUnitQualifiedName(typeBinding.getQualifiedName()) ||
				hasUnexpectedSuperType(typeBinding) ||
				hasUnexpectedTypeArgument(typeBinding);
	}

	private boolean hasUnexpectedSuperType(ITypeBinding typeBinding) {

		List<ITypeBinding> ancestors = ClassRelationUtil.findAncestors(typeBinding);
		for (ITypeBinding ancestor : ancestors) {
			if (hasUnexpectedJUnitReference(ancestor)) {
				return true;
			}
		}
		return false;
	}

	private boolean hasUnexpectedTypeArgument(ITypeBinding typeBinding) {
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

	protected boolean isUnexpectedJUnitQualifiedName(String qualifiedName) {
		return isJUnitName(qualifiedName);
	}
}
