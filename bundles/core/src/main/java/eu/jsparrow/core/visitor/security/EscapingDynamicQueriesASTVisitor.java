package eu.jsparrow.core.visitor.security;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import eu.jsparrow.rules.common.builder.NodeBuilder;
import eu.jsparrow.rules.common.util.ASTNodeUtil;

/**
 * 
 * @since 3.17.0
 *
 */
public class EscapingDynamicQueriesASTVisitor extends DynamicQueryASTVisitor {

	private static final List<String> IMPORTS_FOR_ESCAPE = Collections.unmodifiableList(Arrays.asList(
			"org.owasp.esapi.codecs.Codec", //$NON-NLS-1$
			"org.owasp.esapi.codecs.OracleCodec", //$NON-NLS-1$
			"org.owasp.esapi.ESAPI" //$NON-NLS-1$
	));

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		for (String qualifiedName : IMPORTS_FOR_ESCAPE) {
			if (!isSafeToAddImport(compilationUnit, qualifiedName)) {
				return false;
			}
		}
		return super.visit(compilationUnit);
	}

	@Override
	public boolean visit(MethodInvocation methodInvocation) {
		SqlVariableAnalyzerVisitor sqlVariableVisitor = createSqlVariableAnalyzerVisitor(methodInvocation);
		if (sqlVariableVisitor == null) {
			return true;
		}

		List<Expression> expressionsToEscape = analyzeQueryComponents(sqlVariableVisitor);
		if (expressionsToEscape.isEmpty()) {
			return true;
		}
		Expression expression0 = sqlVariableVisitor.getDynamicQueryComponents()
			.get(0);
		Statement statement = ASTNodeUtil.getSpecificAncestor(expression0, Statement.class);
		if (statement.getLocationInParent() != Block.STATEMENTS_PROPERTY) {
			return true;
		}

		Block enclosingBlock = (Block) statement.getParent();
		ListRewrite listRewrite = astRewrite.getListRewrite(enclosingBlock, Block.STATEMENTS_PROPERTY);
		listRewrite.insertBefore(createOracleCODECDeclarationStatement(), statement, null);

		for (Expression expressionToEscape : expressionsToEscape) {
			astRewrite.replace(expressionToEscape, createEscapeExpression(expressionToEscape), null);
		}
		onRewrite();
		return true;
	}

	@SuppressWarnings("nls")
	private Expression createEscapeExpression(Expression expressionToEscape) {

		AST ast = astRewrite.getAST();
		MethodInvocation encoderInvocationOfESAPI = NodeBuilder.newMethodInvocation(ast, ast.newSimpleName("ESAPI"),
				"encoder");
		SimpleName encodeForSQLName = ast.newSimpleName("encodeForSQL");
		List<Expression> arguments = new ArrayList<>();
		arguments.add(ast.newSimpleName("ORACLE_CODEC"));
		arguments.add((Expression) astRewrite.createCopyTarget(expressionToEscape));
		return NodeBuilder.newMethodInvocation(ast, encoderInvocationOfESAPI, encodeForSQLName, arguments);
	}

	@SuppressWarnings("nls")
	private VariableDeclarationStatement createOracleCODECDeclarationStatement() {
		for (String qualifiedName : IMPORTS_FOR_ESCAPE) {
			addImports.add(qualifiedName);
		}
		AST ast = astRewrite.getAST();
		VariableDeclarationFragment fragment = ast.newVariableDeclarationFragment();
		fragment.setName(ast.newSimpleName("ORACLE_CODEC"));
		ClassInstanceCreation oracleCODECinitializer = ast.newClassInstanceCreation();
		oracleCODECinitializer.setType(ast.newSimpleType(ast.newSimpleName("OracleCodec")));
		fragment.setInitializer(oracleCODECinitializer);
		VariableDeclarationStatement oracleCODECDeclarationStatement = ast.newVariableDeclarationStatement(fragment);
		oracleCODECDeclarationStatement.setType(ast.newSimpleType(ast.newSimpleName("Codec")));
		return oracleCODECDeclarationStatement;
	}


	private List<Expression> analyzeQueryComponents(SqlVariableAnalyzerVisitor sqlVariableVisitor) {
		List<Expression> queryComponents = sqlVariableVisitor.getDynamicQueryComponents();
		QueryComponentsAnalyzerForEscaping componentsAnalyzer = new QueryComponentsAnalyzerForEscaping(queryComponents);
		componentsAnalyzer.analyze();
		return componentsAnalyzer.getExpressionsToEscape();
	}

}
