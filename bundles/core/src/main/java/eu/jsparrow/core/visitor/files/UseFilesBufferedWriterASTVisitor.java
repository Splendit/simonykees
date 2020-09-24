package eu.jsparrow.core.visitor.files;

import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

/**
 * Replaces the initializations of {@link java.io.BufferedWriter} objects with
 * the non-blocking alternative
 * {@link java.nio.file.Files#newBufferedWriter(java.nio.file.Path, java.nio.charset.Charset)}.
 * 
 * For example, the following code:
 * <p>
 * {@code BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File("path/to/file")));}
 * <p>
 * is transformed to:
 * <p>
 * {@code BufferedWriter bufferedWriter = Files.newBufferedWriter(Paths.get("path/to/file"), Charset.defaultCharset());}
 * <p>
 * 
 * @since 3.22.0
 *
 */
public class UseFilesBufferedWriterASTVisitor extends AbstractUseFilesMethodsASTVisitor {

	private static final String BUFFERED_WRITER_QUALIFIED_NAME = java.io.BufferedWriter.class.getName();
	private static final String NEW_BUFFERED_WRITER = "newBufferedWriter"; //$NON-NLS-1$

	@Override
	public boolean visit(VariableDeclarationFragment fragment) {

		ClassInstanceCreation newBufferedWriter = findClassInstanceCreationAsInitializer(fragment,
				BUFFERED_WRITER_QUALIFIED_NAME);
		if (newBufferedWriter == null) {
			return true;
		}
		Expression bufferedWriterArg = findFirstArgumentOfType(newBufferedWriter, java.io.FileWriter.class.getName());
		if (bufferedWriterArg == null) {
			return true;
		}

		NewBufferedIOArgumentsAnalyzer analyzer = new NewBufferedIOArgumentsAnalyzer();
		if (bufferedWriterArg.getNodeType() == ASTNode.CLASS_INSTANCE_CREATION
				&& analyzer.analyzeInitializer((ClassInstanceCreation) bufferedWriterArg)) {

			AST ast = fragment.getAST();
			List<Expression> pathExpressions = analyzer.getPathExpressions();
			Expression charset = analyzer.getCharset()
				.map(exp -> (Expression) astRewrite.createCopyTarget(exp))
				.orElse(createDefaultCharsetExpression(ast));

			MethodInvocation filesNewBufferedReader = createFilesNewBufferedIOMethodInvocation(ast, pathExpressions,
					charset, NEW_BUFFERED_WRITER);

			astRewrite.replace(newBufferedWriter, filesNewBufferedReader, null);
			onRewrite();
		}
		return super.visit(fragment);
	}

}
