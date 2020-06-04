package eu.jsparrow.core.visitor.security;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;

@SuppressWarnings("nls")
public class JPAQueryVariableAnalyzerASTVisitor extends AbstractLocalVariableUsageASTVisitor {

	private static final String SET_PARAMETER = "setParameter";
	private static final List<String> EXECUTION_METHOD_NAMES = Arrays.asList("getResultList", "getSingleResult",
			"executeUpdate");

	private MethodInvocation executionInvocation;

	protected JPAQueryVariableAnalyzerASTVisitor(SimpleName sqlStatement, MethodInvocation methodInvocation) {
		super(sqlStatement, methodInvocation);
	}

	@Override
	protected boolean isOtherUnsafeVariableReference(SimpleName simpleName) {
		if (simpleName.getLocationInParent() == MethodInvocation.EXPRESSION_PROPERTY) {
			MethodInvocation methodInvocation = (MethodInvocation) simpleName.getParent();
			String methodName = methodInvocation.getName()
				.getIdentifier();
			if (SET_PARAMETER.equals(methodName)) {
				return true;
			}
			if (EXECUTION_METHOD_NAMES.stream()
				.anyMatch(methodName::equals)) {
				if (executionInvocation == null) {
					executionInvocation = methodInvocation;
				} else {
					return true;
				}
			}
		}
		return false;
	}

}
