package eu.jsparrow.core.visitor.impl.entryset;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Dimension;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import eu.jsparrow.rules.common.util.ASTNodeUtil;

class KeyVariableDeclarationData {

	private final Type keyType;
	private final String keyIdentifier;
	private List<Dimension> keyExtraDimensions;

	@SuppressWarnings("unchecked")
	static VariableDeclarationStatement createKeyVariableDeclaration(
			SingleVariableDeclaration singleVariableDeclaration) throws UnsupportedDimensionException {
		AST ast = singleVariableDeclaration.getAST();
		VariableDeclarationFragment declarationFragment = ast.newVariableDeclarationFragment();

		if (singleVariableDeclaration.getExtraDimensions() > 0) {

			int apiLevel = ast.apiLevel();
			boolean supportingExtraDimensions = SingleVariableDeclaration
				.propertyDescriptors(apiLevel)
				.contains(SingleVariableDeclaration.EXTRA_DIMENSIONS2_PROPERTY);

			if (!supportingExtraDimensions) {
				throw new UnsupportedDimensionException(
						"Cannot carry out operations with extra dimensions on apiLevel" + //$NON-NLS-1$
								apiLevel + " ."); //$NON-NLS-1$
			}

			declarationFragment.extraDimensions()
				.addAll(ASTNode.copySubtrees(ast,
						singleVariableDeclaration.extraDimensions()));
		}

		declarationFragment.setName((SimpleName) ASTNode.copySubtree(ast, singleVariableDeclaration.getName()));
		VariableDeclarationStatement declarationStatement = ast.newVariableDeclarationStatement(declarationFragment);
		declarationStatement.setType((Type) ASTNode.copySubtree(ast, singleVariableDeclaration.getType()));
		return declarationStatement;
	}

	static KeyVariableDeclarationData extractKeyVariableDeclarationData(
			SingleVariableDeclaration singleVariableDeclaration) throws UnsupportedDimensionException {

		Type keyType = singleVariableDeclaration.getType();
		String keyIdentifier = singleVariableDeclaration.getName()
			.getIdentifier();

		if (singleVariableDeclaration.getExtraDimensions() > 0) {
			AST ast = singleVariableDeclaration.getAST();
			int apiLevel = ast.apiLevel();
			boolean supportingExtraDimensions = SingleVariableDeclaration
				.propertyDescriptors(apiLevel)
				.contains(SingleVariableDeclaration.EXTRA_DIMENSIONS2_PROPERTY);

			if (!supportingExtraDimensions) {
				throw new UnsupportedDimensionException(
						"Cannot carry out operations with extra dimensions on apiLevel" + //$NON-NLS-1$
								apiLevel + " ."); //$NON-NLS-1$
			}
			List<Dimension> keyExtraDimensions = ASTNodeUtil
				.convertToTypedList(singleVariableDeclaration.extraDimensions(), Dimension.class);
			return new KeyVariableDeclarationData(keyType, keyIdentifier, keyExtraDimensions);
		}

		return new KeyVariableDeclarationData(keyType, keyIdentifier);
	}

	private KeyVariableDeclarationData(Type keyType, String keyIdentifier, List<Dimension> keyExtraDimensions) {
		this(keyType, keyIdentifier);
		this.keyExtraDimensions = keyExtraDimensions;

	}

	private KeyVariableDeclarationData(Type keyType, String keyIdentifier) {
		this.keyType = keyType;
		this.keyIdentifier = keyIdentifier;

	}

	Type getKeyType() {
		return keyType;
	}

	String getKeyIdentifier() {
		return keyIdentifier;
	}

	Optional<List<Dimension>> getKeyExtraDimensions() {
		return Optional.ofNullable(keyExtraDimensions);
	}
}
