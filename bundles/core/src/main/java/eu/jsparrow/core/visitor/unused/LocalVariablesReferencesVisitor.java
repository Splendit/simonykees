package eu.jsparrow.core.visitor.unused;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.exception.visitor.DeclaringNodeNotFoundException;
import eu.jsparrow.rules.common.exception.UnresolvedBindingException;
import eu.jsparrow.rules.common.util.ASTNodeUtil;

/**
 * Finds the references on a local variable in a block. Determines whether all
 * references belong to expression statements which can be removed without side
 * effect.
 * 
 * @since 4.9.0
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
		} catch (UnresolvedBindingException | DeclaringNodeNotFoundException e) {
			logger.debug(e.getMessage(), e);
			unresolvedReferenceFound = true;
			return false;
		}
		if (!isReference) {
			return false;
		}

		Expression outermostExpression = simpleName;
		while (outermostExpression.getLocationInParent() == ArrayAccess.ARRAY_PROPERTY) {
			outermostExpression = (ArrayAccess) outermostExpression.getParent();
		}

		Optional<ExpressionStatement> referencingStatementToRemove = SafelyRemoveable
			.findSafelyRemovableReassignment(outermostExpression, options);
		referencingStatementToRemove.ifPresent(reassignments::add);
		if (referencingStatementToRemove.isPresent()) {
			return false;
		}

		activeReferenceFound = true;
		return true;
	}

	private boolean isTargetLocalVariableReference(SimpleName simpleName)
			throws UnresolvedBindingException, DeclaringNodeNotFoundException {
		String identifier = simpleName.getIdentifier();
		if (!identifier.equals(originalIdentifier)) {
			return false;
		}

		IBinding binding = simpleName.resolveBinding();
		if (binding == null) {
			if (ASTNodeUtil.isLabel(simpleName)) {
				return false;
			}
			throw new UnresolvedBindingException("The binding of the reference candidate cannot be resolved."); //$NON-NLS-1$
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
			 * field binding.
			 */
			throw new DeclaringNodeNotFoundException("Could not find fragment declaring local variable."); //$NON-NLS-1$
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
