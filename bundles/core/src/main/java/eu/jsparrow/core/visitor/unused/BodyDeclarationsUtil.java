package eu.jsparrow.core.visitor.unused;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Modifier;

import eu.jsparrow.core.rule.impl.unused.Constants;
import eu.jsparrow.core.visitor.renaming.JavaAccessModifier;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;

/**
 * A utility class providing functionalities to find or analyze the modifiers
 * and annotations of class member declarations.
 * 
 * @since 4.9.0
 *
 */
public class BodyDeclarationsUtil {

	private BodyDeclarationsUtil() {
		/*
		 * Hide default constructor
		 */
	}

	public static JavaAccessModifier findAccessModifier(BodyDeclaration bodyDeclaration) {
		int modifierFlags = bodyDeclaration.getModifiers();
		if (Modifier.isPrivate(modifierFlags)) {
			return JavaAccessModifier.PRIVATE;
		} else if (Modifier.isProtected(modifierFlags)) {
			return JavaAccessModifier.PROTECTED;
		} else if (Modifier.isPublic(modifierFlags)) {
			return JavaAccessModifier.PUBLIC;
		}
		return JavaAccessModifier.PACKAGE_PRIVATE;
	}

	public static boolean hasSelectedAccessModifier(BodyDeclaration bodyDeclaration, Map<String, Boolean> options) {
		int modifierFlags = bodyDeclaration.getModifiers();
		switch (bodyDeclaration.getNodeType()) {
		case ASTNode.METHOD_DECLARATION:
			return hasSelectedMethodDeclarationAccessModifier(options, modifierFlags);
		case ASTNode.FIELD_DECLARATION:
			return hasSelectedFieldDeclarationAccessModifier(options, modifierFlags);
		case ASTNode.TYPE_DECLARATION:
			return hasSelectedTypeDeclarationAccessModifier(options, modifierFlags);
		default:
			return false;
		}
	}

	private static boolean hasSelectedTypeDeclarationAccessModifier(Map<String, Boolean> options, int modifierFlags) {
		if (Modifier.isPublic(modifierFlags)) {
			return options.getOrDefault(Constants.PUBLIC_CLASSES, false);
		} else if (Modifier.isProtected(modifierFlags)) {
			return options.getOrDefault(Constants.PROTECTED_CLASSES, false);
		} else if (Modifier.isPrivate(modifierFlags)) {
			return options.getOrDefault(Constants.PRIVATE_CLASSES, false);
		} else {
			return options.getOrDefault(Constants.PACKAGE_PRIVATE_CLASSES, false);
		}
	}

	private static boolean hasSelectedFieldDeclarationAccessModifier(Map<String, Boolean> options, int modifierFlags) {
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

	private static boolean hasSelectedMethodDeclarationAccessModifier(Map<String, Boolean> options, int modifierFlags) {
		if (Modifier.isPublic(modifierFlags)) {
			return options.getOrDefault(Constants.PUBLIC_METHODS, false);
		} else if (Modifier.isProtected(modifierFlags)) {
			return options.getOrDefault(Constants.PROTECTED_METHODS, false);
		} else if (Modifier.isPrivate(modifierFlags)) {
			return options.getOrDefault(Constants.PRIVATE_METHODS, false);
		} else {
			return options.getOrDefault(Constants.PACKAGE_PRIVATE_METHODS, false);
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
