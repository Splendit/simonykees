package eu.jsparrow.core.visitor.security.random;

import java.util.Collections;
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
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import eu.jsparrow.core.visitor.security.AbstractMethodInvocationAnalyzer;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;

/**
 * Used to replace invocations of constructors of {@link java.util.Random}.
 * <p>
 * Invocations of {@link java.util.Random#Random() } are simply replaced by
 * invocations of {@link java.security.SecureRandom#SecureRandom() }, and when
 * replacing {@link java.util.Random#Random(long)}, additionally a subsequent
 * invocation of {@link java.util.Random#setSeed(long)} is inserted.
 * <p>
 * Example:
 * 
 * <pre>
 * Random random = new Random(0L);
 * </pre>
 * 
 * is transformed to:
 * 
 * <pre>
 * Random random = new SecureRandom();
 * random.setSeed(0L);
 * </pre>
 * 
 * @since 3.20.0
 */
public class UseClassSecureRandomASTVisitor extends AbstractAddImportASTVisitor {

	/**
	 * This class is intended to be used exclusively inside
	 * {@link UseClassSecureRandomASTVisitor} for storing information needed for
	 * transformation, without any logics.
	 *
	 */
	private static class TransformationData {
		private final ClassInstanceCreation randomConstruction;
		private final Expression nonParenthesized;
		private final Expression seedArgument;
		private final Expression assignmentTarget;
		private final Statement randomConstructionStatement;
		private final Block blockOfConstructionStatement;

		public TransformationData(ClassInstanceCreation classInstanceCreation, Expression nonParenthesized,
				Expression seedArgument,
				Expression assignmentTarget, Statement randomConstructionStatement,
				Block blockOfConstructionStatement) {
			super();
			this.randomConstruction = classInstanceCreation;
			this.nonParenthesized = nonParenthesized;
			this.seedArgument = seedArgument;

			this.assignmentTarget = assignmentTarget;
			this.randomConstructionStatement = randomConstructionStatement;
			this.blockOfConstructionStatement = blockOfConstructionStatement;
		}
	}

	private static final String SECURE_RANDOM_QUALIFIED_NAME = java.security.SecureRandom.class.getName();
	private final Map<CompilationUnit, Boolean> isSafeToAddImportMap = new HashMap<>();

	@Override
	public boolean visit(CompilationUnit node) {
		super.visit(node);
		boolean flagSafeImport = isSafeToAddImport(node, SECURE_RANDOM_QUALIFIED_NAME);
		isSafeToAddImportMap.put(node, Boolean.valueOf(flagSafeImport));
		return true;
	}

	private TransformationData createTransformationData(ClassInstanceCreation node) {

		Expression nonParenthesized = node;
		while (nonParenthesized.getLocationInParent() == ParenthesizedExpression.EXPRESSION_PROPERTY) {
			nonParenthesized = (Expression) nonParenthesized.getParent();
		}
		if (nonParenthesized.getLocationInParent() == ClassInstanceCreation.ARGUMENTS_PROPERTY
				|| nonParenthesized.getLocationInParent() == MethodInvocation.ARGUMENTS_PROPERTY) {
			return null;
		}

		if (node.arguments()
			.isEmpty()) {
			return new TransformationData(node, nonParenthesized, null, null, null, null);
		}
		
		Statement randomConstructionStatement = null;
		Expression assignmentTarget = null;

		if (nonParenthesized.getLocationInParent() == VariableDeclarationFragment.INITIALIZER_PROPERTY) {
			VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) nonParenthesized
				.getParent();
			if (variableDeclarationFragment.getLocationInParent() == VariableDeclarationStatement.FRAGMENTS_PROPERTY) {
				randomConstructionStatement = (VariableDeclarationStatement) variableDeclarationFragment.getParent();
				assignmentTarget = variableDeclarationFragment.getName();
			}

		} else if (nonParenthesized.getLocationInParent() == Assignment.RIGHT_HAND_SIDE_PROPERTY) {
			Assignment assignment = (Assignment) nonParenthesized.getParent();
			if (assignment.getLocationInParent() == ExpressionStatement.EXPRESSION_PROPERTY) {
				randomConstructionStatement = (ExpressionStatement) assignment.getParent();
				assignmentTarget = assignment.getLeftHandSide();
			}
		}

		if (randomConstructionStatement == null || assignmentTarget == null) {
			return null;
		}

		if (randomConstructionStatement.getLocationInParent() != Block.STATEMENTS_PROPERTY) {
			return null;
		}

		Block blockOfConstructionStatement = (Block) randomConstructionStatement.getParent();
		Expression seedArgument = ASTNodeUtil.convertToTypedList(node.arguments(), Expression.class)
			.get(0);

		return new TransformationData(node, nonParenthesized, seedArgument, assignmentTarget,
				randomConstructionStatement, blockOfConstructionStatement);
	}

	@SuppressWarnings("nls")
	@Override
	public boolean visit(ClassInstanceCreation node) {
		AbstractMethodInvocationAnalyzer invocationAnalyzer = new AbstractMethodInvocationAnalyzer(node.resolveConstructorBinding());
		if(!invocationAnalyzer.analyze(java.util.Random.class.getName(), "Random", Collections.emptyList()) &&
				!invocationAnalyzer.analyze(java.util.Random.class.getName(),  "Random", Collections.singletonList("long"))) {
			return true;
		}
		TransformationData transformationData = createTransformationData(node);
		if (transformationData != null) {
			transform(transformationData);
		}
		return true;
	}

	void transform(TransformationData data) {
		if (data.seedArgument == null) {
			if (data.nonParenthesized != data.randomConstruction) {
				astRewrite.replace(data.nonParenthesized, data.randomConstruction, null);
			}
			astRewrite.replace(data.randomConstruction.getType(), getSecureRandomType(), null);
			onRewrite();
			return;
		}

		Expression insertedSetSeedExpression = (Expression) astRewrite.createCopyTarget(data.assignmentTarget);
		ASTNode insertedSetSeedArgument = astRewrite.createMoveTarget(data.seedArgument);
		astRewrite.remove(data.seedArgument, null);

		AST ast = data.blockOfConstructionStatement.getAST();
		MethodInvocation setSeedInvocation = ast.newMethodInvocation();
		setSeedInvocation.setExpression(insertedSetSeedExpression);
		setSeedInvocation.setName(ast.newSimpleName("setSeed")); //$NON-NLS-1$
		ListRewrite argumentListRewrite = astRewrite.getListRewrite(setSeedInvocation,
				MethodInvocation.ARGUMENTS_PROPERTY);
		argumentListRewrite.insertFirst(insertedSetSeedArgument, null);
		ExpressionStatement setSeedInvocationStatement = ast.newExpressionStatement(setSeedInvocation);

		ListRewrite statementListRewrite = astRewrite.getListRewrite(data.blockOfConstructionStatement,
				Block.STATEMENTS_PROPERTY);
		statementListRewrite.insertAfter(setSeedInvocationStatement, data.randomConstructionStatement, null);
		if (data.nonParenthesized != data.randomConstruction) {
			astRewrite.replace(data.nonParenthesized, data.randomConstruction, null);
		}
		astRewrite.replace(data.randomConstruction.getType(), getSecureRandomType(), null);
		onRewrite();
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
