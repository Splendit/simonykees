package eu.jsparrow.core.visitor.files;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;

import eu.jsparrow.core.visitor.impl.trycatch.TwrCommentsUtil;
import eu.jsparrow.rules.common.builder.NodeBuilder;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;
import eu.jsparrow.rules.common.visitor.helper.CommentRewriter;

/**
 * This visitor looks for {@link org.eclipse.jdt.core.dom.TryStatement}-nodes
 * which contain invocations of {@link java.io.Writer#write(String)}. <br>
 * Code transformation on a given {@link org.eclipse.jdt.core.dom.TryStatement}
 * is carried out if the given {@link org.eclipse.jdt.core.dom.TryStatement}
 * contains at least one invocation of {@link java.io.Writer#write(String)}
 * which can be replaced by one of the methods with the name "writeString" which
 * are available as static methods of the class {@link java.nio.file.Files}
 * since Java 11.
 * 
 * Example:
 * 
 * <pre>
 * try (BufferedWriter bufferedWriter = new BufferedWriter(
 * 		new FileWriter("/home/test/testpath", StandardCharsets.UTF_8))) {
 * 	bufferedWriter.write("Hello World!");
 * } catch (IOException ioException) {
 * 	logError("File could not be written.", ioException);
 * }
 * </pre>
 * 
 * is transformed to
 * 
 * <pre>
 * try {
 * 	Files.writeString(Paths.get("/home/test/testpath"), "Hello World!", StandardCharsets.UTF_8);
 * } catch (IOException ioException) {
 * 	logError("File could not be written.", ioException);
 * }
 * </pre>
 * 
 * @since 3.24.0
 *
 */
