package eu.jsparrow.rules.common.visitor.helper;

import java.util.Optional;
import java.util.function.Predicate;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import eu.jsparrow.rules.common.exception.UnresolvedBindingException;

/**
 * Helper class to find the {@link ASTNode} which represents the declaration of
 * a variable.
 *
 */
public class NodeDeclaringVariableHelper {

	private static final Predicate<IVariableBinding> PREDICATE_LOCAL_VARIABLE = NodeDeclaringVariableHelper::isLocalVariable;
	private static final String FORMAT_MESSAGE_UNRESOLVED_BINDING = "Could not resolve binding for name: {%s}."; //$NON-NLS-1$
	private final CompilationUnit compilationUnit;

	private static boolean isLocalVariable(IVariableBinding variableBinding) {
		return !variableBinding.isField() && !variableBinding.isParameter();
	}

	public NodeDeclaringVariableHelper(CompilationUnit compilationUnit) {
		this.compilationUnit = compilationUnit;
	}

	/**
	 * 
	 * @param simpleName
	 * @return an Optional storing an instance of VariableDeclarationFragment
	 *         which represents the declaration of the local variable referenced
	 *         by the SimpleName given by the parameter, or an empty Optional,
	 *         if no such VariableDeclarationFragment is available.
	 * @throws UnresolvedBindingException
	 */

	public Optional<VariableDeclarationFragment> findFragmentDeclaringLocalVariable(SimpleName simpleName)
			throws UnresolvedBindingException {
		return findFragmentDeclaringVariable(simpleName, PREDICATE_LOCAL_VARIABLE);
	}

	public Optional<VariableDeclarationFragment> findFragmentDeclaringVariable(SimpleName simpleName,
			Predicate<IVariableBinding> variableBindingPredicate)
			throws UnresolvedBindingException {
		final IBinding binding = simpleName.resolveBinding();
		if (binding == null) {
			final String message = String.format(FORMAT_MESSAGE_UNRESOLVED_BINDING, simpleName.getIdentifier());
			throw new UnresolvedBindingException(message);
		}
		int kind = binding.getKind();
		if (kind != IBinding.VARIABLE) {
			return Optional.empty();
		}
		IVariableBinding variableBinding = (IVariableBinding) binding;
		if (!variableBindingPredicate.test(variableBinding)) {
			return Optional.empty();
		}
		final ASTNode declaringNode = compilationUnit.findDeclaringNode(variableBinding);
		if (declaringNode == null) {
			return Optional.empty();
		}
		if (declaringNode.getNodeType() != ASTNode.VARIABLE_DECLARATION_FRAGMENT) {
			return Optional.empty();
		}
		return Optional.of((VariableDeclarationFragment) declaringNode);
	}
}
