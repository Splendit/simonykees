package eu.jsparrow.core.visitor.unused;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.exception.visitor.UnresolvedTypeBindingException;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;

/**
 * Finds the references of a field declaration in a compilation unit. Determines
 * whether the reference is a safely removable reassignment.
 * 
 * @since 4.8.0
 *
 */
public class ReferencesVisitor extends ASTVisitor {

	private static final Logger logger = LoggerFactory.getLogger(ReferencesVisitor.class);

	private AbstractTypeDeclaration originalTypeDeclaration;
	private Map<String, Boolean> options;
	private String originalIdentifier;

	private boolean activeReferenceFound = false;
	private boolean unresolvedReferenceFound = false;
	private List<ExpressionStatement> reassignments = new ArrayList<>();
	private ITypeBinding originalType;

	public ReferencesVisitor(VariableDeclarationFragment originalFragment,
			AbstractTypeDeclaration originalTypeDeclaration,
			Map<String, Boolean> options) {
		this.originalTypeDeclaration = originalTypeDeclaration;
		this.options = options;
		SimpleName name = originalFragment.getName();
		this.originalIdentifier = name.getIdentifier();
		this.originalType = this.originalTypeDeclaration.resolveBinding();
	}

	@Override
	public boolean preVisit2(ASTNode node) {
		return !activeReferenceFound && !unresolvedReferenceFound;
	}

	@Override
	public boolean visit(SimpleName simpleName) {
		boolean isReference;
		try {
			isReference = isTargetFieldReference(simpleName);
		} catch (UnresolvedTypeBindingException e) {
			logger.debug(e.getMessage(), e);
			unresolvedReferenceFound = true;
			return false;
		}
		if (!isReference) {
			return false;
		}

		StructuralPropertyDescriptor locationInParent = simpleName.getLocationInParent();
		if (locationInParent == VariableDeclarationFragment.NAME_PROPERTY) {
			return false;
		}

		Expression outermostExpression = findOutermostExpression(simpleName);
		Optional<ExpressionStatement> reassignment = SafelyRemoveable.findSafelyRemovableReassignment(outermostExpression, options);
		reassignment.ifPresent(reassignments::add);
		if (reassignment.isPresent()) {
			return false;
		}

		activeReferenceFound = true;
		return true;
	}

	Expression findOutermostExpression(SimpleName simpleName) {
		StructuralPropertyDescriptor locationInParent = simpleName.getLocationInParent();
		Expression outermostExpression;

		if (locationInParent == FieldAccess.NAME_PROPERTY) {
			outermostExpression = (FieldAccess) simpleName.getParent();
		} else if (locationInParent == SuperFieldAccess.NAME_PROPERTY) {
			outermostExpression = (SuperFieldAccess) simpleName.getParent();
		} else if (locationInParent == QualifiedName.NAME_PROPERTY) {
			outermostExpression = (QualifiedName) simpleName.getParent();
		} else {
			outermostExpression = simpleName;
		}

		while (outermostExpression.getLocationInParent() == ArrayAccess.ARRAY_PROPERTY) {
			outermostExpression = (ArrayAccess) outermostExpression.getParent();
		}

		return outermostExpression;
	}

	private boolean isTargetFieldReference(SimpleName simpleName) throws UnresolvedTypeBindingException {
		String identifier = simpleName.getIdentifier();
		if (!identifier.equals(originalIdentifier)) {
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
		if (kind != IBinding.VARIABLE) {
			return false;
		}

		IVariableBinding variableBinding = (IVariableBinding) binding;
		if (!variableBinding.isField()) {
			return false;
		}
		ITypeBinding declaringClass = variableBinding.getDeclaringClass();
		if (declaringClass == null) {
			throw new UnresolvedTypeBindingException("The declaring class of the reference candidate cannot be found."); //$NON-NLS-1$
		}

		return ClassRelationUtil.isContentOfType(declaringClass, originalType.getQualifiedName());
	}

	public boolean hasActiveReference() {
		return activeReferenceFound;
	}

	public boolean hasUnresolvedReference() {
		return unresolvedReferenceFound;
	}

	public List<ExpressionStatement> getReassignments() {
		return this.reassignments;
	}
}
