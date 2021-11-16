package eu.jsparrow.core.visitor.sub;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.rules.common.util.ClassRelationUtil;

/**
 * Used to determine whether a given {@link IMethodBinding} fulfills the
 * following conditions:
 * <ul>
 * <li>The method must be declared by a specified class.</li>
 * <li>The method must have a specified name and signature.</li>
 * </ul>
 * 
 * @since 3.20.0
 */
public class SignatureData {

	private final String declaringTypeName;
	private final String methodName;
	private final List<String> parameterTypeNames;

	public SignatureData(String declaringTypeName, String methodName, List<String> parameterTypeNames) {
		this.declaringTypeName = declaringTypeName;
		this.methodName = methodName;
		this.parameterTypeNames = Collections.unmodifiableList(parameterTypeNames);
	}

	public SignatureData(Class<?> declaringClass, String methodName, Class<?>... parameterTypes) {
		this(declaringClass.getName(), methodName, Arrays.stream(parameterTypes)
			.map(Class::getName)
			.collect(Collectors.toList()));
	}

	/**
	 * @return true if the given {@link IMethodBinding} fulfills all conditions
	 *         regarding the declaring type, name and the parameter types.
	 */
	public boolean isEquivalentTo(IMethodBinding methodBinding) {
		if (methodBinding == null) {
			return false;
		}
		if (!methodBinding.getName()
			.equals(methodName)) {
			return false;
		}

		if (!ClassRelationUtil.isContentOfType(methodBinding.getDeclaringClass(), declaringTypeName)) {
			return false;
		}

		return checkParameterTypesEquivalence(methodBinding.getParameterTypes());
	}

	/**
	 * @return true if the given {@link MethodInvocation} fulfills all
	 *         conditions regarding the declaring type, name and the parameter
	 *         types.
	 */
	public boolean isSignatureMatching(MethodInvocation methodInvocation,
			Function<IMethodBinding, ITypeBinding[]> bindingToParameterTypes) {

		if (!methodInvocation.getName()
			.getIdentifier()
			.equals(methodName)) {
			return false;
		}
		
		IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();

		if (methodBinding == null) {
			return false;
		}

		if (!ClassRelationUtil.isContentOfType(methodBinding.getDeclaringClass(), declaringTypeName)) {
			return false;
		}
		return checkParameterTypesEquivalence(bindingToParameterTypes.apply(methodBinding));
	}

	private boolean checkParameterTypesEquivalence(ITypeBinding[] parameterTypes) {
		if (parameterTypes.length != parameterTypeNames.size()) {
			return false;
		}

		Iterator<String> parameterTypesIterator = parameterTypeNames.iterator();
		return Arrays.stream(parameterTypes)
			.allMatch(t -> ClassRelationUtil.isContentOfType(t, parameterTypesIterator.next()));
	}
}
