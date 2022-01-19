package eu.jsparrow.core.visitor.assertj.dedicated;

import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;

public class BooleanAssertionWithInstanceofAnalyzer {

	static Optional<BooleanAssertionWithInstanceofAnalysisResult> findAssertThatInstanceOfAnalysisData(
			AssertJAssertThatWithAssertionData assertThatWithAsssertion, InstanceofExpression instanceofExpression) {

		String assertionName = assertThatWithAsssertion.getAssertionName();

		if (assertionName.equals(UseDedicatedAssertJAssertionsASTVisitor.IS_FALSE)) {
			return Optional.empty();
		}
		Expression leftOperand = instanceofExpression.getLeftOperand();
		Type rightOperand = instanceofExpression.getRightOperand();
		if (rightOperand.getNodeType() != ASTNode.SIMPLE_TYPE) {
			return Optional.empty();
		}
		SimpleType simpleType = (SimpleType) rightOperand;

		ITypeBinding leftOperandType = leftOperand.resolveTypeBinding();
		if (leftOperandType == null) {
			return Optional.empty();
		}
		if (!SupportedAssertJAssertThatArgumentTypes.isSupportedAssertThatArgumentType(leftOperandType)) {
			return Optional.empty();
		}
		AssertJAssertThatData assertThatData = assertThatWithAsssertion.getAssertThatData();
		AssertJAssertThatData newAssartThatData = AssertJAssertThatData.createDataReplacingArgument(assertThatData,
				leftOperand);
		return Optional.of(new BooleanAssertionWithInstanceofAnalysisResult(newAssartThatData, simpleType));
	}

	private BooleanAssertionWithInstanceofAnalyzer() {
		/*
		 * private default constructor hiding implicit public one
		 */
	}
}
