package eu.jsparrow.core.visitor.unused;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.exception.visitor.UnresolvedTypeBindingException;
import eu.jsparrow.core.rule.impl.unused.Constants;

/**
 * Finds the references of a field declaration in a compilation unit. Determines
 * whether the reference is a safely removable reassignment.
 * 
 * @since 4.8.0
 *
 */
public class LocalVariablesReferencesVisitor extends ASTVisitor {

	private static final Logger logger = LoggerFactory.getLogger(LocalVariablesReferencesVisitor.class);
	private final CompilationUnit compilationUnit;
	private final VariableDeclarationFragment originalFragment;
	private final SimpleName originalFragmentName;
	private final String originalIdentifier;
	private final Map<String, Boolean> options;

	private boolean activeReferenceFound = false;
	private boolean unresolvedReferenceFound = false;
	private List<ExpressionStatement> reassignments = new ArrayList<>();

	public LocalVariablesReferencesVisitor(CompilationUnit compilationUnit,
			VariableDeclarationFragment originalFragment,
			Map<String, Boolean> options) {
		this.compilationUnit = compilationUnit;
		this.originalFragment = originalFragment;
		this.originalFragmentName = originalFragment.getName();
		this.originalIdentifier = originalFragmentName.getIdentifier();
		this.options = options;
	}

	@Override
	public boolean preVisit2(ASTNode node) {
		return !activeReferenceFound && !unresolvedReferenceFound;
	}

	@Override
	public boolean visit(SimpleName simpleName) {

		if (simpleName == originalFragment.getName()) {
			return false;
		}
		boolean isReference;
		try {
			isReference = isTargetLocalVariableReference(simpleName);
		} catch (UnresolvedTypeBindingException e) {
			logger.debug(e.getMessage(), e);
			unresolvedReferenceFound = true;
			return false;
		}
		if (!isReference) {
			return false;
		}

		Optional<ExpressionStatement> referencingStatementToRemove = findReferencingStatementToRemove(simpleName);
		referencingStatementToRemove.ifPresent(reassignments::add);
		if (referencingStatementToRemove.isPresent()) {
			return false;
		}

		activeReferenceFound = true;
		return true;
	}

	Optional<ExpressionStatement> findReferencingStatementToRemove(SimpleName simpleName) {
		StructuralPropertyDescriptor locationInParent = simpleName.getLocationInParent();
		boolean removeInitializersSideEffects = options.getOrDefault(Constants.REMOVE_INITIALIZERS_SIDE_EFFECTS, false);
		if (locationInParent == Assignment.LEFT_HAND_SIDE_PROPERTY) {
			Assignment assignment = (Assignment) simpleName.getParent();
			Optional<ExpressionStatement> optionalParentStatement = SafelyRemoveable
				.findParentStatementInBlock(assignment);
			if (optionalParentStatement.isPresent()
					&& (removeInitializersSideEffects || ExpressionWithoutSideEffectRecursive
						.isExpressionWithoutSideEffect(assignment.getRightHandSide()))) {
				return optionalParentStatement;
			}
		}
		return Optional.empty();
	}

	private boolean isTargetLocalVariableReference(SimpleName simpleName) throws UnresolvedTypeBindingException {
		String identifier = simpleName.getIdentifier();
		if (!identifier.equals(originalIdentifier)) {
			return false;
		}

		if (simpleName.getLocationInParent() == LabeledStatement.LABEL_PROPERTY
				|| simpleName.getLocationInParent() == ContinueStatement.LABEL_PROPERTY
				|| simpleName.getLocationInParent() == BreakStatement.LABEL_PROPERTY

		) {
			return false;
		}

		IBinding binding = simpleName.resolveBinding();
		if (binding == null) {
			throw new UnresolvedTypeBindingException("The binding of the reference candidate cannot be resolved."); //$NON-NLS-1$
		}
		int kind = binding.getKind();
		if (kind != IBinding.VARIABLE) {
			return false;
		}

		IVariableBinding variableBinding = (IVariableBinding) binding;
		if (variableBinding.isField()) {
			return false;
		}

		ASTNode declaringNode = compilationUnit.findDeclaringNode(variableBinding);
		if (declaringNode == null) {
			/*
			 * This should never happen if variableBinding is not null and not a
			 * field binding
			 */
			throw new UnresolvedTypeBindingException("Could not find fragment declaring local variable."); //$NON-NLS-1$
		}
		return declaringNode == originalFragment;
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
