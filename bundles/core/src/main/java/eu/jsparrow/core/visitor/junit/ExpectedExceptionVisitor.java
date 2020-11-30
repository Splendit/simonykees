package eu.jsparrow.core.visitor.junit;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;

public class ExpectedExceptionVisitor extends ASTVisitor {

	private static final String EXPECTED_EXCEPTION = "org.junit.rules.ExpectedException"; //$NON-NLS-1$
	private List<MethodInvocation> expectedExceptionInvocations = new ArrayList<>();

	public boolean visit(MethodInvocation methodInvocation) {
		IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
		ITypeBinding declaringClass = methodBinding.getDeclaringClass();
		boolean isExpectedException = ClassRelationUtil.isContentOfType(declaringClass,
				EXPECTED_EXCEPTION);
		if (!isExpectedException) {
			return true;
		}

		Expression expression = methodInvocation.getExpression();
		if (expression == null) {
			return true;
		}

		if (!isFieldAccess(expression)) {
			return true;
		}
		expectedExceptionInvocations.add(methodInvocation);
		return true;
	}

	private boolean isFieldAccess(Expression expression) {
		if (expression.getNodeType() == ASTNode.SIMPLE_NAME) {
			SimpleName name = (SimpleName) expression;
			IBinding typeBinding = name.resolveBinding();
			int kind = typeBinding.getKind();
			if (IBinding.VARIABLE == kind) {
				IVariableBinding variableBinding = (IVariableBinding) typeBinding;
				return variableBinding.isField();
			}
			return false;

		} else {
			return ASTNode.FIELD_ACCESS == expression.getNodeType();
		}

	}

	public List<Expression> getExpectedExceptionsTypes() {
		List<MethodInvocation> expectedInvocations = getExpectExceptionInvocations();
		List<Expression> expectedExceptions = new ArrayList<>();
		for (MethodInvocation expected : expectedInvocations) {
			List<Expression> arguments = ASTNodeUtil.convertToTypedList(expected.arguments(), Expression.class);
			for (Expression argument : arguments) {
				expectedExceptions.add(argument);
			}
		}
		return expectedExceptions;

	}

	public List<MethodInvocation> getExpectExceptionInvocations() {
		return this.expectedExceptionInvocations.stream()
			.filter(mi -> "expect".equals(mi.getName().getIdentifier()))
			.collect(Collectors.toList());
	}

	public List<Expression> getExpectedMessages(Predicate<MethodInvocation> argTypeFilter) {
		return this.expectedExceptionInvocations.stream()
			.filter(mi -> "expectMessage".equals(mi.getName()
				.getIdentifier()))
			.filter(argTypeFilter)
			.flatMap(mi -> ASTNodeUtil.convertToTypedList(mi.arguments(), Expression.class)
				.stream())
			.collect(Collectors.toList());
	}

	public boolean hasStringParameter(MethodInvocation methodInvocation) {
		return false;
	}

	public boolean hasMatcherParameter(MethodInvocation methodInvocation) {
		return false;
	}

	public List<Expression> getExpectedCauses() {
		return this.expectedExceptionInvocations.stream()
			.filter(mi -> "expectCause".equals(mi.getName()
				.getIdentifier()))
			.filter(this::hasMatcherParameter)
			.flatMap(mi -> ASTNodeUtil.convertToTypedList(mi.arguments(), Expression.class)
				.stream())
			.collect(Collectors.toList());
	}

}
