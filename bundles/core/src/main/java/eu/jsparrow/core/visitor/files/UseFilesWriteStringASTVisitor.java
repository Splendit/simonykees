package eu.jsparrow.core.visitor.files;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import eu.jsparrow.core.visitor.impl.trycatch.TwrCommentsUtil;
import eu.jsparrow.rules.common.builder.NodeBuilder;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;
import eu.jsparrow.rules.common.visitor.helper.CommentRewriter;
import eu.jsparrow.rules.common.visitor.helper.LocalVariableUsagesASTVisitor;

/**
 * This visitor looks for invocations of {@link java.io.Writer#write(String)}
 * which can be replaced by invocations of methods with the name "writeString"
 * which are available as static methods of the class
 * {@link java.nio.file.Files} since Java 11.
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
	public boolean visit(MethodInvocation methodInvocation) {

		WriteMethodInvocationAnalyzer writeInvocationAnalyzer = new WriteMethodInvocationAnalyzer();
		if (!writeInvocationAnalyzer.analyze(methodInvocation)) {
			return true;
		}

		if (writeInvocationAnalyzer.getBlockOfInvocationStatement()
			.getLocationInParent() != TryStatement.BODY_PROPERTY) {
			return true;
		}
		TryStatement tryStatement = (TryStatement) writeInvocationAnalyzer.getBlockOfInvocationStatement()
			.getParent();

		TryResourceAnalyzer bufferedWriterResourceAnalyzer = new TryResourceAnalyzer();
		if (!bufferedWriterResourceAnalyzer.analyze(tryStatement,
				writeInvocationAnalyzer.getWriterVariableSimpleName())) {
			return true;
		}

		WriteInvocationsInTryStatementBodyVisitor writeInvocationsVisitor = new WriteInvocationsInTryStatementBodyVisitor();
		tryStatement.getBody()
			.accept(writeInvocationsVisitor);
		if (writeInvocationsVisitor.getWriteMethodInvocations()
			.size() != 1) {
			return true;
		}

		if (!checkWriterVariableUsage(writeInvocationAnalyzer.getWriterVariableSimpleName(),
				writeInvocationAnalyzer.getBlockOfInvocationStatement())) {
			return true;
		}
		FilesNewBufferedIOTransformationData filesNewBufferedIOTransformationData = findFilesNewBufferedIOTransformationData(
				bufferedWriterResourceAnalyzer, writeInvocationAnalyzer).orElse(null);
		if (filesNewBufferedIOTransformationData != null) {
			transform(methodInvocation, filesNewBufferedIOTransformationData);
		} else {
			findResultByBufferedWriterInstanceCreation(bufferedWriterResourceAnalyzer, writeInvocationAnalyzer)
				.ifPresent(result -> transform(methodInvocation, result));
		}
		return true;
	}

	private boolean checkFilesNewBufferedWriterParameterTypes(IMethodBinding methodBinding) {
		ITypeBinding[] parameterTypes = methodBinding.getParameterTypes();
		if (parameterTypes.length == 2) {
			if (!ClassRelationUtil.isContentOfType(parameterTypes[0], java.nio.file.Path.class.getName())) {
				return false;
			}
			return ClassRelationUtil.isContentOfType(parameterTypes[1].getElementType(),
					java.nio.file.OpenOption.class.getName());
		} else if (parameterTypes.length == 3) {
			if (!ClassRelationUtil.isContentOfType(parameterTypes[0], java.nio.file.Path.class.getName())) {
				return false;
			}
			if (!ClassRelationUtil.isContentOfType(parameterTypes[1], java.nio.charset.Charset.class.getName())) {
				return false;
			}
			return ClassRelationUtil.isContentOfType(parameterTypes[2].getElementType(),
					java.nio.file.OpenOption.class.getName());
		}
		return false;
	}

	private Optional<FilesNewBufferedIOTransformationData> findFilesNewBufferedIOTransformationData(
			TryResourceAnalyzer bufferedWriterResourceAnalyzer, WriteMethodInvocationAnalyzer writeInvocationAnalyzer) {

		Expression bufferedIOInitializer = bufferedWriterResourceAnalyzer.getResourceInitializer();
		if (bufferedIOInitializer.getNodeType() != ASTNode.METHOD_INVOCATION) {
			return Optional.empty();
		}
		MethodInvocation methodInvocation = (MethodInvocation) bufferedIOInitializer;
		IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();

		if (!ClassRelationUtil.isContentOfType(methodBinding
			.getDeclaringClass(), java.nio.file.Files.class.getName())) {
			return Optional.empty();
		}
		if (!Modifier.isStatic(methodBinding.getModifiers())) {
			return Optional.empty();
		}
		if (!methodBinding.getName()
			.equals("newBufferedWriter")) { //$NON-NLS-1$
			return Optional.empty();
		}
		if (!checkFilesNewBufferedWriterParameterTypes(methodBinding)) {
			return Optional.empty();
		}

		List<Expression> argumentsToCopy = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(),
				Expression.class);

		argumentsToCopy.add(1, writeInvocationAnalyzer.getCharSequenceArgument());
		TryStatement tryStatement = bufferedWriterResourceAnalyzer.getTryStatement();
		VariableDeclarationExpression resourceToRemove = bufferedWriterResourceAnalyzer.getResource();
		return Optional.of(
				new FilesNewBufferedIOTransformationData(tryStatement, resourceToRemove,
						writeInvocationAnalyzer.getWriteInvocationStatementToReplace(), argumentsToCopy));

	}

	Optional<UseFilesWriteStringAnalysisResult> findResultByBufferedWriterInstanceCreation(
			TryResourceAnalyzer bufferedWriterResourceAnalyzer, WriteMethodInvocationAnalyzer writeInvocationAnalyzer) {

		Expression bufferedIOInitializer = bufferedWriterResourceAnalyzer.getResourceInitializer();
		if (!ClassRelationUtil.isNewInstanceCreationOf(bufferedIOInitializer, java.io.BufferedWriter.class.getName())) {
			return Optional.empty();
		}
		ClassInstanceCreation bufferedWriterInstanceCreation = (ClassInstanceCreation) bufferedIOInitializer;

		Expression bufferedWriterInstanceCreationArgument = FilesUtil
			.findBufferedIOArgument(bufferedWriterInstanceCreation, java.io.FileWriter.class.getName())
			.orElse(null);
		if (bufferedWriterInstanceCreationArgument == null) {
			return Optional.empty();
		}
		TryStatement tryStatement = bufferedWriterResourceAnalyzer.getTryStatement();
		VariableDeclarationExpression resourceDeclaringBufferedWriter = bufferedWriterResourceAnalyzer.getResource();
		if (bufferedWriterInstanceCreationArgument.getNodeType() == ASTNode.CLASS_INSTANCE_CREATION) {
			NewBufferedIOArgumentsAnalyzer newBufferedIOArgumentsAnalyzer = new NewBufferedIOArgumentsAnalyzer();
			ClassInstanceCreation writerInstanceCreation = (ClassInstanceCreation) bufferedWriterInstanceCreationArgument;
			if (newBufferedIOArgumentsAnalyzer.analyzeInitializer(writerInstanceCreation)) {
				List<VariableDeclarationExpression> resourcesToRemove = Arrays.asList(resourceDeclaringBufferedWriter);
				return Optional
					.of(new UseFilesWriteStringAnalysisResult(writeInvocationAnalyzer, newBufferedIOArgumentsAnalyzer,
							tryStatement, resourcesToRemove));
			}
		} else if (bufferedWriterInstanceCreationArgument.getNodeType() == ASTNode.SIMPLE_NAME) {
			return createTransformationDataUsingFileIOResource(bufferedWriterResourceAnalyzer, writeInvocationAnalyzer,
					(SimpleName) bufferedWriterInstanceCreationArgument);
		}
		return Optional.empty();
	}

	private Optional<UseFilesWriteStringAnalysisResult> createTransformationDataUsingFileIOResource(
			TryResourceAnalyzer bufferedWriterResourceAnalyzer,
			WriteMethodInvocationAnalyzer writeInvocationAnalyzer,
			SimpleName bufferedIOArgAsSimpleName) {

		TryStatement tryStatement = bufferedWriterResourceAnalyzer.getTryStatement();
		TryResourceAnalyzer fileWriterResourceAnalyzer = new TryResourceAnalyzer();
		if (!fileWriterResourceAnalyzer.analyze(tryStatement, bufferedIOArgAsSimpleName)) {
			return Optional.empty();
		}

		VariableDeclarationFragment fileWriterResourceFragment = fileWriterResourceAnalyzer.getResourceFragment();
		VariableDeclarationExpression fileWriterResource = fileWriterResourceAnalyzer.getResource();

		FileIOAnalyzer fileIOAnalyzer = new FileIOAnalyzer(java.io.FileWriter.class.getName());
		if (!fileIOAnalyzer.analyzeFileIO(fileWriterResource)) {
			return Optional.empty();
		}

		LocalVariableUsagesASTVisitor visitor = new LocalVariableUsagesASTVisitor(fileWriterResourceFragment.getName());
		tryStatement.accept(visitor);
		List<SimpleName> usages = visitor.getUsages();
		usages.remove(fileWriterResourceFragment.getName());
		usages.remove(bufferedIOArgAsSimpleName);
		if (!usages.isEmpty()) {
			return Optional.empty();
		}
		VariableDeclarationExpression resourceDeclaringBufferedWriter = bufferedWriterResourceAnalyzer.getResource();
		List<VariableDeclarationExpression> resourcesToRemove = Arrays.asList(resourceDeclaringBufferedWriter,
				fileWriterResource);
		return Optional.of(new UseFilesWriteStringAnalysisResult(writeInvocationAnalyzer, fileIOAnalyzer, tryStatement,
				resourcesToRemove));
	}

	private boolean checkWriterVariableUsage(SimpleName writerVariableName,
			Block blockOfInvocationStatement) {
		LocalVariableUsagesASTVisitor visitor = new LocalVariableUsagesASTVisitor(
				writerVariableName);
		blockOfInvocationStatement.accept(visitor);
		int usages = visitor.getUsages()
			.size();
		return usages == 1;
	}

	private void transform(MethodInvocation methodInvocation, FilesNewBufferedIOTransformationData transformationData) {
		MethodInvocation writeStringMethodInvocation = createFilesWriteStringMethodInvocation(transformationData);
		int resourcesSize = transformationData.getTryStatement()
			.resources()
			.size();
		if (resourcesSize > 1) {
			astRewrite.replace(methodInvocation, writeStringMethodInvocation, null);
			astRewrite.remove(transformationData.getResourceToRemove(), null);
		} else {
			TryStatement tryStatementReplacement = createNewTryStatementWithoutResources(
					transformationData.getTryStatement(),
					transformationData.getWriteInvocationStatementToReplace(), writeStringMethodInvocation);
			astRewrite.replace(transformationData.getTryStatement(), tryStatementReplacement, null);
		}
		onRewrite();
	}

	private void transform(MethodInvocation methodInvocationToReplace,
			UseFilesWriteStringAnalysisResult transformationData) {

		MethodInvocation writeStringMethodInvocation = createFilesWriteStringMethodInvocation(methodInvocationToReplace,
				transformationData);
		List<VariableDeclarationExpression> resourcesToRemove = transformationData.getResourcesToRemove();
		int allResourcesSize = transformationData.getTryStatement()
			.resources()
			.size();
		if (allResourcesSize > resourcesToRemove.size()) {
			astRewrite.replace(methodInvocationToReplace, writeStringMethodInvocation, null);
			resourcesToRemove.stream()
				.forEach(resource -> astRewrite.remove(resource, null));
		} else {
			TryStatement tryStatementReplacement = createNewTryStatementWithoutResources(
					transformationData.getTryStatement(),
					transformationData.getWriteInvocationStatementToReplace(), writeStringMethodInvocation);
			astRewrite.replace(transformationData.getTryStatement(), tryStatementReplacement, null);
		}
		onRewrite();
	}

	private MethodInvocation createFilesWriteStringMethodInvocation(
			FilesNewBufferedIOTransformationData transformationData) {
		List<Expression> arguments = new ArrayList<>();
		transformationData.getArgumentsToCopy()
			.stream()
			.forEach(arg -> arguments.add((Expression) astRewrite.createCopyTarget(arg)));

		Name filesTypeName = addImport(FilesUtil.FILES_QUALIFIED_NAME,
				transformationData.getWriteInvocationStatementToReplace());
		AST ast = astRewrite.getAST();
		return NodeBuilder.newMethodInvocation(ast, filesTypeName,
				ast.newSimpleName("writeString"), arguments); //$NON-NLS-1$
	}

	private MethodInvocation createFilesWriteStringMethodInvocation(
			MethodInvocation methodInvocationToReplace,
			UseFilesWriteStringAnalysisResult transformationData) {
		AST ast = astRewrite.getAST();
		Name pathsTypeName = addImport(FilesUtil.PATHS_QUALIFIED_NAME, methodInvocationToReplace);
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
			Name charsetTypeName = addImport(FilesUtil.CHARSET_QUALIFIED_NAME, methodInvocationToReplace);
			charset = NodeBuilder.newMethodInvocation(ast, charsetTypeName, FilesUtil.DEFAULT_CHARSET);
		}

		List<Expression> arguments = new ArrayList<>();
		arguments.add(pathArgument);
		arguments.add(writeStringArgumentCopy);
		arguments.add(charset);
		Name filesTypeName = addImport(FilesUtil.FILES_QUALIFIED_NAME, methodInvocationToReplace);
		return NodeBuilder.newMethodInvocation(ast, filesTypeName,
				ast.newSimpleName("writeString"), arguments); //$NON-NLS-1$
	}

	@SuppressWarnings("unchecked")
	private TryStatement createNewTryStatementWithoutResources(TryStatement tryStatement,
			ASTNode invocationStatementToReplace,
			MethodInvocation filesWriteStringMethodInvocation) {
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

		int replacementIndex = oldBody.statements()
			.indexOf(invocationStatementToReplace);
		newBodyStatementsTypedList.remove(replacementIndex);
		ExpressionStatement invocationStatementReplacement = getASTRewrite().getAST()
			.newExpressionStatement(filesWriteStringMethodInvocation);
		newBodyStatementsTypedList.add(replacementIndex, invocationStatementReplacement);

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

}
