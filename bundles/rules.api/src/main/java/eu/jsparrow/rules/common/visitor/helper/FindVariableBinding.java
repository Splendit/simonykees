package eu.jsparrow.rules.common.visitor.helper;

import java.util.Optional;

import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.SimpleName;

import eu.jsparrow.rules.common.exception.UnresolvedBindingException;

/**
 * 
 * Utility class to find an optional variable binding object for a name.
 *
 */
public class FindVariableBinding {

	private static final String FORMAT_MESSAGE_UNRESOLVED_BINDING = "Could not resolve binding for name: {%s}."; //$NON-NLS-1$

	/**
	 * 
	 * @param simpleName
	 * @return an Optional storing an instance of IVariableBinding if a
	 *         IVariableBinding can be found for the the SimpleName given by the
	 *         parameter, otherwise an empty Optional. If a variable binding can
	 *         be excluded structurally, then no binding is resolved but an
	 *         empty optional is returned immediately. thus unnecessary calls of
	 *         {@link SimpleName#resolveBinding()} are avoided.
	 * @throws UnresolvedBindingException
	 */

	public static Optional<IVariableBinding> findVariableBinding(SimpleName simpleName)
			throws UnresolvedBindingException {

		if (ExcludeVariableBinding.isVariableBindingExcludedFor(simpleName)) {
			return Optional.empty();
		}

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
		return Optional.of(variableBinding);
	}

	private FindVariableBinding() {
		// private default constructor hiding implicit public one
	}
}
