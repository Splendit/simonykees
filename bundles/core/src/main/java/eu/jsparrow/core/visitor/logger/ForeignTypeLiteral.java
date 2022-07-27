package eu.jsparrow.core.visitor.logger;

import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.TypeLiteral;

import eu.jsparrow.rules.common.util.ClassRelationUtil;

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
	static boolean isForeignTypeLiteral(TypeLiteral typeLiteral, AbstractTypeDeclaration enclosingTypeDeclaration) {
		ITypeBinding typeLiteralBinding = typeLiteral.getType()
			.resolveBinding();
		ITypeBinding surroundingTypeDeclarationBinding = enclosingTypeDeclaration.resolveBinding();
		return !ClassRelationUtil.compareITypeBinding(typeLiteralBinding, surroundingTypeDeclarationBinding);
	}

	private ForeignTypeLiteral() {
		// private default constructor hiding implicit public one
	}

}
