package eu.jsparrow.core.visitor.renaming;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import eu.jsparrow.rules.common.util.ASTNodeUtil;

/**
 * an enum representing the java access modifiers
 * 
 * @author Matthias Webhofer
 * @since 2.4.0
 */
public enum JavaAccessModifier {
	PUBLIC("public"), //$NON-NLS-1$
	PROTECTED("protected"), //$NON-NLS-1$
	PRIVATE("private"), //$NON-NLS-1$
	PACKAGE_PRIVATE("package-private"); //$NON-NLS-1$

	private final String modifier;

	private JavaAccessModifier(String s) {
		modifier = s;
	}

	@Override
	public String toString() {
		return this.modifier;
	}

	public static Optional<JavaAccessModifier> findModifier(VariableDeclarationFragment fragment) {
		if(fragment.getLocationInParent() != FieldDeclaration.FRAGMENTS_PROPERTY) {
			return Optional.empty();
		}
		FieldDeclaration field = (FieldDeclaration) fragment.getParent();
		List<Modifier> modifiers = ASTNodeUtil.convertToTypedList(field.modifiers(), Modifier.class);
		if(modifiers.size() == 1) {
			Modifier fieldModifier = modifiers.get(0);
			if(fieldModifier.isPrivate()) {
				return Optional.of(PRIVATE);
			} else if(fieldModifier.isProtected()) {
				return Optional.of(PROTECTED);
			} else  {
				return Optional.of(PUBLIC);
			}
		} else {
			return Optional.of(PACKAGE_PRIVATE);
		}
	}
}