public class UseFilesWriteStringASTVisitor extends AbstractAddImportASTVisitor {

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		boolean continueVisiting = super.visit(compilationUnit);
		if (!continueVisiting) {
			return false;
		}
		verifyImport(compilationUnit, java.nio.file.Paths.class.getName());
		verifyImport(compilationUnit, java.nio.charset.Charset.class.getName());
		verifyImport(compilationUnit, java.nio.file.Files.class.getName());
		return continueVisiting;
	}

	@Override
	public boolean visit(TryStatement tryStatement) {
		UseFilesWriteStringTWRStatementAnalyzer analyzer = new UseFilesWriteStringTWRStatementAnalyzer();
		if (analyzer.collectTransformationData(tryStatement)) {

			List<WriteReplacementUsingFilesNewBufferedWriter> resultsUsingFilesNewBufferedWriter = analyzer
				.getResultsUsingFilesNewBufferedWriter();
			List<WriteReplacementUsingBufferedWriterConstructor> resultsUsingBufferedWriterConstructor = analyzer
				.getResultsUsingBufferedWriterConstructor();
			List<VariableDeclarationExpression> resourcesToRemove = analyzer.getResourcesToRemove();
			if (resourcesToRemove.size() < tryStatement.resources()
				.size()) {
				resultsUsingFilesNewBufferedWriter.stream()
					.forEach(data -> {
						astRewrite.replace(data.getWriteInvocationStatementToReplace(),
								createFilesWriteStringMethodInvocationStatement(data), null);
						onRewrite();
					});
				resultsUsingBufferedWriterConstructor.stream()
					.forEach(data -> {
						astRewrite.replace(data.getWriteInvocationStatementToReplace(),
								createFilesWriteStringMethodInvocationStatement(data), null);
						onRewrite();
					});
				resourcesToRemove.stream()
					.forEach(resource -> astRewrite.remove(resource, null));
			} else {
				TryStatement newTryStatementWithoutResources = createNewTryStatementWithoutResources(tryStatement,
						analyzer);
				astRewrite.replace(tryStatement, newTryStatementWithoutResources, null);
				resultsUsingFilesNewBufferedWriter.stream()
					.forEach(data -> onRewrite());
				resultsUsingBufferedWriterConstructor.stream()
					.forEach(data -> onRewrite());
			}
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	private TryStatement createNewTryStatementWithoutResources(TryStatement tryStatement,
			UseFilesWriteStringTWRStatementAnalyzer analyzer) {
		TryStatement tryStatementReplacement = getASTRewrite().getAST()
			.newTryStatement();

		Block oldBody = tryStatement.getBody();
		Block newBody = (Block) ASTNode.copySubtree(tryStatement.getAST(), oldBody);
		List<Statement> newBodyStatementsTypedList = newBody.statements();
		Map<Integer, List<Comment>> comments = TwrCommentsUtil.findBodyComments(tryStatement, getCommentRewriter());
		CommentRewriter commentRewriter = getCommentRewriter();
		comments.forEach((key, value) -> {
			int newBodySize = newBodyStatementsTypedList.size();
			if (newBodySize > key) {
				Statement statement = newBodyStatementsTypedList.get(key);
				commentRewriter.saveBeforeStatement(statement, value);
			} else if (!newBodyStatementsTypedList.isEmpty()) {
				Statement statement = newBodyStatementsTypedList.get(newBodySize - 1);
				commentRewriter.saveAfterStatement(statement, value);
			} else {
				commentRewriter.saveBeforeStatement(tryStatement, value);
			}
		});

		List<WriteReplacementUsingFilesNewBufferedWriter> resultsUsingFilesNewBufferedWriter = analyzer
			.getResultsUsingFilesNewBufferedWriter();
		for (WriteReplacementUsingFilesNewBufferedWriter data : resultsUsingFilesNewBufferedWriter) {
			ExpressionStatement writeInvocationStatementToReplace = data.getWriteInvocationStatementToReplace();
			ExpressionStatement writeInvocationStatementReplacement = createFilesWriteStringMethodInvocationStatement(
					data);
			int replacementIndex = oldBody.statements()
				.indexOf(writeInvocationStatementToReplace);
			newBodyStatementsTypedList.remove(replacementIndex);
			newBodyStatementsTypedList.add(replacementIndex, writeInvocationStatementReplacement);
		}

		List<WriteReplacementUsingBufferedWriterConstructor> resultsUsingBufferedWriterConstructor = analyzer
			.getResultsUsingBufferedWriterConstructor();
		for (WriteReplacementUsingBufferedWriterConstructor data : resultsUsingBufferedWriterConstructor) {
			ExpressionStatement writeInvocationStatementToReplace = data.getWriteInvocationStatementToReplace();
			ExpressionStatement writeInvocationStatementReplacement = createFilesWriteStringMethodInvocationStatement(
					data);
			int replacementIndex = oldBody.statements()
				.indexOf(writeInvocationStatementToReplace);
			newBodyStatementsTypedList.remove(replacementIndex);
			newBodyStatementsTypedList.add(replacementIndex, writeInvocationStatementReplacement);
		}

		List<CatchClause> newCatchClauses = ASTNodeUtil
			.convertToTypedList(tryStatement.catchClauses(), CatchClause.class)
			.stream()
			.map(clause -> (CatchClause) ASTNode.copySubtree(tryStatement.getAST(), clause))
			.collect(Collectors.toList());

		tryStatementReplacement.setBody(newBody);
		tryStatementReplacement.catchClauses()
			.addAll(newCatchClauses);
		tryStatementReplacement
			.setFinally((Block) ASTNode.copySubtree(tryStatement.getAST(), tryStatement.getFinally()));

		return tryStatementReplacement;
	}

	private ExpressionStatement createFilesWriteStringMethodInvocationStatement(
			WriteReplacementUsingFilesNewBufferedWriter transformationData) {
		List<Expression> arguments = new ArrayList<>();
		transformationData.getArgumentsToCopy()
			.stream()
			.forEach(arg -> arguments.add((Expression) astRewrite.createCopyTarget(arg)));

		Name filesTypeName = addImport(java.nio.file.Files.class.getName(),
				transformationData.getWriteInvocationStatementToReplace());
		AST ast = astRewrite.getAST();
		return ast.newExpressionStatement(NodeBuilder.newMethodInvocation(ast, filesTypeName,
				ast.newSimpleName("writeString"), arguments)); //$NON-NLS-1$
	}

	private ExpressionStatement createFilesWriteStringMethodInvocationStatement(
			WriteReplacementUsingBufferedWriterConstructor transformationData) {
		AST ast = astRewrite.getAST();
		Name pathsTypeName = addImport(java.nio.file.Paths.class.getName(),
				transformationData.getWriteInvocationStatementToReplace());
		List<Expression> pathsGetArguments = transformationData.getPathExpressions()
			.stream()
			.map(pathExpression -> (Expression) astRewrite.createCopyTarget(pathExpression))
			.collect(Collectors.toList());
		Expression pathArgument = NodeBuilder.newMethodInvocation(ast, pathsTypeName,
				ast.newSimpleName("get"), pathsGetArguments); //$NON-NLS-1$

		Expression writeStringArgumentCopy = (Expression) astRewrite
			.createCopyTarget(transformationData.getCharSequenceArgument());

		Expression charset = transformationData.getCharSet()
			.map(exp -> (Expression) astRewrite.createCopyTarget(exp))
			.orElse(null);
		if (charset == null) {
			Name charsetTypeName = addImport(java.nio.charset.Charset.class.getName(),
					transformationData.getWriteInvocationStatementToReplace());
			charset = NodeBuilder.newMethodInvocation(ast, charsetTypeName, "defaultCharset"); //$NON-NLS-1$
		}

		List<Expression> arguments = new ArrayList<>();
		arguments.add(pathArgument);
		arguments.add(writeStringArgumentCopy);
		arguments.add(charset);
		Name filesTypeName = addImport(java.nio.file.Files.class.getName(),
				transformationData.getWriteInvocationStatementToReplace());
		return ast.newExpressionStatement(NodeBuilder.newMethodInvocation(ast, filesTypeName,
				ast.newSimpleName("writeString"), arguments)); //$NON-NLS-1$
	}
}
