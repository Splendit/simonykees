package eu.jsparrow.core.visitor.files.writestring;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
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

		List<WriteInvocationData> transformationDataList = analyzer
			.createTransformationDataList(tryStatement);

		if (transformationDataList.isEmpty()) {
			return true;
		}

		List<VariableDeclarationExpression> resourcesToRemove = new ArrayList<>();
		transformationDataList.stream()
			.map(WriteInvocationData::getResourcesToRemove)
			.forEach(resourcesToRemove::addAll);

		if (resourcesToRemove.size() < tryStatement.resources()
			.size()) {

			transformationDataList.stream()
				.forEach(data -> {
					ExpressionStatement invocationStatementReplacement = data
						.createWriteInvocationStatementReplacement(this);
					astRewrite.replace(data.getWriteInvocationStatementToReplace(), invocationStatementReplacement,
							null);
					onRewrite();
				});

			resourcesToRemove.stream()
				.forEach(resource -> astRewrite.remove(resource, null));
		} else {
			TryStatement newTryStatementWithoutResources = createNewTryStatementWithoutResources(tryStatement,
					transformationDataList);
			astRewrite.replace(tryStatement, newTryStatementWithoutResources, null);
			transformationDataList.stream()
				.forEach(data -> onRewrite());
		}

		return true;
	}

	@SuppressWarnings("unchecked")
	private TryStatement createNewTryStatementWithoutResources(TryStatement tryStatement,
			List<WriteInvocationData> transformationDataList) {
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

		for (WriteInvocationData data : transformationDataList) {
			ExpressionStatement writeInvocationStatementToReplace = data.getWriteInvocationStatementToReplace();
			ExpressionStatement writeInvocationStatementReplacement = data
				.createWriteInvocationStatementReplacement(this);
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

	ExpressionStatement createFilesWriteStringMethodInvocationStatement(
			WriteInvocationData writeInvocationData,
			Expression pathArgument,
			List<Expression> additionalArguments) {

		Expression originalPathArgument = pathArgument;
		Expression charSequenceArgument = writeInvocationData.getCharSequenceArgument();
		List<Expression> originalArgumentsAfterPath = additionalArguments;

		List<Expression> arguments = new ArrayList<>();
		arguments.add((Expression) astRewrite.createCopyTarget(originalPathArgument));
		arguments.add((Expression) astRewrite.createCopyTarget(charSequenceArgument));
		originalArgumentsAfterPath
			.forEach(argument -> arguments.add((Expression) astRewrite.createCopyTarget(argument)));

		Name filesTypeName = addImport(java.nio.file.Files.class.getName(),
				writeInvocationData.getWriteInvocationStatementToReplace());
		AST ast = astRewrite.getAST();
		return ast.newExpressionStatement(NodeBuilder.newMethodInvocation(ast, filesTypeName,
				ast.newSimpleName("writeString"), arguments)); //$NON-NLS-1$
	}

	ExpressionStatement createFilesWriteStringMethodInvocationStatement(
			WriteInvocationData writeInvocationData,
			List<Expression> pathExpressions) {
		AST ast = astRewrite.getAST();
		Name pathsTypeName = addImport(java.nio.file.Paths.class.getName(),
				writeInvocationData.getWriteInvocationStatementToReplace());
		List<Expression> pathsGetArguments = pathExpressions
			.stream()
			.map(pathExpression -> (Expression) astRewrite.createCopyTarget(pathExpression))
			.collect(Collectors.toList());
		Expression pathArgument = NodeBuilder.newMethodInvocation(ast, pathsTypeName,
				ast.newSimpleName("get"), pathsGetArguments); //$NON-NLS-1$

		Expression writeStringArgumentCopy = (Expression) astRewrite
			.createCopyTarget(writeInvocationData.getCharSequenceArgument());
		Optional<Expression> optionalCharset = writeInvocationData.getCharsetExpression();
		Expression charset = optionalCharset.map(exp -> (Expression) astRewrite.createCopyTarget(exp))
			.orElse(null);
		if (charset == null) {
			Name charsetTypeName = addImport(java.nio.charset.Charset.class.getName(),
					writeInvocationData.getWriteInvocationStatementToReplace());
			charset = NodeBuilder.newMethodInvocation(ast, charsetTypeName, "defaultCharset"); //$NON-NLS-1$
		}

		List<Expression> arguments = new ArrayList<>();
		arguments.add(pathArgument);
		arguments.add(writeStringArgumentCopy);
		arguments.add(charset);
		Name filesTypeName = addImport(java.nio.file.Files.class.getName(),
				writeInvocationData.getWriteInvocationStatementToReplace());
		return ast.newExpressionStatement(NodeBuilder.newMethodInvocation(ast, filesTypeName,
				ast.newSimpleName("writeString"), arguments)); //$NON-NLS-1$
	}
}
