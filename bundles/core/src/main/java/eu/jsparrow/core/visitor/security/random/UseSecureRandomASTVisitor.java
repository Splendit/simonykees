package eu.jsparrow.core.visitor.security.random;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

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
public class UseSecureRandomASTVisitor extends AbstractAddImportASTVisitor {

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
	public void endVisit(CompilationUnit node) {
		super.endVisit(node);
		isSafeToAddImportMap.clear();
	}

	@Override
	public boolean visit(ClassInstanceCreation node) {
		NewRandomAnalyzer analyzer = new NewRandomAnalyzer();
		if (analyzer.analyze(node)) {
			transform(analyzer);
		}
		return true;
	}

	private void transform(NewRandomAnalyzer analyzer) {

		Expression seedArgument = analyzer.getSeedArgument();
		if (seedArgument != null) {
			Expression insertedSetSeedExpression = (Expression) astRewrite
				.createCopyTarget(analyzer.getAssignmentTarget());
			ASTNode insertedSetSeedArgument = astRewrite.createMoveTarget(seedArgument);
			astRewrite.remove(seedArgument, null);

			Block blockOfConstructionStatement = analyzer.getBlockOfConstructionStatement();
			Statement randomConstructionStatement = analyzer.getRandomConstructionStatement();

			AST ast = blockOfConstructionStatement.getAST();
			MethodInvocation setSeedInvocation = ast.newMethodInvocation();
			setSeedInvocation.setExpression(insertedSetSeedExpression);
			setSeedInvocation.setName(ast.newSimpleName("setSeed")); //$NON-NLS-1$
			ListRewrite argumentListRewrite = astRewrite.getListRewrite(setSeedInvocation,
					MethodInvocation.ARGUMENTS_PROPERTY);
			argumentListRewrite.insertFirst(insertedSetSeedArgument, null);
			ExpressionStatement setSeedInvocationStatement = ast.newExpressionStatement(setSeedInvocation);

			ListRewrite statementListRewrite = astRewrite.getListRewrite(blockOfConstructionStatement,
					Block.STATEMENTS_PROPERTY);
			statementListRewrite.insertAfter(setSeedInvocationStatement, randomConstructionStatement, null);
		}
		astRewrite.replace(analyzer.getRandomExpressionToReplace(), getSecureRandomInstanceCreation(), null);
		onRewrite();
	}

	private ClassInstanceCreation getSecureRandomInstanceCreation() {		
		
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
		SimpleType secureRandomType = ast.newSimpleType(secureRandomName);
		ClassInstanceCreation secureRandomInstanceCreation = ast.newClassInstanceCreation();
		secureRandomInstanceCreation.setType(secureRandomType);
		return secureRandomInstanceCreation;
	}
}
