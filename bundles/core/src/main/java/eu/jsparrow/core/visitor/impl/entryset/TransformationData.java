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

	TransformationData(SupportedLoopStructure forStatementData) {
		this.forStatementData = forStatementData;
		this.mapExpression = forStatementData.getAssumedMapExpression();

		ValueDeclarationStructure valueDeclarationData = forStatementData.getValueDeclarationData();
		this.valueType = valueDeclarationData.getDeclarationStatement()
			.getType();

		// this.valueType = forStatementData.getSupportedBodyStructure()
		// .getMappingValueType();

		this.extraValueDimensions = valueDeclarationData.getDeclarationFragment()
			.getExtraDimensions();

		// this.extraValueDimensions =
		// forStatementData.getSupportedBodyStructure()
		// .getExtraValueDimensions();

		this.mapGetterInvocationToReplace = valueDeclarationData.getValueByKeyGetterInvocation();

		// this.mapGetterInvocationToReplace =
		// forStatementData.getSupportedBodyStructure()
		// .getMapGetterInvocation();
	}

	public Expression getMapExpression() {
		return mapExpression;
	}

	public Type getValueType() {
		return valueType;
	}

	public int getExtraValueDimensions() {
		return extraValueDimensions;
	}

	public SingleVariableDeclaration getLoopParameter() {
		return forStatementData.getParameter();
	}

	public Expression getLoopExpression() {
		return forStatementData.getExpression();
	}

	public Block getLoopBody() {
		return forStatementData.getBody();
	}

	public MethodInvocation getMapGetterInvocationToReplace() {
		return mapGetterInvocationToReplace;
	}
}
