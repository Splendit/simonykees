package eu.jsparrow.core.visitor.impl;

import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

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
	private static final String MINUS_ONE_LITERAL = "-1"; //$NON-NLS-1$
	private static final SignatureData INDEX_OF_INT = new SignatureData(java.lang.String.class, INDEX_OF, int.class);
	private static final SignatureData INDEX_OF_STRING = new SignatureData(java.lang.String.class, INDEX_OF,
			java.lang.String.class);
	private static final SignatureData LAST_INDEX_OF_INT = new SignatureData(java.lang.String.class, LAST_INDEX_OF,
			int.class);
	private static final SignatureData LAST_INDEX_OF_STRING = new SignatureData(java.lang.String.class, LAST_INDEX_OF,
			java.lang.String.class);
	private static final SignatureData STARTS_WITH_STRING = new SignatureData(java.lang.String.class, STARTS_WITH,
			java.lang.String.class);
	private static final SignatureData SUBSTRING_WITH_BEGIN_INDEX = new SignatureData(java.lang.String.class, SUBSTRING,
			int.class);
	private boolean flagSafeImportStaticMathMax;
	private boolean flagSafeImportStaticMathMaxExistsOnDemand;
	private boolean flagSafeImportMath;

	private boolean checkSignature(IMethodBinding methodBinding) {
		return INDEX_OF_INT.isEquivalentTo(methodBinding) ||
				INDEX_OF_STRING.isEquivalentTo(methodBinding) ||
				LAST_INDEX_OF_INT.isEquivalentTo(methodBinding) ||
				LAST_INDEX_OF_STRING.isEquivalentTo(methodBinding) ||
				STARTS_WITH_STRING.isEquivalentTo(methodBinding);
	}

	@Override
	public boolean visit(CompilationUnit node) {
		super.visit(node);
		flagSafeImportStaticMathMax = isSafeToAddStaticMethodImport(node, MATH_MAX_FULLY_QUALIFIED_NAME);
		if (flagSafeImportStaticMathMax) {
			List<ImportDeclaration> importDeclarations = ASTNodeUtil.convertToTypedList(node.imports(),
					ImportDeclaration.class);
			flagSafeImportStaticMathMaxExistsOnDemand = containsUnambiguousStaticMethodImportOnDemand(
					importDeclarations, MATH_MAX_FULLY_QUALIFIED_NAME);
		}
		flagSafeImportMath = isSafeToAddImport(node, MATH_FULLY_QUALIFIED_NAME);
		return true;
	}

	@Override
	public void endVisit(CompilationUnit node) {
		super.endVisit(node);
		flagSafeImportStaticMathMax = false;
		flagSafeImportMath = false;
		flagSafeImportStaticMathMaxExistsOnDemand = false;
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
		if (!SUBSTRING_WITH_BEGIN_INDEX.isEquivalentTo(substringInvocation.resolveMethodBinding())) {
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

	private MethodInvocation createMathMaxExpression(MethodInvocation methodInvocation, Expression substringArgument) {

		AST ast = methodInvocation.getAST();
		InfixExpression offsetSubtraction = ast.newInfixExpression();
		offsetSubtraction.setOperator(InfixExpression.Operator.MINUS);
		offsetSubtraction.setLeftOperand((MethodInvocation) astRewrite.createCopyTarget(methodInvocation));
		offsetSubtraction.setRightOperand((Expression) astRewrite.createCopyTarget(substringArgument));
		MethodInvocation maxInvocation = ast.newMethodInvocation();
		maxInvocation.setName(ast.newSimpleName(MAX));
		ListRewrite maxArgumentsListRewrite = astRewrite.getListRewrite(maxInvocation,
				MethodInvocation.ARGUMENTS_PROPERTY);
		maxArgumentsListRewrite.insertFirst(offsetSubtraction, null);
		maxArgumentsListRewrite.insertLast(ast.newNumberLiteral(MINUS_ONE_LITERAL), null);
		Name maxInvocationQualifier = findMaxInvocationQualifier(ast);
		if (maxInvocationQualifier != null) {
			maxInvocation.setExpression(maxInvocationQualifier);
		}
		return maxInvocation;
	}

	private Name findMaxInvocationQualifier(AST ast) {
		if (flagSafeImportStaticMathMax) {
			if (!flagSafeImportStaticMathMaxExistsOnDemand) {
				addStaticImport(MATH_MAX_FULLY_QUALIFIED_NAME);
			}
			return null;
		}
		if (flagSafeImportMath) {
			return ast.newSimpleName(java.lang.Math.class.getSimpleName());
		}

		return ast.newName(MATH_FULLY_QUALIFIED_NAME);
	}
}
