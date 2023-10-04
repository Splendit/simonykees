package eu.jsparrow.core.visitor.impl.entryset;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

class TransformationData {
	private final SupportedLoopStructure forStatementData;
	private final ParameterizedType parameterizedMapType;
	private final String mapVariableIdentifier;
	private final MethodInvocation mapGetterInvocationToReplace;
	private final String mapEntryIdentifier;
	private final KeyVariableDeclarationData keyDeclarationData;

	TransformationData(SupportedLoopStructure forStatementData, ParameterizedType parameterizedMapType,
			String mapEntryIdentifier, KeyVariableDeclarationData keyDeclarationData) {
		this.forStatementData = forStatementData;
		this.mapVariableIdentifier = forStatementData.getAssumedMapVariableName()
			.getIdentifier();
		this.parameterizedMapType = parameterizedMapType;
		this.mapGetterInvocationToReplace = forStatementData.getAssumedMapGetterInvocation();
		this.mapEntryIdentifier = mapEntryIdentifier;
		this.keyDeclarationData = keyDeclarationData;
	}

	String getMapVariableIdentifier() {
		return mapVariableIdentifier;
	}

	ParameterizedType getParameterizedMapType() {
		return parameterizedMapType;
	}

	SingleVariableDeclaration getLoopParameter() {
		return forStatementData.getParameter();
	}

	Expression getLoopExpression() {
		return forStatementData.getExpression();
	}

	Block getLoopBody() {
		return forStatementData.getBody();
	}

	MethodInvocation getMapGetterInvocationToReplace() {
		return mapGetterInvocationToReplace;
	}

	String getMapEntryIdentifier() {
		return mapEntryIdentifier;
	}

	KeyVariableDeclarationData getKeyDeclarationData() {
		return keyDeclarationData;
	}
}
