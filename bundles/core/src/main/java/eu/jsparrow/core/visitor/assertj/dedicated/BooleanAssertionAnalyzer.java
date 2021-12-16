package eu.jsparrow.core.visitor.assertj.dedicated;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.*;

import eu.jsparrow.core.visitor.junit.dedicated.NotOperandUnwrapper;
import eu.jsparrow.rules.common.util.ASTNodeUtil;

@SuppressWarnings("nls")
class BooleanAssertionAnalyzer {

	private static final String IS_FALSE = "isFalse";
	private static final String IS_TRUE = "isTrue";
	private static final Map<String, String> ASSERTION_NAME_REPLACEMENTS;
	private static final Map<String, String> ASSERTION_NAME_NEGATED_REPLACEMENTS;

	private static final Map<InfixExpression.Operator, InfixExpression.Operator> INFIX_OPERATOR_NEGATIONS;

	static {
		Map<String, String> tmpMap = new HashMap<>();
		tmpMap.put("equals", "isEqualTo");
		tmpMap.put("equalsIgnoreCase", "isEqualToIgnoringCase");
		tmpMap.put("startsWith", "startsWith");
		tmpMap.put("contains", "contains");
		tmpMap.put("containsAll", "containsAll");
		tmpMap.put("containsKey", "containsKey");
		tmpMap.put("containsValue", "containsValue");
		tmpMap.put("endsWith", "endsWith");
		tmpMap.put("matches", "matches");
		tmpMap.put("isEmpty", "isEmpty");
		tmpMap.put("isBlank", "isBlank");
		tmpMap.put("hasNext", "hasNext"); // Iterator
		// tmpMap.put("hasPrevious", "hasPrevious"); // ListIterator invalid!!!
		// ??? tmpMap.put("hasMoreElements", "hasMoreElements"); // Enumeration
		// isPresent // Optional
		// after // Date
		// before // Date
		tmpMap.put("allMatch", "allMatch"); // Stream
		tmpMap.put("anyMatch", "anyMatch"); // Stream
		tmpMap.put("noneMatch", "noneMatch"); // Stream
		//
		tmpMap.put("exists", "exists"); // File
		tmpMap.put("isFile", "isFile"); // File
		tmpMap.put("isDirectory", "isDirectory"); // File
		// tmpMap.put("isHidden", "isHidden"); // File -- not supported
		tmpMap.put("canRead", "canRead"); // File

		ASSERTION_NAME_REPLACEMENTS = Collections.unmodifiableMap(tmpMap);

		tmpMap = new HashMap<>();
		tmpMap.put("equals", "isNotEqualTo");
		tmpMap.put("equalsIgnoreCase", "isNotEqualToIgnoringCase");
		tmpMap.put("startsWith", "doesNotStartWith");
		tmpMap.put("contains", "doesNotContain");
		tmpMap.put("containsKey", "doesNotContainKey");
		tmpMap.put("containsValue", "doesNotContainValue");
		tmpMap.put("endsWith", "doesNotEndWith");
		tmpMap.put("matches", "doesNotMatch");
		tmpMap.put("isEmpty", "isNotEmpty");
		tmpMap.put("isBlank", "isNotBlank");
		ASSERTION_NAME_NEGATED_REPLACEMENTS = Collections.unmodifiableMap(tmpMap);

		Map<InfixExpression.Operator, InfixExpression.Operator> tmpOperatorMap = new HashMap<>();

		tmpOperatorMap.put(EQUALS, NOT_EQUALS);
		tmpOperatorMap.put(NOT_EQUALS, EQUALS);
		tmpOperatorMap.put(LESS, GREATER_EQUALS);
		tmpOperatorMap.put(LESS_EQUALS, GREATER);
		tmpOperatorMap.put(GREATER, LESS_EQUALS);
		tmpOperatorMap.put(GREATER_EQUALS, LESS);
		INFIX_OPERATOR_NEGATIONS = Collections.unmodifiableMap(tmpOperatorMap);
	}

