package eu.jsparrow.core.visitor.optional;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;

import eu.jsparrow.rules.common.util.ASTNodeUtil;

public class OptionalGetVisitor extends ASTVisitor {

	private static final String GET = "get"; //$NON-NLS-1$

	private Expression optional;
	private List<MethodInvocation> getInvocations = new ArrayList<>();
	private ASTMatcher matcher = new ASTMatcher();

	public OptionalGetVisitor(Expression optional) {
		this.optional = optional;
	}

	@Override
	public boolean visit(MethodInvocation node) {
		SimpleName name = node.getName();
		if (!GET.equals(name.getIdentifier())) {
			return true;
		}

		List<Expression> arguments = ASTNodeUtil.convertToTypedList(node.arguments(), Expression.class);
		if (!arguments.isEmpty()) {
			return true;
		}

		Expression expression = node.getExpression();
		if (expression == null) {
			return false;
		}

		if (optional.subtreeMatch(matcher, expression)) {
			getInvocations.add(node);
		}

		return true;
	}

	public List<MethodInvocation> getInvocations() {
		return getInvocations;
	}

}
