package eu.jsparrow.rules.java16.javarecords;

import java.util.Optional;

import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import eu.jsparrow.rules.common.util.ASTNodeUtil;

class TypeVisibilityAnalyzer {

	static final int VISIBILITY_PRIVATE = 0;
	static final int VISIBILITY_PACKAGE = 1;
	static final int VISIBILITY_PROTECTED = 2;
	static final int VISIBILITY_PUBLIC = 3;
	private int effectiveVisibilityGrade;
	private AbstractTypeDeclaration privateAncestor;

	boolean analyzeEffectiveVisibility(TypeDeclaration typeDeclaration) {
		effectiveVisibilityGrade = getVisibilityGrade(typeDeclaration);
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
		if (effectiveVisibilityGrade < VISIBILITY_PROTECTED) {
			return true;
		}
		return false;
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
