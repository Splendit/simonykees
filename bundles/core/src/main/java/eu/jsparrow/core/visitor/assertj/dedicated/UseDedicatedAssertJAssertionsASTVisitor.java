package eu.jsparrow.core.visitor.assertj.dedicated;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.TypeLiteral;

import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * 
 * This visitor looks for AssertJ assertions which can be replaced by more
 * specific, dedicated AssertJ assertions.
 * <p>
 * For example, the AssertJ assertion
 * 
 * <pre>
 * assertThat(string.equals("Hello World!")).isTrue();
 * </pre>
 * 
 * can be replaced by a single one like
 * 
 * <pre>
 * assertThat(string).isEqualTo("Hello World!");
 * </pre>
 * 
 * @since 4.7.0
 *
 */
public class UseDedicatedAssertJAssertionsASTVisitor extends AbstractASTRewriteASTVisitor {

	@Override
	public boolean visit(MethodInvocation node) {

		final AssertJAssertThatWithAssertionData initialData = AssertJAssertThatWithAssertionData
			.findDataForAssumedAssertion(node)
			.orElse(null);

		if (initialData == null) {
			return true;
		}
		AssertJAssertThatWithAssertionData dataExpectedToChange = initialData;

		AllBooleanAssertionsAnalyzer allBooleanAssertinsAnalyzer = AllBooleanAssertionsAnalyzer
			.conditionalInstance(initialData)
			.orElse(null);

		if (allBooleanAssertinsAnalyzer != null) {
			BooleanAssertionWithInstanceofAnalysisResult resultForAssertionWithInstanceof = allBooleanAssertinsAnalyzer
				.findResultForInstanceofAsAssertThatArgument()
				.orElse(null);
			if (resultForAssertionWithInstanceof != null) {
				transform(node, resultForAssertionWithInstanceof);
				return true;
			}
			dataExpectedToChange = allBooleanAssertinsAnalyzer.findResultForOtherAssertThatArgument()
				.orElse(null);
			if (dataExpectedToChange == null) {
				return true;
			}
		}

		dataExpectedToChange = AssertionWithLiteralArgumentAnalyzer
			.findDataForAssertionWithLiteral(dataExpectedToChange)
			.orElse(dataExpectedToChange);

		if (dataExpectedToChange != initialData) {
			transform(node, dataExpectedToChange);
		}

		return true;
	}

	private void transform(MethodInvocation node, BooleanAssertionWithInstanceofAnalysisResult data) {
		SimpleType instanceofRightOperand = data.getInstanceofRightOperand();
		MethodInvocation newAssertion = createIsInstanceofInvocation(instanceofRightOperand);
		AssertJAssertThatData newAssertThatData = data.getNewAssertThatData();
		MethodInvocation newAssertThatInvocation = createNewAssertThatInvocation(newAssertThatData);
		newAssertion.setExpression(newAssertThatInvocation);
		astRewrite.replace(node, newAssertion, null);
		onRewrite();
	}

	@SuppressWarnings("unchecked")
	private void transform(MethodInvocation node, AssertJAssertThatWithAssertionData data) {
		AST ast = astRewrite.getAST();
		MethodInvocation newAssertion = ast.newMethodInvocation();
		String newAssertionName = data.getAssertionName();
		newAssertion.setName(ast.newSimpleName(newAssertionName));
		data.getAssertionArgument()
			.map(assertionArgument -> (Expression) astRewrite.createCopyTarget(assertionArgument))
			.ifPresent(assertionArgument -> newAssertion.arguments()
				.add(assertionArgument));
		AssertJAssertThatData newAssertThatData = data.getAssertThatData();
		MethodInvocation newAssertThatInvocation = createNewAssertThatInvocation(newAssertThatData);
		newAssertion.setExpression(newAssertThatInvocation);
		astRewrite.replace(node, newAssertion, null);
		onRewrite();
	}

	@SuppressWarnings("unchecked")
	private MethodInvocation createNewAssertThatInvocation(AssertJAssertThatData newAssertThatData) {
		AST ast = astRewrite.getAST();
		MethodInvocation newAssertThatInvocation = ast.newMethodInvocation();
		newAssertThatInvocation.setName(ast.newSimpleName(newAssertThatData.getAssertThatMethodName()));
		Expression newAssertThatArgument = (Expression) astRewrite
			.createCopyTarget(newAssertThatData.getAssertThatArgument());
		newAssertThatInvocation.arguments()
			.add(newAssertThatArgument);
		newAssertThatData.getAssertThatInvocationExpression()
			.map(expression -> (Expression) astRewrite.createCopyTarget(expression))
			.ifPresent(newAssertThatInvocation::setExpression);
		return newAssertThatInvocation;
	}

	@SuppressWarnings("unchecked")
	private MethodInvocation createIsInstanceofInvocation(SimpleType instanceofRightOperand) {
		AST ast = astRewrite.getAST();
		MethodInvocation newAssertion = ast.newMethodInvocation();
		newAssertion.setName(ast.newSimpleName("isInstanceOf")); //$NON-NLS-1$
		SimpleType simpleTypeCopy = (SimpleType) astRewrite.createCopyTarget(instanceofRightOperand);
		TypeLiteral newAssertionArgument = ast.newTypeLiteral();
		newAssertionArgument.setType(simpleTypeCopy);
		newAssertion.arguments()
			.add(newAssertionArgument);
		return newAssertion;
	}
}
