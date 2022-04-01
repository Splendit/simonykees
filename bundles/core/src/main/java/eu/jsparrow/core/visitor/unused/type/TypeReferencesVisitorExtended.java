package eu.jsparrow.core.visitor.unused.type;

import java.util.Collections;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
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
public class TypeReferencesVisitorExtended extends ASTVisitor {

	private AbstractTypeDeclaration targetTypeDeclaration;

	private boolean typeReferenceFound = false;
	private boolean unresolvedReferenceFound = false;
	private ITypeBinding targetTypeBindingErasure;

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

	public TypeReferencesVisitorExtended(AbstractTypeDeclaration targetTypeDeclaration) {
		this.targetTypeDeclaration = targetTypeDeclaration;
		this.targetTypeBindingErasure = getNonParameterizedTypeErasure(this.targetTypeDeclaration.resolveBinding());
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
		ITypeBinding thisTypeBinding = node.resolveTypeBinding();
		if (thisTypeBinding == null) {
			unresolvedReferenceFound = true;
		} else if (isActiveThisReference(node, thisTypeBinding)) {
			typeReferenceFound = true;

		}
		return false;
	}

	@Override
	public boolean visit(SimpleName simpleName) {
		if (!isDeclarationNameProperty(simpleName)) {
			IBinding binding = simpleName.resolveBinding();
			if (binding != null) {
				typeReferenceFound = isTargetTypeReference(simpleName, binding);

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
		if (isTargetTypeReference(qualifiedName, binding)) {
			typeReferenceFound = true;
			return false;
		}
		return true;
	}

	private boolean isActiveThisReference(ThisExpression thisExpression, ITypeBinding thisTypeBinding) {
		
		if (isEqualToOrSubtypeOfTargetType(thisTypeBinding)) {
			StructuralPropertyDescriptor locationInParent = thisExpression.getLocationInParent();
			return locationInParent != FieldAccess.EXPRESSION_PROPERTY &&
					locationInParent != MethodInvocation.EXPRESSION_PROPERTY;
		}
		return false;
	}

	private boolean isTargetTypeReference(Name name, IBinding binding) {

		int kind = binding.getKind();
		if (kind == IBinding.TYPE) {
			return isEqualToOrSubtypeOfTargetType((ITypeBinding) binding);
		}
		if (kind == IBinding.VARIABLE) {
			return isActiveTypeReferenceByField(name, (IVariableBinding) binding);
		}
		if (kind == IBinding.METHOD) {
			return isActiveTypeReferenceByMethod(name, (IMethodBinding) binding);
		}

		// kind can only be one of the following which are supposed not to occur
		// for a simple name or not to be references to the given type
		// declaration:
		// kind == IBinding.PACKAGE
		// IBinding.ANNOTATION
		// IBinding.MEMBER_VALUE_PAIR
		// kind == IBinding.MODULE

		return false;
	}

	private boolean isEqualToOrSubtypeOfTargetType(ITypeBinding typeBinding) {
		ITypeBinding erasure = getNonParameterizedTypeErasure(typeBinding);
		if (ClassRelationUtil.compareITypeBinding(erasure, targetTypeBindingErasure)) {
			return true;
		}
		return ClassRelationUtil.isInheritingContentOfTypes(erasure,
				Collections.singletonList(targetTypeBindingErasure.getQualifiedName()));
	}

	private boolean isActiveTypeReferenceByField(Name name, IVariableBinding variableBinding) {
		if (isEqualToOrSubtypeOfTargetType(variableBinding.getType())) {
			return true;
		}
		if (isEnclosedByTargetTypeDeclaration(name)) {
			return false;
		}
		if (!variableBinding.isField()) {
			return false;
		}
		if (isEqualToOrSubtypeOfTargetType(variableBinding.getDeclaringClass())) {
			return true;
		}
		return false;
	}

	private boolean isActiveTypeReferenceByMethod(Name name, IMethodBinding methodBinding) {
		if (isEqualToOrSubtypeOfTargetType(methodBinding.getReturnType())) {
			return true;
		}
		if (isEnclosedByTargetTypeDeclaration(name)) {
			return false;
		}
		if (isEqualToOrSubtypeOfTargetType(methodBinding.getDeclaringClass())) {
			return true;
		}
		return false;
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

	private boolean isEnclosedByTargetTypeDeclaration(Name name) {
		AbstractTypeDeclaration enclosingTypeDeclaration = ASTNodeUtil
			.getSpecificAncestor(name, AbstractTypeDeclaration.class);
		return enclosingTypeDeclaration == targetTypeDeclaration;
	}

	public boolean typeReferenceFound() {
		return typeReferenceFound;
	}

	public boolean hasUnresolvedReference() {
		return unresolvedReferenceFound;
	}
}
