package eu.jsparrow.core.visitor.impl.entryset;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;

class TransformationData {
	private final SupportedLoopStructure forStatementData;
	private final String mapVariableIdentifier;
	private final Type typeArgumentForKey;
	private final Type typeArgumentForValue;
	private final MethodInvocation mapGetterInvocationToReplace;
	private final String mapEntryIdentifier;

	TransformationData(SupportedLoopStructure forStatementData, Type typeArgumentForKey, Type typeArgumentForValue,
			String mapEntryIdentifier) {
		this.forStatementData = forStatementData;
		this.mapVariableIdentifier = forStatementData.getAssumedMapVariableName()
			.getIdentifier();
		this.typeArgumentForKey = typeArgumentForKey;
		this.typeArgumentForValue = typeArgumentForValue;
		this.mapGetterInvocationToReplace = forStatementData.getAssumedMapGetterInvocation();
		this.mapEntryIdentifier = mapEntryIdentifier;
	}

	String getMapVariableIdentifier() {
		return mapVariableIdentifier;
	}

	Type getTypeArgumentForKey() {
		return typeArgumentForKey;
	}

	Type getTypeArgumentForValue() {
		return typeArgumentForValue;
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
}
