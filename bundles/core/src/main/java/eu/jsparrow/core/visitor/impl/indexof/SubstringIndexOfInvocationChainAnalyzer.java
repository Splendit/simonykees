package eu.jsparrow.core.visitor.impl.indexof;

import java.util.Collections;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.core.visitor.security.common.SignatureData;
import eu.jsparrow.rules.common.util.ASTNodeUtil;

class SubstringIndexOfInvocationChainAnalyzer {

	private static class StringMethodSignatureData extends SignatureData {

		public StringMethodSignatureData(String methodName, Class<?> parameterClass) {
			super(java.lang.String.class.getName(), methodName, Collections.singletonList(parameterClass.getName()));
		}
	}

	private static final String SUBSTRING = "substring"; //$NON-NLS-1$
	private static final String STARTS_WITH = "startsWith"; //$NON-NLS-1$
	private static final String LAST_INDEX_OF = "lastIndexOf"; //$NON-NLS-1$
	private static final String INDEX_OF = "indexOf"; //$NON-NLS-1$
	private static final StringMethodSignatureData INDEX_OF_INT = new StringMethodSignatureData(INDEX_OF, int.class);
	private static final StringMethodSignatureData INDEX_OF_STRING = new StringMethodSignatureData(INDEX_OF,
			java.lang.String.class);
	private static final StringMethodSignatureData LAST_INDEX_OF_INT = new StringMethodSignatureData(LAST_INDEX_OF,
			int.class);
	private static final StringMethodSignatureData LAST_INDEX_OF_STRING = new StringMethodSignatureData(LAST_INDEX_OF,
			java.lang.String.class);
	private static final StringMethodSignatureData STARTS_WITH_STRING = new StringMethodSignatureData(STARTS_WITH,
			java.lang.String.class);
	private static final StringMethodSignatureData SUBSTRING_AT_BEGIN_INDEX = new StringMethodSignatureData(SUBSTRING,
			int.class);

	private Expression stringExpression;
	private String offsetBasedMethodName;
	private Expression offsetArgument;

	private boolean checkSignature(IMethodBinding methodBinding) {
		return INDEX_OF_INT.isEquivalentTo(methodBinding) ||
				INDEX_OF_STRING.isEquivalentTo(methodBinding) ||
				LAST_INDEX_OF_INT.isEquivalentTo(methodBinding) ||
				LAST_INDEX_OF_STRING.isEquivalentTo(methodBinding) ||
				STARTS_WITH_STRING.isEquivalentTo(methodBinding);
	}

	boolean analyze(MethodInvocation methodInvocation) {
		IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
		if (!checkSignature(methodBinding)) {
			return false;
		}
		offsetBasedMethodName = methodBinding.getName();

		Expression expression = methodInvocation.getExpression();
		if (expression.getNodeType() != ASTNode.METHOD_INVOCATION) {
			return false;
		}
		MethodInvocation substringInvocation = (MethodInvocation) expression;
		if (SUBSTRING_AT_BEGIN_INDEX.isEquivalentTo(substringInvocation.resolveMethodBinding())) {
			return false;
		}
		stringExpression = substringInvocation.getExpression();
		offsetArgument = ASTNodeUtil.convertToTypedList(substringInvocation.arguments(), Expression.class)
			.get(0);

		return true;
	}

	public Expression getStringExpression() {
		return stringExpression;
	}

	public String getOffsetBasedMethodName() {
		return offsetBasedMethodName;
	}

	public Expression getOffsetArgument() {
		return offsetArgument;
	}

}
