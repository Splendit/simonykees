package eu.jsparrow.core.visitor.unused.type;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.text.edits.TextEditGroup;

import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

public class RemoveUnusedTypesASTVisitor extends AbstractASTRewriteASTVisitor {

	private List<UnusedTypeWrapper> unusedTypes;
	private Map<AbstractTypeDeclaration, UnusedTypeWrapper> relevantDeclarations;

	public RemoveUnusedTypesASTVisitor(List<UnusedTypeWrapper> unusedTypes) {
		this.unusedTypes = unusedTypes;
	}

	@Override
	public boolean visit(CompilationUnit compilationUnit) {

		IPath currentPath = compilationUnit.getJavaElement()
			.getPath();

		Map<AbstractTypeDeclaration, UnusedTypeWrapper> declarations = new HashMap<>();

		for (UnusedTypeWrapper unusedTypeWrapper : unusedTypes) {
			IPath path = unusedTypeWrapper.getDeclarationPath();
			if (path.equals(currentPath)) {
				AbstractTypeDeclaration declaration = unusedTypeWrapper.getTypeDeclaration();
				declarations.put(declaration, unusedTypeWrapper);
			}
		}
		this.relevantDeclarations = declarations;
		return super.visit(compilationUnit);

	}

	@Override
	public boolean visit(TypeDeclaration typeDeclaration) {
		UnusedTypeWrapper designated = isDesignatedForRemoval(typeDeclaration, relevantDeclarations)
			.orElse(null);
		if (designated != null) {
			TextEditGroup editGroup = designated.getTextEditGroup((ICompilationUnit) getCompilationUnit()
				.getJavaElement());
			ASTNode nodeToRemove = typeDeclaration;
			if (nodeToRemove.getLocationInParent() == TypeDeclarationStatement.DECLARATION_PROPERTY) {
				astRewrite.remove(typeDeclaration.getParent(), editGroup);
			} else {
				astRewrite.remove(typeDeclaration, editGroup);
			}
			onRewrite();
		}
		return true;
	}

	private Optional<UnusedTypeWrapper> isDesignatedForRemoval(AbstractTypeDeclaration typeDeclaration,
			Map<AbstractTypeDeclaration, UnusedTypeWrapper> map) {
		ITypeBinding typeBinding = typeDeclaration.resolveBinding();
		for (Map.Entry<AbstractTypeDeclaration, UnusedTypeWrapper> entry : map.entrySet()) {
			AbstractTypeDeclaration relevantDeclaration = entry.getKey();
			ITypeBinding unusedTypeBinding = relevantDeclaration.resolveBinding();
			UnusedTypeWrapper unusedTypeWrapper = entry.getValue();
			if (typeBinding.isEqualTo(unusedTypeBinding)) {
				return Optional.of(unusedTypeWrapper);
			}
		}
		return Optional.empty();
	}

}
