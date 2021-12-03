package eu.jsparrow.core.visitor.assertj;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.Type;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

public class ShiftAssertJDescriptionsBeforeAssertionsASTVisitor extends AbstractASTRewriteASTVisitor {

	@SuppressWarnings("nls")
	private List<String> descriptionSettingMethods = Arrays.asList("as", "describedAs", "withFailMessage",
			"overridingErrorMessage");

	private static final String ORG_ASSERTJ_CORE_API_DESCRIPTABLE = "org.assertj.core.api.Descriptable"; //$NON-NLS-1$
	private static final String ORG_ASSERTJ_CORE_API_ABSTRACT_ASSERT = "org.assertj.core.api.AbstractAssert"; //$NON-NLS-1$
	@SuppressWarnings("nls")
	private List<String> assertJAssertionMethodsPrefix = Arrays.asList("is", "contains", "has", "starts", "ends",
			"matches"); // FIXME take them from SIM-2027

	@Override
	public boolean visit(MethodInvocation methodInvocation) {
		SimpleName methodName = methodInvocation.getName();
		String methodIdentifier = methodName.getIdentifier();
		if (!descriptionSettingMethods.contains(methodIdentifier)) {
			return true;
		}
		IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
		if (methodBinding == null) {
			return true;
		}

		ITypeBinding declaringClass = methodBinding.getDeclaringClass();
		if (declaringClass == null) {
			return true;
		}

		boolean isAssertJClass = ClassRelationUtil.isContentOfTypes(declaringClass,
				Arrays.asList(ORG_ASSERTJ_CORE_API_ABSTRACT_ASSERT, ORG_ASSERTJ_CORE_API_DESCRIPTABLE));
		if (!isAssertJClass) {
			return true;
		}
		StructuralPropertyDescriptor locInParent = methodInvocation.getLocationInParent();
		if (locInParent != ExpressionStatement.EXPRESSION_PROPERTY) {
			return true;
		}

		Expression expression = methodInvocation.getExpression();
		if (expression == null || !isAssertJAssertionInvocation(expression)) {
			return true;
		}

		MethodInvocation assertJAssertion = (MethodInvocation) expression;

		MethodInvocation swappedMethodInvcation = swap(assertJAssertion, methodInvocation);
		astRewrite.replace(methodInvocation, swappedMethodInvcation, null);
		onRewrite();
		return true;
	}

	@SuppressWarnings("unchecked")
	private MethodInvocation swap(MethodInvocation assertJAssertion, MethodInvocation methodInvocation) {
		AST ast = methodInvocation.getAST();
		MethodInvocation descSetting = ast.newMethodInvocation();
		descSetting.setExpression((Expression) astRewrite.createCopyTarget(assertJAssertion.getExpression()));
		descSetting.setName(ast.newSimpleName(methodInvocation.getName()
			.getIdentifier()));
		List<Expression> descArgs = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(), Expression.class);
		for (Expression descArg : descArgs) {
			Expression argCopy = (Expression) astRewrite.createCopyTarget(descArg);
			descSetting.arguments()
				.add(argCopy);
		}
		List<Type> descTypeArgs = ASTNodeUtil.convertToTypedList(methodInvocation.typeArguments(), Type.class);
		for (Type decTypeArg : descTypeArgs) {
			Type typeCopy = (Type) astRewrite.createCopyTarget(decTypeArg);
			descSetting.typeArguments()
				.add(typeCopy);
		}

		MethodInvocation swapped = ast.newMethodInvocation();
		swapped.setExpression(descSetting);
		swapped.setName(ast.newSimpleName(assertJAssertion.getName()
			.getIdentifier()));
		List<Expression> arguments = ASTNodeUtil.convertToTypedList(assertJAssertion.arguments(), Expression.class);
		for (Expression argument : arguments) {
			Expression argCopy = (Expression) astRewrite.createCopyTarget(argument);
			swapped.arguments()
				.add(argCopy);
		}
		List<Type> types = ASTNodeUtil.convertToTypedList(assertJAssertion.typeArguments(), Type.class);
		for (Type type : types) {
			Type typeCopy = (Type) astRewrite.createCopyTarget(type);
			swapped.typeArguments()
				.add(typeCopy);
		}
		return swapped;
	}

	private boolean isAssertJAssertionInvocation(Expression expression) {
		if (expression.getNodeType() != ASTNode.METHOD_INVOCATION) {
			return false;
		}
		MethodInvocation methodInvocation = (MethodInvocation) expression;
		SimpleName methodName = methodInvocation.getName();
		String identifier = methodName.getIdentifier();
		return assertJAssertionMethodsPrefix
			.stream()
			.anyMatch(identifier::startsWith);
	}

}
