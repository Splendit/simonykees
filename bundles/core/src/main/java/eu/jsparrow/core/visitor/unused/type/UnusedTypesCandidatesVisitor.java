package eu.jsparrow.core.visitor.unused.type;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;

import eu.jsparrow.core.rule.impl.unused.Constants;
import eu.jsparrow.core.visitor.renaming.JavaAccessModifier;
import eu.jsparrow.core.visitor.unused.BodyDeclarationsUtil;
import eu.jsparrow.rules.common.util.ASTNodeUtil;

/**
 * Analyzes type declarations. Verifies if they are used within the compilation
 * unit.
 * 
 * @since 4.10.0
 */
public class UnusedTypesCandidatesVisitor extends ASTVisitor {

	private CompilationUnit compilationUnit;
	private Map<String, Boolean> options;

	private List<UnusedTypeWrapper> unusedPrivateTypes = new ArrayList<>();
	private List<UnusedTypeWrapper> unusedLocalTypes = new ArrayList<>();
	private List<NonPrivateUnusedTypeCandidate> nonPrivateCandidates = new ArrayList<>();

	/**
	 * 
	 * @param options
	 *            expects the values defined in {@link Constants} as keys.
	 */
	public UnusedTypesCandidatesVisitor(Map<String, Boolean> options) {
		this.options = options;
	}

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		this.compilationUnit = compilationUnit;
		return true;
	}

	@Override
	public boolean visit(AnonymousClassDeclaration node) {
		return false;
	}

	@Override
	public boolean visit(TypeDeclarationStatement typeDeclarationStatement) {
		return true;
	}
	
	@Override
	public boolean visit(Block block) {
		return true;
	}
	
	@Override
	public boolean visit(TypeDeclaration typeDeclaration) {

		boolean hasAnnotations = BodyDeclarationsUtil.hasUsefulAnnotations(typeDeclaration);
		if (hasAnnotations) {
			return true;
		}

		TypeDeclarationStatement typeDeclarationStatement = ASTNodeUtil.getSpecificAncestor(typeDeclaration,
				TypeDeclarationStatement.class);
		if (typeDeclarationStatement != null) {
			boolean localClassOption = options.getOrDefault(Constants.LOCAL_CLASSES, false);
			if (!localClassOption) {
				return false;
			}
			TypeReferencesVisitor referencesVisitor = new TypeReferencesVisitor(typeDeclaration);
			typeDeclarationStatement.getParent()
				.accept(referencesVisitor);
			if (!referencesVisitor.typeReferenceFound() && !referencesVisitor.hasUnresolvedReference()) {
				UnusedTypeWrapper unusedField = new UnusedTypeWrapper(compilationUnit,
						JavaAccessModifier.PRIVATE, typeDeclaration);
				unusedLocalTypes.add(unusedField);
				return false;
			}
		} else {
			if (!BodyDeclarationsUtil.hasSelectedAccessModifier(typeDeclaration, options)) {
				return true;
			}
			TypeReferencesVisitor referencesVisitor = new TypeReferencesVisitor(typeDeclaration);
			this.compilationUnit.accept(referencesVisitor);
			if (!referencesVisitor.typeReferenceFound() && !referencesVisitor.hasUnresolvedReference()) {
				markAsUnusedInternally(typeDeclaration);
				return false;
			}
		}
		return true;
	}

	private void markAsUnusedInternally(AbstractTypeDeclaration typeDeclaration) {
		int modifierFlags = typeDeclaration.getModifiers();
		if (Modifier.isPrivate(modifierFlags)) {
			UnusedTypeWrapper unusedField = new UnusedTypeWrapper(compilationUnit,
					JavaAccessModifier.PRIVATE, typeDeclaration);
			unusedPrivateTypes.add(unusedField);
		} else {
			JavaAccessModifier accessModifier = BodyDeclarationsUtil.findAccessModifier(typeDeclaration);
			NonPrivateUnusedTypeCandidate candidate = new NonPrivateUnusedTypeCandidate(typeDeclaration,
					accessModifier);
			nonPrivateCandidates.add(candidate);
		}
	}

	public CompilationUnit getCompilationUnit() {
		return compilationUnit;
	}

	public List<UnusedTypeWrapper> getUnusedLocalTypes() {
		return unusedLocalTypes;
	}

	public List<UnusedTypeWrapper> getUnusedPrivateTypes() {
		return unusedPrivateTypes;
	}

	public List<NonPrivateUnusedTypeCandidate> getNonPrivateCandidates() {
		return nonPrivateCandidates;
	}
}
