package eu.jsparrow.core.visitor.security;

import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;

public abstract class AbstractMethodInvocationAnalyzer {

	private final MethodInvocation methodInvocation;
	private final SimpleName methodSimpleName;
	private final IMethodBinding methodBinding;
	private final ITypeBinding declaringClass;
	private final List<Expression> arguments;

	public AbstractMethodInvocationAnalyzer(MethodInvocation methodInvocation) {
		this.methodInvocation = methodInvocation;
		methodSimpleName = methodInvocation.getName();
		methodBinding = methodInvocation.resolveMethodBinding();
		declaringClass = methodBinding.getDeclaringClass();
		arguments = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(), Expression.class);

	}

	public boolean analyze(String declaringTypeName, String methodName, List<String> parameterTypes) {
		//TODO: as soon as we find an unsatisfied condition we can return
		boolean valid = methodSimpleName.getIdentifier()
			.equals(methodName);

		valid &= ClassRelationUtil.isContentOfType(declaringClass, declaringTypeName);
		valid &= arguments.size() == parameterTypes.size();

		if (!valid) {
			return false;
		}

		Iterator<String> parameterTypesIterator = parameterTypes.iterator();
		return arguments.stream()
			.map(Expression::resolveTypeBinding)
			.allMatch(t -> ClassRelationUtil.isContentOfType(t, parameterTypesIterator.next()));

	}

	public MethodInvocation getMethodInvocation() {
		return methodInvocation;
	}

	public SimpleName getMethodSimpleName() {
		return methodSimpleName;
	}

	public IMethodBinding getMethodBinding() {
		return methodBinding;
	}

	public ITypeBinding getDeclaringClass() {
		return declaringClass;
	}

	public List<Expression> getArguments() {
		return arguments;
	}

}
