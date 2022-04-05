package eu.jsparrow.core.visitor.unused;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;

public class UsefulAnnotations {

	private static final List<String> IGNORED_ANNOTATIONS = Collections.unmodifiableList(
			Arrays.asList(java.lang.Deprecated.class.getName(), java.lang.SuppressWarnings.class.getName()));

	private UsefulAnnotations() {
		/*
		 * Hide default constructor
		 */
	}

	public static boolean hasUsefulAnnotations(FieldDeclaration bodyDeclaration) {
		List<Annotation> annotationsOnBodyDeclaration = getAnnotationsOnBodyDeclaration(bodyDeclaration);
		return containsUsefulAnnotations(annotationsOnBodyDeclaration);
	}

	public static boolean hasUsefulAnnotations(MethodDeclaration methodDeclaration) {
		List<Annotation> annotationsOnBodyDeclaration = getAnnotationsOnBodyDeclaration(methodDeclaration);
		if (containsUsefulAnnotations(annotationsOnBodyDeclaration)) {
			return true;
		}

		List<SingleVariableDeclaration> parameters = ASTNodeUtil.convertToTypedList(methodDeclaration.parameters(),
				SingleVariableDeclaration.class);

		for (SingleVariableDeclaration parameter : parameters) {
			List<Annotation> annotationsOnParameter = ASTNodeUtil.convertToTypedList(parameter.modifiers(),
					Annotation.class);
			if (containsUsefulAnnotations(annotationsOnParameter)) {
				return true;
			}
		}
		return false;
	}

	private static List<Annotation> getAnnotationsOnBodyDeclaration(BodyDeclaration bodyDeclaration) {
		return ASTNodeUtil.convertToTypedList(bodyDeclaration.modifiers(), Annotation.class);
	}

	public static boolean containsUsefulAnnotations(List<Annotation> annotations) {
		for (Annotation annotation : annotations) {
			ITypeBinding typeBinding = annotation.resolveTypeBinding();
			if (!ClassRelationUtil.isContentOfTypes(typeBinding, IGNORED_ANNOTATIONS)) {
				return true;
			}
		}
		return false;
	}
}
