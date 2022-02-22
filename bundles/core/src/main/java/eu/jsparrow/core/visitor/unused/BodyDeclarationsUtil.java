package eu.jsparrow.core.visitor.unused;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;

import eu.jsparrow.core.rule.impl.unused.Constants;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;

public class BodyDeclarationsUtil {
	
	private BodyDeclarationsUtil() {
		/*
		 * Hide default constructor
		 */
	}

	public static boolean hasSelectedAccessModifier(BodyDeclaration methodDeclaration, Map<String, Boolean>options) {
		int modifierFlags = methodDeclaration.getModifiers();
		if (Modifier.isPublic(modifierFlags)) {
			return options.getOrDefault(Constants.PUBLIC_FIELDS, false);
		} else if (Modifier.isProtected(modifierFlags)) {
			return options.getOrDefault(Constants.PROTECTED_FIELDS, false);
		} else if (Modifier.isPrivate(modifierFlags)) {
			return options.getOrDefault(Constants.PRIVATE_FIELDS, false);
		} else {
			return options.getOrDefault(Constants.PACKAGE_PRIVATE_FIELDS, false);
		}
	}
	
	public static boolean hasUsefulAnnotations(BodyDeclaration methodDeclaration) {
		List<Annotation> annotations = ASTNodeUtil.convertToTypedList(methodDeclaration.modifiers(), Annotation.class);
		for (Annotation annotation : annotations) {
			ITypeBinding typeBinding = annotation.resolveTypeBinding();
			if (!ClassRelationUtil.isContentOfTypes(typeBinding,
					Arrays.asList(java.lang.Deprecated.class.getName(), java.lang.SuppressWarnings.class.getName()))) {
				return true;
			}
		}

		return false;
	}
}
