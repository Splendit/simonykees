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
		verifyImport(compilationUnit, FilesUtil.PATHS_QUALIFIED_NAME);
		verifyImport(compilationUnit, FilesUtil.CHARSET_QUALIFIED_NAME);
		verifyImport(compilationUnit, FilesUtil.FILES_QUALIFIED_NAME);
		return continueVisiting;
	}

	@Override
	public boolean visit(TryStatement tryStatement) {
		WriteInvocationInTWRBodyVisitor visitor = new WriteInvocationInTWRBodyVisitor(tryStatement);
		visitor.analyze();

		if (visitor.hasTransformationData()) {
			transform(tryStatement, visitor);
		}
		return true;
	}

	private void transform(TryStatement tryStatement, WriteInvocationInTWRBodyVisitor visitor) {

		List<TransformationDataUsingFilesNewBufferedWriter> filesNewBufferedWriterInvocationDataList = visitor
			.getFilesNewBufferedWriterInvocationDataList();
		List<TransformationDataUsingBufferedWriterConstructor> bufferedWriterInstanceCreationDataList = visitor
			.getBufferedWriterInstanceCreationDataList();
		List<VariableDeclarationExpression> resourcesToRemove = visitor.getResourcesToRemove();
		if (resourcesToRemove.size() < tryStatement.resources()
			.size()) {
			filesNewBufferedWriterInvocationDataList.stream()
				.forEach(data -> {
					astRewrite.replace(data.getWriteInvocationStatementToReplace(),
							createFilesWriteStringMethodInvocationStatement(data), null);
					onRewrite();
				});
			bufferedWriterInstanceCreationDataList.stream()
				.forEach(data -> {
					astRewrite.replace(data.getWriteInvocationStatementToReplace(),
							createFilesWriteStringMethodInvocationStatement(data), null);
					onRewrite();
				});
			resourcesToRemove.stream()
				.forEach(resource -> astRewrite.remove(resource, null));
		} else {
			astRewrite.replace(tryStatement, createNewTryStatementWithoutResources(tryStatement,
					filesNewBufferedWriterInvocationDataList, bufferedWriterInstanceCreationDataList), null);
			filesNewBufferedWriterInvocationDataList.stream()
				.forEach(data -> onRewrite());
			bufferedWriterInstanceCreationDataList.stream()
				.forEach(data -> onRewrite());
		}
	}

	@SuppressWarnings("unchecked")
	private TryStatement createNewTryStatementWithoutResources(TryStatement tryStatement,
			List<TransformationDataUsingFilesNewBufferedWriter> filesNewBufferedWriterInvocationDataList,
			List<TransformationDataUsingBufferedWriterConstructor> bufferedWriterInstanceCreationDataList) {

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

		for (TransformationDataUsingFilesNewBufferedWriter data : filesNewBufferedWriterInvocationDataList) {
			ExpressionStatement writeInvocationStatementToReplace = data.getWriteInvocationStatementToReplace();
			ExpressionStatement writeInvocationStatementReplacement = createFilesWriteStringMethodInvocationStatement(
					data);
			int replacementIndex = oldBody.statements()
				.indexOf(writeInvocationStatementToReplace);
			newBodyStatementsTypedList.remove(replacementIndex);
			newBodyStatementsTypedList.add(replacementIndex, writeInvocationStatementReplacement);
		}

		for (TransformationDataUsingBufferedWriterConstructor data : bufferedWriterInstanceCreationDataList) {
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
			TransformationDataUsingFilesNewBufferedWriter transformationData) {
		List<Expression> arguments = new ArrayList<>();
		transformationData.getArgumentsToCopy()
			.stream()
			.forEach(arg -> arguments.add((Expression) astRewrite.createCopyTarget(arg)));

		Name filesTypeName = addImport(FilesUtil.FILES_QUALIFIED_NAME,
				transformationData.getWriteInvocationStatementToReplace());
		AST ast = astRewrite.getAST();
		return ast.newExpressionStatement(NodeBuilder.newMethodInvocation(ast, filesTypeName,
				ast.newSimpleName("writeString"), arguments)); //$NON-NLS-1$
	}

	private ExpressionStatement createFilesWriteStringMethodInvocationStatement(
			TransformationDataUsingBufferedWriterConstructor transformationData) {
		AST ast = astRewrite.getAST();
		Name pathsTypeName = addImport(FilesUtil.PATHS_QUALIFIED_NAME,
				transformationData.getWriteInvocationStatementToReplace());
		List<Expression> pathsGetArguments = transformationData.getPathExpressions()
			.stream()
			.map(pathExpression -> (Expression) astRewrite.createCopyTarget(pathExpression))
			.collect(Collectors.toList());
		Expression pathArgument = NodeBuilder.newMethodInvocation(ast, pathsTypeName,
				ast.newSimpleName(FilesUtil.GET), pathsGetArguments);

		Expression writeStringArgumentCopy = (Expression) astRewrite
			.createCopyTarget(transformationData.getCharSequenceArgument());

		Expression charset = transformationData.getCharSet()
			.map(exp -> (Expression) astRewrite.createCopyTarget(exp))
			.orElse(null);
		if (charset == null) {
			Name charsetTypeName = addImport(FilesUtil.CHARSET_QUALIFIED_NAME,
					transformationData.getWriteInvocationStatementToReplace());
			charset = NodeBuilder.newMethodInvocation(ast, charsetTypeName, FilesUtil.DEFAULT_CHARSET);
		}

		List<Expression> arguments = new ArrayList<>();
		arguments.add(pathArgument);
		arguments.add(writeStringArgumentCopy);
		arguments.add(charset);
		Name filesTypeName = addImport(FilesUtil.FILES_QUALIFIED_NAME,
				transformationData.getWriteInvocationStatementToReplace());
		return ast.newExpressionStatement(NodeBuilder.newMethodInvocation(ast, filesTypeName,
				ast.newSimpleName("writeString"), arguments)); //$NON-NLS-1$
	}
}
