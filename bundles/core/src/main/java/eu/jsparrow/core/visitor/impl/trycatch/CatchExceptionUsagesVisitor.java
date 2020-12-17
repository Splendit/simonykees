package eu.jsparrow.core.visitor.impl.trycatch;

import java.util.Arrays;
import java.util.List;

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

/**
 * Visitor for finding if the exceptions are used as parameters in generic
 * methods.
 * 
 * @since 3.0.0
 *
 */
public class CatchExceptionUsagesVisitor extends ASTVisitor {

	private String exceptionIdentifer;
	private boolean exceptionUsedInTypeInference = false;

	public CatchExceptionUsagesVisitor(SimpleName exceptionName) {
		this.exceptionIdentifer = exceptionName.getIdentifier();
	}

	@Override
	public boolean preVisit2(ASTNode node) {
		return !exceptionUsedInTypeInference;
	}

	@Override
	public boolean visit(SimpleName simpleName) {
		String identifier = simpleName.getIdentifier();
		if (!identifier.equals(exceptionIdentifer)) {
			return false;
		}

		IBinding binding = simpleName.resolveBinding();
		if (binding == null || binding.getKind() != IBinding.VARIABLE) {
			return false;
		}

		IVariableBinding variableBinding = (IVariableBinding) binding;
		if (variableBinding.isField()) {
			return false;
		}

		if (simpleName.getLocationInParent() != MethodInvocation.ARGUMENTS_PROPERTY) {
			return false;
		}

		MethodInvocation methodInvocation = (MethodInvocation) simpleName.getParent();
		boolean involvedInTypeInference = isUsedToDeriveTypeVariable(methodInvocation, simpleName);
		if (involvedInTypeInference) {
			exceptionUsedInTypeInference = true;
		}

		return false;
	}

	private boolean isUsedToDeriveTypeVariable(MethodInvocation methodInvocation, SimpleName simpleName) {
		List<Expression> arguments = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(), Expression.class);
		int position = arguments.indexOf(simpleName);
		if (position < 0) {
			return false;
		}
		IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
		if (methodBinding == null) {
			return false;
		}
		IMethodBinding methodDeclaration = methodBinding.getMethodDeclaration();
		if (methodDeclaration == null) {
			return false;
		}
		ITypeBinding[] parameterTypes = methodDeclaration.getParameterTypes();
		if (parameterTypes.length <= position) {
			return false;
		}
		ITypeBinding declaredArgumentType = parameterTypes[position];
		if (!declaredArgumentType.isTypeVariable()) {
			return false;
		}
		String typeVariableName = declaredArgumentType.getName();
		ITypeBinding[] thrownTypes = methodDeclaration.getExceptionTypes();
		return Arrays.stream(thrownTypes)
			.map(ITypeBinding::getName)
			.anyMatch(typeVariableName::equals);
	}

	/**
	 * 
	 * @return {@code true} if the exception is used as a parameter in a generic
	 *         method and the type thrown by the method is determined by the
	 *         type of the exception.
	 */
	public boolean isExceptionUsedInTypeInference() {
		return exceptionUsedInTypeInference;
	}
}
