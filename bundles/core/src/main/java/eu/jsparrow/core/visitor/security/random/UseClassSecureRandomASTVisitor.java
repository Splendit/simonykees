package eu.jsparrow.core.visitor.security.random;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;

public class UseClassSecureRandomASTVisitor extends AbstractAddImportASTVisitor {

	private static final String SECURE_RANDOM_QUALIFIED_NAME = java.security.SecureRandom.class.getName();
	private final Map<CompilationUnit, Boolean> isSafeToAddImportMap = new HashMap<>();

	@Override
	public boolean visit(CompilationUnit node) {
		super.visit(node);
		boolean flagSafeImport = isSafeToAddImport(node, SECURE_RANDOM_QUALIFIED_NAME);
		isSafeToAddImportMap.put(node, Boolean.valueOf(flagSafeImport));
		return true;
	}

	@Override
	public boolean visit(ClassInstanceCreation node) {

		ITypeBinding typeBinding = node.getType()
			.resolveBinding();
		if (!typeBinding.getQualifiedName()
			.equals(java.util.Random.class.getName())) {
			return true;
		}

		Expression nonParenthesized = node;
		while (nonParenthesized.getLocationInParent() == ParenthesizedExpression.EXPRESSION_PROPERTY) {
			nonParenthesized = (Expression) nonParenthesized.getParent();
		}
		if (nonParenthesized.getLocationInParent() == ClassInstanceCreation.ARGUMENTS_PROPERTY
				|| nonParenthesized.getLocationInParent() == MethodInvocation.ARGUMENTS_PROPERTY) {
			return true;
		}

		if (node.arguments()
			.isEmpty()) {
			if (nonParenthesized != node) {
				astRewrite.replace(nonParenthesized, node, null);
			}
			astRewrite.replace(node.getType(), getSecureRandomType(), null);
			onRewrite();
			return true;
		}

		Statement statementBeforeSetSeed = null;
		Expression assignmentTarget = null;

		if (nonParenthesized.getLocationInParent() == VariableDeclarationFragment.INITIALIZER_PROPERTY) {
			VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) nonParenthesized
				.getParent();
			if (variableDeclarationFragment.getLocationInParent() == VariableDeclarationStatement.FRAGMENTS_PROPERTY) {
				statementBeforeSetSeed = (VariableDeclarationStatement) variableDeclarationFragment.getParent();
				assignmentTarget = variableDeclarationFragment.getName();
			}

		} else if (nonParenthesized.getLocationInParent() == Assignment.RIGHT_HAND_SIDE_PROPERTY) {
			Assignment assignment = (Assignment) nonParenthesized.getParent();
			if (assignment.getLocationInParent() == ExpressionStatement.EXPRESSION_PROPERTY) {
				statementBeforeSetSeed = (ExpressionStatement) assignment.getParent();
				assignmentTarget = assignment.getLeftHandSide();
			}
		}
		if (statementBeforeSetSeed == null || assignmentTarget == null) {
			return true;
		}

		if (statementBeforeSetSeed.getLocationInParent() != Block.STATEMENTS_PROPERTY) {
			return true;
		}

		Expression setSeedExpression = (Expression) astRewrite.createCopyTarget(assignmentTarget);

		Block block = (Block) statementBeforeSetSeed.getParent();

		Expression seedArgument = ASTNodeUtil.convertToTypedList(node.arguments(), Expression.class)
			.get(0);
		ASTNode removedSetSeed = astRewrite.createMoveTarget(seedArgument);
		astRewrite.remove(seedArgument, null);

		AST ast = block.getAST();
		MethodInvocation setSeedInvocation = ast.newMethodInvocation();
		setSeedInvocation.setExpression(setSeedExpression);
		setSeedInvocation.setName(ast.newSimpleName("setSeed")); //$NON-NLS-1$
		ListRewrite argumentListRewrite = astRewrite.getListRewrite(setSeedInvocation,
				MethodInvocation.ARGUMENTS_PROPERTY);
		argumentListRewrite.insertFirst(removedSetSeed, null);
		ExpressionStatement setSeedInvocationStatement = ast.newExpressionStatement(setSeedInvocation);

		ListRewrite statementListRewrite = astRewrite.getListRewrite(block, Block.STATEMENTS_PROPERTY);
		statementListRewrite.insertAfter(setSeedInvocationStatement, statementBeforeSetSeed, null);
		if (nonParenthesized != node) {
			astRewrite.replace(nonParenthesized, node, null);
		}
		astRewrite.replace(node.getType(), getSecureRandomType(), null);
		onRewrite();
		return true;
	}

	@Override
	public void endVisit(CompilationUnit node) {
		super.endVisit(node);
		isSafeToAddImportMap.clear();
	}

	private Type getSecureRandomType() {
		CompilationUnit compilationUnit = getCompilationUnit();
		AST ast = compilationUnit.getAST();
		Name secureRandomName;
		boolean flagSafeImport = isSafeToAddImportMap.get(compilationUnit)
			.booleanValue();
		if (flagSafeImport) {
			this.addImports.add(SECURE_RANDOM_QUALIFIED_NAME);
			secureRandomName = ast
				.newSimpleName(java.security.SecureRandom.class.getSimpleName());
		} else {
			secureRandomName = ast.newName(SECURE_RANDOM_QUALIFIED_NAME);
		}
		return ast.newSimpleType(secureRandomName);
	}

}
