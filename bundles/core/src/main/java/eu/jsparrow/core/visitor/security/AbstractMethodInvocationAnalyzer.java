package eu.jsparrow.core.visitor.security;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

import eu.jsparrow.rules.common.util.ClassRelationUtil;

/**
 * Used to determine whether a given {@link IMethodBinding} fulfills the
 * following conditions:
 * <ul>
 * <li>The method must be declared by a specified class.</li>
 * <li>The method must have a specified name and signature.</li>
 * </ul>
 * 
 * @since 3.19.0
 */
public class AbstractMethodInvocationAnalyzer {

	private final IMethodBinding methodBinding;

	public AbstractMethodInvocationAnalyzer(IMethodBinding methodBinding) {
		this.methodBinding = methodBinding;
	}

	/**
	 * 
	 * @param declaringTypeName
	 *            the qualified name of the type by which the method is expected
	 *            to be declared
	 * @param methodName
	 *            expected method name
	 * @param parameterTypeNames
	 *            list of the qualified names of the expected parameter types.
	 * @return true if all conditions given by the parameters are fulfilled,
	 *         otherwise false.
	 */
	public boolean analyze(String declaringTypeName, String methodName, List<String> parameterTypeNames) {
		if (!methodBinding.getName()
			.equals(methodName)) {
			return false;
		}

		if (!ClassRelationUtil.isContentOfType(methodBinding.getDeclaringClass(), declaringTypeName)) {
			return false;
		}

		ITypeBinding[] parameterTypes = methodBinding.getParameterTypes();

		if (parameterTypes.length != parameterTypeNames.size()) {
			return false;
		}

		Iterator<String> parameterTypesIterator = parameterTypeNames.iterator();
		return Arrays.stream(parameterTypes)
			.allMatch(t -> ClassRelationUtil.isContentOfType(t, parameterTypesIterator.next()));
	}
}
