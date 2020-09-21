package eu.jsparrow.core.visitor.impl;

import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;

import eu.jsparrow.core.visitor.sub.SignatureData;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;

/**
 * This visitor looks for constructs where an invocations of
 * <ul>
 * <li>{@link String#indexOf(int)} or</li>
 * <li>{@link String#indexOf(String)} or</li>
 * <li>{@link String#lastIndexOf(int)} or</li>
 * <li>{@link String#lastIndexOf(String)} or</li>
 * <li>{@link String#startsWith(String)}</li>
 * </ul>
 * is immediately carried out upon the return value of a preceding invocation of
 * {@link String#substring(int)}. The invocation of the substring method is
 * eliminated and the subsequent indexOf-, lastIndexOf- or startsWith-
 * invocation is replaced by an invocation of the corresponding offset based
 * method.
 * <p>
 * Example using {@link String#indexOf(String)}
 * <p>
 * {@code str.substring(6).indexOf("d")} is transformed to <br>
 * {@code Math.max(str.indexOf("d", 6) - 6, -1)}
 * <p>
 * Example using {@link String#lastIndexOf(String)}
 * <p>
 * {@code str.substring(6).lastIndexOf("d")} is transformed to <br>
 * {@code Math.max(str.lastIndexOf("d", 6) - 6, -1)}
 * <p>
 * Example using {@link String#startsWith(String)}
 * <p>
 * {@code str.substring(6).startsWith("World")} is transformed to <br>
 * {@code str.startsWith("World", 6)}
 * 
 * @since 3.21.0
 *
 */
public class UseOffsetBasedStringMethodsASTVisitor extends AbstractAddImportASTVisitor {

	private static final String SUBSTRING = "substring"; //$NON-NLS-1$
	private static final String STARTS_WITH = "startsWith"; //$NON-NLS-1$
	private static final String LAST_INDEX_OF = "lastIndexOf"; //$NON-NLS-1$
	private static final String INDEX_OF = "indexOf"; //$NON-NLS-1$
	private static final String MAX = "max"; //$NON-NLS-1$
	private static final String MATH_FULLY_QUALIFIED_NAME = java.lang.Math.class.getName(); // $NON-NLS-1$
	private static final String MATH_MAX_FULLY_QUALIFIED_NAME = MATH_FULLY_QUALIFIED_NAME + "." + MAX; //$NON-NLS-1$
	private static final Class<?> STRING = java.lang.String.class;

	private final SignatureData indexOfInt = new SignatureData(STRING, INDEX_OF, int.class);
	private final SignatureData indexOfString = new SignatureData(STRING, INDEX_OF, STRING);
	private final SignatureData lastIndexOfInt = new SignatureData(STRING, LAST_INDEX_OF, int.class);
	private final SignatureData lastIndexOfString = new SignatureData(STRING, LAST_INDEX_OF, STRING);
	private final SignatureData startsWithString = new SignatureData(STRING, STARTS_WITH, STRING);
	private final SignatureData substringWithOffset = new SignatureData(STRING, SUBSTRING, int.class);

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		boolean continueVisiting = super.visit(compilationUnit);
		if (continueVisiting) {
			verifyStaticMethodImport(compilationUnit, MATH_MAX_FULLY_QUALIFIED_NAME);
			verifyImport(compilationUnit, MATH_FULLY_QUALIFIED_NAME);
		}
		return continueVisiting;
	}

	@Override
	public boolean visit(MethodInvocation node) {
		IMethodBinding methodBinding = node.resolveMethodBinding();
		if (!checkSignature(methodBinding)) {
			return true;
		}

		Expression expression = node.getExpression();
		if (expression.getNodeType() != ASTNode.METHOD_INVOCATION) {
			return true;
		}
		MethodInvocation substringInvocation = (MethodInvocation) expression;
		if (!substringWithOffset.isEquivalentTo(substringInvocation.resolveMethodBinding())) {
			return true;
		}

		List<Expression> substringArgumentList = ASTNodeUtil.convertToTypedList(substringInvocation.arguments(),
				Expression.class);

		// begin transforming...
		Expression substringArgument = substringArgumentList.get(0);
		ASTNode copyForOffsetArgument = astRewrite.createCopyTarget(substringArgument);
		astRewrite.getListRewrite(node, MethodInvocation.ARGUMENTS_PROPERTY)
			.insertLast(copyForOffsetArgument, null);

		ASTNode copyForStringExpression = astRewrite.createCopyTarget(substringInvocation.getExpression());
		astRewrite.replace(node.getExpression(), copyForStringExpression, null);

		if (!methodBinding.getName()
			.equals(STARTS_WITH)) {
			MethodInvocation maxInvocation = createMathMaxExpression(node, substringArgument);
			astRewrite.replace(node, maxInvocation, null);
		}

		onRewrite();
		return true;
	}

	private boolean checkSignature(IMethodBinding methodBinding) {
		return indexOfInt.isEquivalentTo(methodBinding) ||
				indexOfString.isEquivalentTo(methodBinding) ||
				lastIndexOfInt.isEquivalentTo(methodBinding) ||
				lastIndexOfString.isEquivalentTo(methodBinding) ||
				startsWithString.isEquivalentTo(methodBinding);
	}

	private MethodInvocation createMathMaxExpression(MethodInvocation methodInvocation, Expression substringArgument) {

		AST ast = methodInvocation.getAST();
		InfixExpression offsetSubtraction = ast.newInfixExpression();
		offsetSubtraction.setOperator(InfixExpression.Operator.MINUS);
		offsetSubtraction.setLeftOperand((MethodInvocation) astRewrite.createCopyTarget(methodInvocation));
		offsetSubtraction.setRightOperand((Expression) astRewrite.createCopyTarget(substringArgument));
		MethodInvocation maxInvocation = ast.newMethodInvocation();
		maxInvocation.setName(ast.newSimpleName(MAX));

		@SuppressWarnings("unchecked")
		List<Expression> maxArguments = maxInvocation.arguments();
		maxArguments.add(offsetSubtraction);
		maxArguments.add(ast.newNumberLiteral("-1")); //$NON-NLS-1$

		addImportForStaticMethod(MATH_MAX_FULLY_QUALIFIED_NAME);
		Name maxInvocationQualifier = findQualifierForStaticMethodInvocation(MATH_MAX_FULLY_QUALIFIED_NAME);
		if (findQualifierForStaticMethodInvocation(MATH_MAX_FULLY_QUALIFIED_NAME) != null) {
			maxInvocation.setExpression(maxInvocationQualifier);
		}
		return maxInvocation;
	}

}
