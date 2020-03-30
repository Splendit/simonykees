package eu.jsparrow.core.visitor.security;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;

public class QueryComponentsAnalyzer {

	private List<Expression> components;
	private List<ReplaceableParameter> parameters = new ArrayList<>();

	public QueryComponentsAnalyzer(List<Expression> components) {
		this.components = components;
	}

	public boolean analyze() {
		List<Expression> nonLiteralComponents = components.stream()
				.filter(component -> component.getNodeType() != ASTNode.STRING_LITERAL)
				//.filter(component -> isNotFinal(component))
				.collect(Collectors.toList());

		/*
		 * 1. If it is final variable then skip it. 2.Prepare a list of parameters and
		 * the affected literals.
		 */
		for (Expression component : nonLiteralComponents) {
			int index = components.indexOf(component);
			StringLiteral previous = findPrevious(index);
			if (previous != null) {
				StringLiteral next = findNext(index);
				if (next != null) {
					this.parameters.add(new ReplaceableParameter(previous, next, component));
				}
			}
		}

		return true;
	}

	private StringLiteral findNext(int index) {
		int nextIndex = index + 1;
		if (components.size() <= nextIndex) {
			return null;
		}
		Expression next = components.get(nextIndex);
		if (next.getNodeType() != ASTNode.STRING_LITERAL) {
			return null;
		}
		StringLiteral literal = (StringLiteral) next;
		String value = literal.getLiteralValue();
		return value.startsWith("'") ? literal : null; //$NON-NLS-1$
	}

	private StringLiteral findPrevious(int index) {
		int previousIndex = index - 1;
		if (previousIndex < 0) {
			return null;
		}
		Expression previous = components.get(previousIndex);
		if (previous.getNodeType() != ASTNode.STRING_LITERAL) {
			return null;
		}
		StringLiteral stringLiteral = (StringLiteral) previous;
		String value = stringLiteral.getLiteralValue();
		return value.endsWith("'") ? stringLiteral : null; //$NON-NLS-1$
	}

	private boolean isNotFinal(Expression component) {
		int nodeType = component.getNodeType();
		if (nodeType != ASTNode.SIMPLE_NAME) {
			return false;
		}
		SimpleName simpleName = (SimpleName) component;
		IBinding binding = simpleName.resolveBinding();
		if (binding.getKind() == IBinding.VARIABLE) {
			IVariableBinding variableBinding = (IVariableBinding) binding;
			Modifier.isFinal(binding.getModifiers());
		}
		ITypeBinding typeBinding = component.resolveTypeBinding();
		return false;
	}

	public Map<Expression, Expression> getReplacements() {
		return new HashMap<>();
	}

	public List<ReplaceableParameter> getReplaceableParameters() {
		return this.parameters;
	}

}
