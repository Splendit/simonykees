package eu.jsparrow.rules.java16.javarecords;

import java.util.Optional;

import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import eu.jsparrow.rules.common.util.ASTNodeUtil;

/**
 * Helper class to decide whether a class declaration can be transformed to a
 * record on the base of its effective visibility which can only be private or
 * package scope.
 * 
 * @see #analyzeEffectiveVisibility(TypeDeclaration)
 * 
 * @since 4.5.0
 */
class EffectiveVisibilityAnalyzer {

	static final int VISIBILITY_PRIVATE = 0;
	static final int VISIBILITY_PACKAGE = 1;
	static final int VISIBILITY_PROTECTED = 2;
	static final int VISIBILITY_PUBLIC = 3;
	private AbstractTypeDeclaration privateAncestor;

	/**
	 * <b>Effective visibility is defined like the following:</b>
	 * <p>
	 * If a type class declaration is a nested class, then the effective
	 * visibility is the visibility of the surrounding type declaration which
	 * has the lowest visibility. <br>
	 * For example if a nested class is declared as public and is enclosed by
	 * another nested class which is private, then the effective visibility is
	 * private.
	 * <p>
	 * The effective visibility of a private class is always private, and the
	 * effective visibility of a top level class is the same as the visibility
	 * specified by its visibility modifier.
	 * 
	 * @return true if the effective visibility is private or package scope,
	 *         otherwise false.
	 */
	boolean analyzeEffectiveVisibility(TypeDeclaration typeDeclaration) {
		int effectiveVisibilityGrade = getVisibilityGrade(typeDeclaration);
		if (effectiveVisibilityGrade == VISIBILITY_PRIVATE) {
			return true;
		}
		AbstractTypeDeclaration ancestorType = ASTNodeUtil.getSpecificAncestor(typeDeclaration,
				AbstractTypeDeclaration.class);
		while (ancestorType != null) {
			int ancestorVisibilityGrade = getVisibilityGrade(ancestorType);
			effectiveVisibilityGrade = Math.min(effectiveVisibilityGrade, ancestorVisibilityGrade);
			if (ancestorVisibilityGrade == VISIBILITY_PRIVATE) {
				privateAncestor = ancestorType;
				return true;
			}
			ancestorType = ASTNodeUtil.getSpecificAncestor(ancestorType, AbstractTypeDeclaration.class);
		}
		return effectiveVisibilityGrade < VISIBILITY_PROTECTED;
	}

	static int getVisibilityGrade(BodyDeclaration bodyDeclaration) {
		int modifiers = bodyDeclaration.getModifiers();
		if (Modifier.isPrivate(modifiers)) {
			return VISIBILITY_PRIVATE;
		}
		if (Modifier.isProtected(modifiers)) {
			return VISIBILITY_PROTECTED;
		}
		if (Modifier.isPublic(modifiers)) {
			return VISIBILITY_PUBLIC;
		}
		return VISIBILITY_PACKAGE;
	}

	Optional<AbstractTypeDeclaration> getPrivateAncestor() {
		return Optional.ofNullable(privateAncestor);
	}
}
