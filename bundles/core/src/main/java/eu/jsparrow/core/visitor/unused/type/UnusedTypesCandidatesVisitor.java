package eu.jsparrow.core.visitor.unused.type;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;

import eu.jsparrow.core.rule.impl.unused.Constants;
import eu.jsparrow.core.visitor.renaming.JavaAccessModifier;
import eu.jsparrow.core.visitor.unused.BodyDeclarationsUtil;
import eu.jsparrow.core.visitor.unused.UsefulAnnotations;
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
	public boolean visit(TypeDeclaration typeDeclaration) {

		if (typeDeclaration.getLocationInParent() == CompilationUnit.TYPES_PROPERTY) {
			IJavaElement javaElement = compilationUnit.getJavaElement();
			String mainClassName = javaElement.getElementName();
			int lastIndexOfFileExtension = mainClassName.lastIndexOf(".java"); //$NON-NLS-1$
			mainClassName = mainClassName.substring(0, lastIndexOfFileExtension);
			if (typeDeclaration.getName()
				.getIdentifier()
				.equals(mainClassName)) {
				int topLevelTypesCount = compilationUnit.types()
					.size();
				if (topLevelTypesCount > 1) {
					return true;
				}
			}
		}

		TypeDeclarationStatement typeDeclarationStatement = ASTNodeUtil.getSpecificAncestor(typeDeclaration,
				TypeDeclarationStatement.class);

		if (typeDeclarationStatement != null) {
			boolean localClassOption = options.getOrDefault(Constants.LOCAL_CLASSES, false);
			if (!localClassOption) {
				return false;
			}
		} else if (!BodyDeclarationsUtil.hasSelectedAccessModifier(typeDeclaration, options)) {
			return true;
		}

		ASTNode typeReferencesScope = typeDeclarationStatement != null ? typeDeclarationStatement.getParent()
				: compilationUnit;

		if (!analyzeUnusedTypeCandidate(typeDeclaration, typeReferencesScope)) {
			return true;
		}

		if (typeDeclarationStatement != null) {
			UnusedTypeWrapper unusedTypeWrapper = new UnusedTypeWrapper(compilationUnit,
					JavaAccessModifier.PRIVATE, typeDeclaration);
			unusedLocalTypes.add(unusedTypeWrapper);
			return false;
		}

		int modifierFlags = typeDeclaration.getModifiers();
		if (Modifier.isPrivate(modifierFlags)) {
			UnusedTypeWrapper unusedTypeWrapper = new UnusedTypeWrapper(compilationUnit,
					JavaAccessModifier.PRIVATE, typeDeclaration);
			unusedPrivateTypes.add(unusedTypeWrapper);
			return false;
		}

		JavaAccessModifier accessModifier = BodyDeclarationsUtil.findAccessModifier(typeDeclaration);
		NonPrivateUnusedTypeCandidate candidate = new NonPrivateUnusedTypeCandidate(typeDeclaration,
				accessModifier);
		nonPrivateCandidates.add(candidate);
		return false;

	}

	private boolean analyzeUnusedTypeCandidate(AbstractTypeDeclaration typeDeclaration, ASTNode typeReferencesScope) {
		boolean hasAnnotations = BodyDeclarationsUtil.hasUsefulAnnotations(typeDeclaration);
		if (hasAnnotations) {
			return false;
		}

		List<BodyDeclaration> bodyDeclarations = ASTNodeUtil.convertToTypedList(typeDeclaration.bodyDeclarations(),
				BodyDeclaration.class);

		for (BodyDeclaration bodyDeclaration : bodyDeclarations) {
			if (!isSupportedBodyDeclaration(bodyDeclaration)) {
				return false;
			}
		}

		TypeReferencesVisitor typeReferencesVisitor = new TypeReferencesVisitor(typeDeclaration, compilationUnit);
		typeReferencesScope.accept(typeReferencesVisitor);
		return !typeReferencesVisitor.typeReferenceFound() && !typeReferencesVisitor.hasUnresolvedReference();
	}

	private boolean isSupportedBodyDeclaration(BodyDeclaration bodyDeclaration) {
		if (bodyDeclaration.getNodeType() == ASTNode.FIELD_DECLARATION) {
			if (UsefulAnnotations.hasUsefulAnnotations((FieldDeclaration) bodyDeclaration)) {
				return false;
			}
		} else if (bodyDeclaration.getNodeType() == ASTNode.METHOD_DECLARATION) {
			if (UsefulAnnotations.hasUsefulAnnotations((MethodDeclaration) bodyDeclaration)) {
				return false;
			}
		} else if (bodyDeclaration.getNodeType() != ASTNode.INITIALIZER) {
			return false;
		}
		UnexpectedLocalDeclarationVisitor unexpectedLocalDeclarationVisitor = new UnexpectedLocalDeclarationVisitor();
		bodyDeclaration.accept(unexpectedLocalDeclarationVisitor);
		return !unexpectedLocalDeclarationVisitor.isUnexpectedLocalDeclarationFound();
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
