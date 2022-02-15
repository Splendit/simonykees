package eu.jsparrow.core.visitor.unused;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.exception.visitor.UnresolvedTypeBindingException;
import eu.jsparrow.core.rule.impl.unused.Constants;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;

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
		if (locationInParent == Assignment.LEFT_HAND_SIDE_PROPERTY) {
			Assignment assignment = (Assignment) simpleName.getParent();
			Optional<ExpressionStatement> reassignment = isSafelyRemovable(assignment);
			reassignment.ifPresent(reassignments::add);
			if(reassignment.isPresent()) {
				return false;
			}

		} else if (locationInParent == FieldAccess.NAME_PROPERTY) {
			FieldAccess fieldAccess = (FieldAccess) simpleName.getParent();
			Optional<ExpressionStatement> reassignment = isSafelyRemovableReassignment(fieldAccess);
			reassignment.ifPresent(reassignments::add);
			if(reassignment.isPresent()) {
				return false;
			}
		} else if (locationInParent == QualifiedName.NAME_PROPERTY) {
			QualifiedName qualifiedName = (QualifiedName) simpleName.getParent();
			Optional<ExpressionStatement> reassignment = isSafelyRemovableReassignment(qualifiedName);
			reassignment.ifPresent(reassignments::add);
			if(reassignment.isPresent()) {
				return false;
			}
			
		}

		activeReferenceFound = true;
		return true;
	}
	
	private Optional<ExpressionStatement> isSafelyRemovableReassignment(Expression expression) {
		if (expression.getLocationInParent() == Assignment.LEFT_HAND_SIDE_PROPERTY) {
			Assignment assignment = (Assignment) expression.getParent();
			return isSafelyRemovable(assignment);
		}
		return Optional.empty();
	}
	
	/*
	 * FIXME:: move this method to class ExpressionWithoutSideEffectRecursive
	 */
	private Optional<ExpressionStatement> isSafelyRemovable(Assignment assignment) {
		if(assignment.getLocationInParent() != ExpressionStatement.EXPRESSION_PROPERTY) {
			return Optional.empty();
		}
		Expression rightHandSide = assignment.getRightHandSide();

		ExpressionStatement expressionStatement = (ExpressionStatement)assignment.getParent();
		boolean ignoreSideEffects = options.getOrDefault(Constants.REMOVE_INITIALIZERS_SIDE_EFFECTS, false);
		if(ignoreSideEffects) {
			return Optional.of(expressionStatement);
		}
		
		// FIXME: use ExpressionWithoutSideEffectRecursive#isExpressionWithoutSideEffect
		boolean isSimpleExpression = rightHandSide.getNodeType() == ASTNode.SIMPLE_NAME || ASTNodeUtil.isLiteral(rightHandSide);
		if(isSimpleExpression) {
			return Optional.of(expressionStatement);
		}
		return Optional.empty();
	}

	private boolean isTargetFieldReference(SimpleName simpleName) throws UnresolvedTypeBindingException {
		String identifier = simpleName.getIdentifier();
		if (!identifier.equals(originalIdentifier)) {
			return false;
		}

		IBinding binding = simpleName.resolveBinding();
		if(binding == null) {
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
		if(declaringClass == null) {
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
