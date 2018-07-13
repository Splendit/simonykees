package eu.jsparrow.core.visitor.optional;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import eu.jsparrow.rules.common.util.ASTNodeUtil;

public class OptionalGetVisitor extends ASTVisitor {

	private static final String GET = "get"; //$NON-NLS-1$

	private Expression optional;
	private List<MethodInvocation> getInvocations = new ArrayList<>();
	private List<SimpleName> assignedWithGet = new ArrayList<>();
	private List<SimpleName> references = new ArrayList<>();
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

		if (!optional.subtreeMatch(matcher, expression)) {
			return true;
		}

		getInvocations.add(node);
		if (VariableDeclarationFragment.INITIALIZER_PROPERTY == node.getLocationInParent()) {
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) node.getParent();
			assignedWithGet.add(fragment.getName());
		}

		return true;
	}

	@Override
	public boolean visit(SimpleName simpleName) {
		IBinding binding = simpleName.resolveBinding();
		int kind = binding.getKind();
		if (kind != IBinding.VARIABLE) {
			return false;
		}

		if (!isToBeRenamed(simpleName)) {
			return false;
		}

		references.add(simpleName);
		return true;
	}

	private boolean isToBeRenamed(SimpleName simpleName) {
		String identifier = simpleName.getIdentifier();
		boolean matchedAssignedWithGet = assignedWithGet.stream()
			.map(SimpleName::getIdentifier)
			.anyMatch(identifier::equals);
		
		if (!matchedAssignedWithGet) {
			return false;
		}
		
		StructuralPropertyDescriptor locationInParent = simpleName.getLocationInParent();
		if(FieldAccess.NAME_PROPERTY == locationInParent) {
			return false;
		}

		return QualifiedName.NAME_PROPERTY != locationInParent;
	}

	public List<MethodInvocation> getInvocations() {
		return getInvocations;
	}

	public List<SimpleName> getReferencesToBeRenamed() {
		return references;
	}
}
