package eu.jsparrow.core.visitor.logger;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.TypeLiteral;

import eu.jsparrow.core.markers.common.ReplaceWrongClassForLoggerEvent;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * 
 * @since 4.13.0
 *
 */
public class ReplaceWrongClassForLoggerASTVisitor extends AbstractASTRewriteASTVisitor
		implements ReplaceWrongClassForLoggerEvent {

	@Override
	public boolean visit(TypeLiteral node) {
		AbstractTypeDeclaration surroundingTypeDeclaration = ASTNodeUtil.getSpecificAncestor(node,
				AbstractTypeDeclaration.class);
		if (ReplaceWrongClassForLoggerAnalyzer.isClassLiteralToReplace(node, surroundingTypeDeclaration)) {
			TypeLiteral typeLiteralReplacement = createTypeLiteralReplacement(surroundingTypeDeclaration);
			astRewrite.replace(node, typeLiteralReplacement, null);
			onRewrite();
			addMarkerEvent(node);
		}
		return false;
	}

	private TypeLiteral createTypeLiteralReplacement(AbstractTypeDeclaration surroundingTypeDeclaration) {

		AST ast = getASTRewrite().getAST();
		SimpleName newSimpleName = ast.newSimpleName(surroundingTypeDeclaration.getName()
			.getIdentifier());
		SimpleType newSimpleType = ast.newSimpleType(newSimpleName);
		TypeLiteral newTypeLiteral = ast.newTypeLiteral();
		newTypeLiteral.setType(newSimpleType);
		return newTypeLiteral;
	}
}
