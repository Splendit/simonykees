package eu.jsparrow.core.visitor.logger;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.TypeLiteral;

import eu.jsparrow.rules.common.exception.UnresolvedTypeBindingException;

/**
 * A {@link TypeLiteral} is regarded as foreign type literal if it is not
 * referencing the enclosing {@link AbstractTypeDeclaration}
 * 
 */
class ForeignTypeLiteral {

	/**
	 * 
	 * @param typeLiteral
	 * @param enclosingTypeDeclaration
	 * @return {@code true } if the given {@link TypeLiteral} is not referencing
	 *         the given {@link AbstractTypeDeclaration}, otherwise false;
	 */
	static boolean isForeignTypeLiteral(TypeLiteral typeLiteral, AbstractTypeDeclaration enclosingTypeDeclaration,
			CompilationUnit compilationUnit)
			throws UnresolvedTypeBindingException {
		ITypeBinding typeLiteralBinding = typeLiteral.getType()
			.resolveBinding();
		if (typeLiteralBinding == null) {
			throw new UnresolvedTypeBindingException(
					String.format("Cannot resolve type binding for type literal {%s}.", typeLiteral.toString())); //$NON-NLS-1$
		}
		ASTNode declaringNode = compilationUnit.findDeclaringNode(typeLiteralBinding);
		if (declaringNode == null) {
			return true;
		}
		return declaringNode != enclosingTypeDeclaration;
	}

	private ForeignTypeLiteral() {
		// private default constructor hiding implicit public one
	}

}
