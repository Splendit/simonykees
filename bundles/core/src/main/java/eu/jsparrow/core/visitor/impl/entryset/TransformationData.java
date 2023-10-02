package eu.jsparrow.core.visitor.impl.entryset;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;

public class TransformationData {
	private final SupportedLoopStructure forStatementData;
	private final Type keyType;
	private final Type valueType;
	private final Expression mapExpression;
	private final int extraValueDimensions;
	private final MethodInvocation mapGetterInvocationToReplace;
	private final String mapEntryIdentifier;

	TransformationData(SupportedLoopStructure forStatementData, Type keyType, Type valueType,
			String mapEntryIdentifier) {
		this.forStatementData = forStatementData;
		this.keyType = keyType;
		this.valueType = valueType;
		this.mapExpression = forStatementData.getAssumedMapExpression();
		ValueDeclarationStructure valueDeclarationData = forStatementData.getValueDeclarationData();

		this.extraValueDimensions = valueDeclarationData.getDeclarationFragment()
			.getExtraDimensions();
		this.mapGetterInvocationToReplace = valueDeclarationData.getValueByKeyGetterInvocation();
		this.mapEntryIdentifier = mapEntryIdentifier;
	}

	Expression getMapExpression() {
		return mapExpression;
	}

	public Type getKeyType() {
		return keyType;
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
