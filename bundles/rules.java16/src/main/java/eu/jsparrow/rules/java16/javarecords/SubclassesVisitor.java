package eu.jsparrow.rules.java16.javarecords;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import eu.jsparrow.rules.common.util.ClassRelationUtil;

/**
 * Helper class finding out whether a non-final nested or local class
 * declaration is effectivelsFinal or not.
 * <p>
 * A class declaration is effectively final if it can be excluded that it is
 * extended by any subclass.
 * 
 * @since 4.5.0
 *
 */
class SubclassesVisitor extends ASTVisitor {

	private final TypeDeclaration rootTypeDeclaration;
	private final String rootTypeQualifiedName;
	private boolean subclassExisting;

	SubclassesVisitor(TypeDeclaration rootTypeDeclaration) {
		this.rootTypeDeclaration = rootTypeDeclaration;
		this.rootTypeQualifiedName = rootTypeDeclaration.resolveBinding()
			.getErasure()
			.getQualifiedName();
	}

	@Override
	public boolean preVisit2(ASTNode node) {
		return !subclassExisting;
	}

	@Override
	public boolean visit(TypeDeclaration node) {

		if (node == rootTypeDeclaration) {
			return true;
		}

		if (node.isInterface()) {
			return true;
		}

		Type superclassType = node.getSuperclassType();
		if (superclassType == null) {
			return true;
		}

		if (ClassRelationUtil.isContentOfType(superclassType.resolveBinding(), rootTypeQualifiedName)) {
			subclassExisting = true;
			return false;
		}
		return true;
	}

	boolean isSubclassExisting() {
		return subclassExisting;
	}
}
