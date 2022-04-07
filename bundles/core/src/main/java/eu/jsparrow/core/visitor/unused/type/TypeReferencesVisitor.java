package eu.jsparrow.core.visitor.unused.type;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;

/**
 * Finds out whether there are references to a specified type declaration.
 * 
 * @since 4.10.0
 *
 */
public class TypeReferencesVisitor extends ASTVisitor {

	private final AbstractTypeDeclaration targetTypeDeclaration;
	private String targetTypeIdentifier;
	private ITypeBinding targetTypeErasure;

	private boolean typeReferenceFound = false;
	private boolean unresolvedReferenceFound = false;

	/**
	 * Private instance method "getNonParameterizedTypeErasure" of class
	 * ChainAssertJAssertThatStatementsASTVisitor has been copied to here and
	 * the static modifier has been added.
	 */
	private static ITypeBinding getNonParameterizedTypeErasure(ITypeBinding typeBinding) {
		ITypeBinding erasure = typeBinding;
		while (erasure.isParameterizedType()) {
			erasure = erasure.getErasure();
		}
		return erasure;
	}

	public TypeReferencesVisitor(AbstractTypeDeclaration targetTypeDeclaration) {
		this.targetTypeDeclaration = targetTypeDeclaration;
		SimpleName name = targetTypeDeclaration.getName();
		this.targetTypeIdentifier = name.getIdentifier();
		this.targetTypeErasure = getNonParameterizedTypeErasure(targetTypeDeclaration.resolveBinding());
	}

	@Override
	public boolean preVisit2(ASTNode node) {
		return !typeReferenceFound && !unresolvedReferenceFound;
	}

	@Override
	public boolean visit(PackageDeclaration node) {
		return false;
	}

	@Override
	public boolean visit(ThisExpression node) {
		ITypeBinding typeBinding = node.resolveTypeBinding();
		if (typeBinding == null) {
			unresolvedReferenceFound = true;
		} else if (isActiveThisReference(node, typeBinding)) {
			typeReferenceFound = true;
		}
		return false;
	}

	@Override
	public boolean visit(SimpleName simpleName) {
		if (!isDeclarationNameProperty(simpleName) && simpleName.getIdentifier()
			.equals(targetTypeIdentifier)) {
			IBinding binding = simpleName.resolveBinding();
			if (binding != null) {
				typeReferenceFound = isActiveReferenceByName(simpleName, binding);

			} else if (!ASTNodeUtil.isLabel(simpleName)) {
				unresolvedReferenceFound = true;
			}
		}
		return false;
	}

	@Override
	public boolean visit(QualifiedName qualifiedName) {
		IBinding binding = qualifiedName.resolveBinding();
		if (binding == null) {
			unresolvedReferenceFound = true;
			return false;
		}

		if (binding.getKind() == IBinding.PACKAGE) {
			return false;
		}

		String simpleNameIdentifier = qualifiedName.getName()
			.getIdentifier();
		if (simpleNameIdentifier.equals(targetTypeIdentifier) && isActiveReferenceByName(qualifiedName, binding)) {
			typeReferenceFound = true;
			return false;
		}

		return true;
	}

	private boolean isActiveThisReference(ThisExpression thisExpression, ITypeBinding typeBinding) {

		if (!isBindingReferencingTargetType(typeBinding)) {
			return false;
		}

		StructuralPropertyDescriptor locationInParent = thisExpression.getLocationInParent();
		return locationInParent != FieldAccess.EXPRESSION_PROPERTY &&
				locationInParent != MethodInvocation.EXPRESSION_PROPERTY;

	}

	private boolean isDeclarationNameProperty(SimpleName simpleName) {
		return simpleName.getLocationInParent() == TypeDeclaration.NAME_PROPERTY ||
				simpleName.getLocationInParent() == EnumDeclaration.NAME_PROPERTY ||
				simpleName.getLocationInParent() == EnumConstantDeclaration.NAME_PROPERTY ||
				simpleName.getLocationInParent() == AnnotationTypeDeclaration.NAME_PROPERTY ||
				simpleName.getLocationInParent() == AnnotationTypeMemberDeclaration.NAME_PROPERTY ||
				simpleName.getLocationInParent() == MethodDeclaration.NAME_PROPERTY ||
				simpleName.getLocationInParent() == VariableDeclarationFragment.NAME_PROPERTY ||
				simpleName.getLocationInParent() == SingleVariableDeclaration.NAME_PROPERTY;
	}

	private boolean isActiveReferenceByName(Name name, IBinding binding) {
		int kind = binding.getKind();
		if (kind != IBinding.TYPE) {
			return false;
		}
		if (!isBindingReferencingTargetType((ITypeBinding) binding)) {
			return false;
		}

		if (name.getLocationInParent() == MethodInvocation.EXPRESSION_PROPERTY
				|| name.getLocationInParent() == QualifiedName.QUALIFIER_PROPERTY) {
			AbstractTypeDeclaration enclosingTypeDeclaration = ASTNodeUtil
				.getSpecificAncestor(name, AbstractTypeDeclaration.class);
			boolean isEnclosedByTargetType = enclosingTypeDeclaration == targetTypeDeclaration;
			if (isEnclosedByTargetType) {
				return false;
			}
		}

		return true;
	}

	private boolean isBindingReferencingTargetType(ITypeBinding typeBinding) {
		ITypeBinding typeErasure = getNonParameterizedTypeErasure(typeBinding);
		return ClassRelationUtil.compareITypeBinding(typeErasure, targetTypeErasure);
	}

	public boolean typeReferenceFound() {
		return typeReferenceFound;
	}

	public boolean hasUnresolvedReference() {
		return unresolvedReferenceFound;
	}
}
