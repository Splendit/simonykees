package eu.jsparrow.core.visitor.impl.entryset;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;

public class TransformationData {
	private final SupportedLoopStructure forStatementData;
	private final Expression mapExpression;
	private final Type valueType;
	private final int extraValueDimensions;
	private final MethodInvocation mapGetterInvocationToReplace;
	private final String mapEntryIdentifier;

	TransformationData(SupportedLoopStructure forStatementData, String mapEntryIdentifier) {
		this.forStatementData = forStatementData;
		this.mapExpression = forStatementData.getAssumedMapExpression();

		ValueDeclarationStructure valueDeclarationData = forStatementData.getValueDeclarationData();
		this.valueType = valueDeclarationData.getDeclarationStatement()
			.getType();
		this.extraValueDimensions = valueDeclarationData.getDeclarationFragment()
			.getExtraDimensions();
		this.mapGetterInvocationToReplace = valueDeclarationData.getValueByKeyGetterInvocation();
		this.mapEntryIdentifier = mapEntryIdentifier;
	}

	Expression getMapExpression() {
		return mapExpression;
	}

	Type getValueType() {
		return valueType;
	}

	int getExtraValueDimensions() {
		return extraValueDimensions;
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
