package eu.jsparrow.core.visitor.security;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

/**
 * Determines whether or not the visited AST node contains at least one type
 * declaration having the specified simple name. <br>
 * The type declaration may be a top level type as well as an inner type as well
 * as a local type.
 * 
 * 
 * @since 3.17.0
 *
 */
class FindTypeDeclarationBySimpleNameASTVisitor extends ASTVisitor {

	private final String simpleName;

	private boolean foundTypeDeclarationWithSimpleName;

	/**
	 * Constructs a visitor which will able to determine whether a type
	 * declaration exists in the visited AST node which has the simple name
	 * given by the constructor parameter.
	 * 
	 * @param simpleName
	 *            expected to be a simple type name without type arguments and
	 *            without dimension.
	 */
	public FindTypeDeclarationBySimpleNameASTVisitor(String simpleName) {
		this.simpleName = simpleName;
	}

	@Override
	public boolean visit(TypeDeclaration typeDeclaration) {
		foundTypeDeclarationWithSimpleName = hasSpecifiedSimpleName(typeDeclaration);
		return !foundTypeDeclarationWithSimpleName;
	}

	@Override
	public boolean visit(EnumDeclaration typeDeclaration) {
		foundTypeDeclarationWithSimpleName = hasSpecifiedSimpleName(typeDeclaration);
		return !foundTypeDeclarationWithSimpleName;
	}

	@Override
	public boolean visit(AnnotationTypeDeclaration typeDeclaration) {
		foundTypeDeclarationWithSimpleName = hasSpecifiedSimpleName(typeDeclaration);
		return !foundTypeDeclarationWithSimpleName;
	}

	private boolean hasSpecifiedSimpleName(AbstractTypeDeclaration typeDeclaration) {
		return this.simpleName.equals(typeDeclaration.getName()
			.getIdentifier());
	}

	public boolean hasFoundTypeDeclarationWithSimpleName() {
		return foundTypeDeclarationWithSimpleName;
	}

}
