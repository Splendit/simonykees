package eu.jsparrow.core.visitor.unused.type;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.exception.visitor.*;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;

/**
 * Finds out whether there are references to a specified type declaration.
 * 
 * @since 4.10.0
 *
 */
public class TypeReferencesVisitor extends ASTVisitor {

	private static final Logger logger = LoggerFactory.getLogger(TypeReferencesVisitor.class);

	private AbstractTypeDeclaration targetTypeDeclaration;
	private String targetTypeIdentifier;

	private boolean typeReferenceFound = false;
	private boolean unresolvedReferenceFound = false;
	private ITypeBinding targetTypeBinding;

	/**
	 * Private instance method "getNonParameterizedTypeErasure" of class
	 * ChainAssertJAssertThatStatementsASTVisitor has been copied to here and
	 * the static modifier has been added.
	 */
	private static ITypeBinding getNonParameterizedTypeErasure(ITypeBinding typeBinding) {
		ITypeBinding erasure = typeBinding.getErasure();
		while (erasure.isParameterizedType()) {
			erasure = erasure.getErasure();
		}
		return erasure;
	}

	public TypeReferencesVisitor(AbstractTypeDeclaration targetTypeDeclaration) {
		this.targetTypeDeclaration = targetTypeDeclaration;

		SimpleName name = targetTypeDeclaration.getName();
		this.targetTypeIdentifier = name.getIdentifier();
		this.targetTypeBinding = getNonParameterizedTypeErasure(this.targetTypeDeclaration.resolveBinding());
	}

	@Override
	public boolean preVisit2(ASTNode node) {
		return !typeReferenceFound && !unresolvedReferenceFound;
	}

	@Override
	public boolean visit(SimpleName simpleName) {
		try {
			typeReferenceFound = isTargetTypeReference(simpleName);
		} catch (UnresolvedTypeBindingException | UnexpectedKindOfBindingException e) {
			logger.debug(e.getMessage(), e);
			unresolvedReferenceFound = true;
		}
		return false;
	}

	private boolean isTargetTypeReference(SimpleName simpleName)
			throws UnresolvedTypeBindingException, UnexpectedKindOfBindingException {

		if (simpleName.getLocationInParent() == TypeDeclaration.NAME_PROPERTY ||
				simpleName.getLocationInParent() == EnumDeclaration.NAME_PROPERTY ||
				simpleName.getLocationInParent() == EnumConstantDeclaration.NAME_PROPERTY ||
				simpleName.getLocationInParent() == AnnotationTypeDeclaration.NAME_PROPERTY ||
				simpleName.getLocationInParent() == AnnotationTypeMemberDeclaration.NAME_PROPERTY ||
				simpleName.getLocationInParent() == MethodDeclaration.NAME_PROPERTY ||
				simpleName.getLocationInParent() == VariableDeclarationFragment.NAME_PROPERTY ||
				simpleName.getLocationInParent() == SingleVariableDeclaration.NAME_PROPERTY) {
			return false;
		}

		String identifier = simpleName.getIdentifier();
		if (!identifier.equals(targetTypeIdentifier)) {
			return false;
		}

		IBinding binding = simpleName.resolveBinding();
		if (binding == null) {
			if (ASTNodeUtil.isLabel(simpleName)) {
				return false;
			}
			throw new UnresolvedTypeBindingException("The binding of the reference candidate cannot be resolved."); //$NON-NLS-1$
		}

		int kind = binding.getKind();
		if (kind == IBinding.PACKAGE) {
			return false;
		}
		if (kind == IBinding.TYPE) {
			ITypeBinding typeBinding = getNonParameterizedTypeErasure((ITypeBinding) binding);
			return ClassRelationUtil.compareITypeBinding(typeBinding, targetTypeBinding);
		}
		if (kind == IBinding.VARIABLE) {
			IVariableBinding variableBinding = (IVariableBinding) binding;
			if (variableBinding.isField()) {
				ITypeBinding declaringClass = getNonParameterizedTypeErasure(variableBinding.getDeclaringClass());
				if (ClassRelationUtil.compareITypeBinding(declaringClass, targetTypeBinding)) {
					return true;
				}
			}
			ITypeBinding variableType = getNonParameterizedTypeErasure(variableBinding.getType());
			return ClassRelationUtil.compareITypeBinding(variableType, targetTypeBinding);
		}
		if (kind == IBinding.METHOD) {
			return false;
		}
		throw new UnexpectedKindOfBindingException("Unexpected or unknown kind of binding."); //$NON-NLS-1$
	}

	public boolean typeReferenceFound() {
		return typeReferenceFound;
	}

	public boolean hasUnresolvedReference() {
		return unresolvedReferenceFound;
	}
}
