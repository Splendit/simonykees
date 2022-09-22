package eu.jsparrow.core.visitor.loop.stream;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

/**
 * visits blocks and checks their validity
 * 
 * @see {@link EnhancedForLoopToStreamForEachASTVisitor}
 * @author Matthias Webhofer
 * @since 1.2
 */
public class StreamForEachCheckValidStatementASTVisitor extends ASTVisitor {

	/*
	 * helper fields
	 */
	private List<SimpleName> variableNames = new LinkedList<>();
	private Map<SimpleName, Integer> parameters = new HashMap<>();

	private List<IVariableBinding> invalidVariables = new LinkedList<>();

	public StreamForEachCheckValidStatementASTVisitor(SimpleName parameter) {
		this.parameters.put(parameter, 0);
	}

	@Override
	public boolean visit(VariableDeclarationFragment variableDeclarationFragmentNode) {
		/*
		 * search local variables
		 */
		variableNames.add(variableDeclarationFragmentNode.getName());
		return true;
	}

	@Override
	public boolean visit(EnhancedForStatement node) {
		this.parameters.entrySet()
			.stream()
			.forEach(entry -> entry.setValue(entry.getValue() + 1));
		this.parameters.put(node.getParameter()
			.getName(), 0);
		return true;
	}

	@Override
	public void endVisit(EnhancedForStatement node) {
		this.parameters.remove(node.getParameter()
			.getName());
		this.parameters.entrySet()
			.stream()
			.forEach(entry -> entry.setValue(entry.getValue() - 1));
	}

	@Override
	public boolean visit(SimpleName simpleNameNode) {

		/*
		 * only local, final or effectively final variables or fields are
		 * allowed.
		 */
		IBinding binding = simpleNameNode.resolveBinding();
		if (binding instanceof IVariableBinding) {
			IVariableBinding variableBinding = (IVariableBinding) binding;
			boolean isField = variableBinding.isField();
			boolean isFinal = Modifier.isFinal(variableBinding.getModifiers());
			boolean isEffectivelyFinal = variableBinding.isEffectivelyFinal();
			boolean isLocalVariable = variableNames.stream()
				.anyMatch(var -> var.getIdentifier()
					.equals(simpleNameNode.getIdentifier()));

			/*
			 * if the value is 0, then the parameter is treated as a local
			 * variable. a higher value indicates a nested loop where a separate
			 * check for final or effectively final has to be done.
			 */
			boolean isEnhancedForParameter = parameters.entrySet()
				.stream()
				.anyMatch(entry -> {
					boolean result = false;

					if (entry.getKey()
						.getIdentifier()
						.equals(simpleNameNode.getIdentifier())) {
						if (entry.getValue() > 0) {
							IVariableBinding entryBinding = (IVariableBinding) entry.getKey()
								.resolveBinding();
							if (entryBinding.isEffectivelyFinal() || Modifier.isFinal(entryBinding.getModifiers())) {
								result = true;
							}
						} else {
							result = true;
						}
					}

					return result;
				});

			if (!(isField || isFinal || isEffectivelyFinal || isLocalVariable || isEnhancedForParameter)) {
				invalidVariables.add(variableBinding);
			}
		}
		return false;
	}

	public boolean containsInvalidVariable() {
		return !invalidVariables.isEmpty();
	}
}