	static Optional<DedicatedAssertionData> analyzeBooleanAssertion(MethodInvocation assertThatInvocation,
			Expression assertThatArgument, MethodInvocation assumedAssertionInvocation) {

		String assertionName = assumedAssertionInvocation
			.getName()
			.getIdentifier();

		if (!assertionName.equals(IS_TRUE) && !assertionName.equals(IS_FALSE)) { // $NON-NLS-1$
			return Optional.empty();
		}

		NotOperandUnwrapper notOperandUnwrapper = new NotOperandUnwrapper(assertThatArgument);
		if (assertionName.equals(IS_FALSE) ^ notOperandUnwrapper.isNegationByNot()) {
			assertionName = IS_FALSE;
		} else {
			assertionName = IS_TRUE;
		}

		Expression unwrappedAssertThatArgument = notOperandUnwrapper.getUnwrappedOperand();

		if (unwrappedAssertThatArgument.getNodeType() == ASTNode.METHOD_INVOCATION) {
			MethodInvocation invocationAsAssertThatArgument = (MethodInvocation) unwrappedAssertThatArgument;
			return analyzeBooleanAssertion(assertThatInvocation, invocationAsAssertThatArgument,
					assertionName);

		}
		if (unwrappedAssertThatArgument.getNodeType() == ASTNode.INFIX_EXPRESSION) {
			InfixExpression infixExpression = (InfixExpression) unwrappedAssertThatArgument;

		} else {
			return Optional.empty();
		}

		return Optional.empty();
	}

	private static Optional<DedicatedAssertionData> analyzeBooleanAssertion(MethodInvocation assertThat,
			MethodInvocation invocationAsAssertThatArgument, String assertionMethodName) {

		Expression newAssertThatArgument = invocationAsAssertThatArgument.getExpression();
		if (newAssertThatArgument == null) {
			return Optional.empty();
		}		
	
		String methodName = invocationAsAssertThatArgument.getName()
			.getIdentifier();
		String newAssertionName;

		if (assertionMethodName.equals(IS_FALSE)) {
			newAssertionName = ASSERTION_NAME_NEGATED_REPLACEMENTS.get(methodName);
		} else {
			newAssertionName = ASSERTION_NAME_REPLACEMENTS.get(methodName);
		}
		if (newAssertionName == null) {
			return Optional.empty();
		}
		
		List<Expression> newAssertionArguments = ASTNodeUtil.convertToTypedList(invocationAsAssertThatArgument.arguments(),
				Expression.class);

		if (newAssertionArguments.size() > 1) {
			return Optional.empty();
		}
		
		IMethodBinding assertThatArgumentMethodBinding = invocationAsAssertThatArgument.resolveMethodBinding();
		if(assertThatArgumentMethodBinding == null) {
			return Optional.empty();
		}		
		
		if(!SupportedTypesForAssertions.isSupportedTypeForAsseertion(assertThatArgumentMethodBinding.getDeclaringClass())) {
			return Optional.empty();
		}	

		MethodInvocationData assertThatData = createNewAssertThatData(assertThat, newAssertThatArgument);

		MethodInvocationData newAssertionData = new MethodInvocationData(newAssertionName);
		newAssertionData.setArguments(newAssertionArguments);

		DedicatedAssertionData dedicatedAssertionData = new DedicatedAssertionData(assertThatData, newAssertionData);

		return Optional.of(dedicatedAssertionData);
	}

	private static MethodInvocationData createNewAssertThatData(MethodInvocation assertThat,
			Expression newAssertThatArgument) {
		String newAssertThaIdentifier = assertThat.getName()
			.getIdentifier();
		MethodInvocationData newAssertThatData = new MethodInvocationData(newAssertThaIdentifier);
		newAssertThatData.setExpression(assertThat.getExpression());
		List<Expression> newAssertThatArguments = Arrays.asList(newAssertThatArgument);
		newAssertThatData.setArguments(newAssertThatArguments);
		return newAssertThatData;
	}

	private static Optional<InfixExpression.Operator> findSupportedInfixOperator(InfixExpression infixExpression,
			String assertionName) {
		InfixExpression.Operator operator = infixExpression.getOperator();

		if (assertionName.equals(IS_FALSE)) {
			return Optional.ofNullable(INFIX_OPERATOR_NEGATIONS.get(operator));
		}

		if (INFIX_OPERATOR_NEGATIONS.keySet()
			.contains(operator)) {
			return Optional.of(operator);
		}

		return Optional.empty();
	}

}
