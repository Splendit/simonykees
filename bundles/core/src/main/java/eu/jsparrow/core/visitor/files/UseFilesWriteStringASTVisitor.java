package eu.jsparrow.core.visitor.files;

import java.util.ArrayList;
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

		VariableDeclarationFragment fragmentDeclaringBufferedWriter = findFragmentDeclaringBufferedWriter(
				writeInvocationAnalyzer.getWriterVariableSimpleName(), getCompilationUnit()).orElse(null);
		if (fragmentDeclaringBufferedWriter == null) {
			return true;
		}

		Expression bufferedIOInitializer = fragmentDeclaringBufferedWriter.getInitializer();
		if (bufferedIOInitializer == null) {
			return true;
		}

		if (fragmentDeclaringBufferedWriter
			.getLocationInParent() != VariableDeclarationExpression.FRAGMENTS_PROPERTY) {
			return true;
		}
		VariableDeclarationExpression parentVariableDeclarationExpression = (VariableDeclarationExpression) fragmentDeclaringBufferedWriter
			.getParent();

		if (parentVariableDeclarationExpression.fragments()
			.size() != 1) {
			return true;
		}

		if (parentVariableDeclarationExpression.getLocationInParent() != TryStatement.RESOURCES2_PROPERTY) {
			return true;
		}
		TryStatement tryStatement = (TryStatement) parentVariableDeclarationExpression.getParent();

		if (writeInvocationAnalyzer.getBlockOfInvocationStatement()
			.getLocationInParent() != TryStatement.BODY_PROPERTY) {
			return true;
		}

		if (writeInvocationAnalyzer.getBlockOfInvocationStatement()
			.getParent() != tryStatement) {
			return true;
		}

		if (!checkWriterVariableUsage(writeInvocationAnalyzer.getWriterVariableSimpleName(),
				writeInvocationAnalyzer.getBlockOfInvocationStatement())) {
			return true;
		}
		FilesNewBufferedIOTransformationData findFilesNewBufferedIOTransformationData = findFilesNewBufferedIOTransformationData(
				tryStatement, parentVariableDeclarationExpression,
				writeInvocationAnalyzer, bufferedIOInitializer).orElse(null);
		if (findFilesNewBufferedIOTransformationData != null) {
			transform(methodInvocation, findFilesNewBufferedIOTransformationData);
		} else {
			findResult(writeInvocationAnalyzer.getCharSequenceArgument(), bufferedIOInitializer, tryStatement)
				.ifPresent(result -> {
					transform(methodInvocation, result);
				});
		}
		return true;
	}

	private Optional<FilesNewBufferedIOTransformationData> findFilesNewBufferedIOTransformationData(
			TryStatement tryStatement, VariableDeclarationExpression resourceToRemove,
			WriteMethodInvocationAnalyzer writeInvocationAnalyzer,
			Expression bufferedIOInitializer) {

		if (bufferedIOInitializer.getNodeType() != ASTNode.METHOD_INVOCATION) {
			return Optional.empty();
		}
		MethodInvocation methodInvocation = (MethodInvocation) bufferedIOInitializer;
		Expression invocationExpression = methodInvocation.getExpression();
		if (invocationExpression == null) {
			return Optional.empty();
		}
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
		List<Expression> argumentsToCopy = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(),
				Expression.class);
		if (argumentsToCopy.isEmpty()) {
			return Optional.empty();
		}
		ITypeBinding firstArgumentTypeBinding = argumentsToCopy.get(0)
			.resolveTypeBinding();
		if (!ClassRelationUtil.isContentOfType(firstArgumentTypeBinding, java.nio.file.Path.class.getName())) {
			return Optional.empty();
		}
		int firstOpenOptionArgumentIndex = 1;
		if (argumentsToCopy.size() > 1) {
			ITypeBinding secondArgumentTypeBinding = argumentsToCopy.get(1)
				.resolveTypeBinding();
			if (ClassRelationUtil.isContentOfType(secondArgumentTypeBinding,
					java.nio.charset.Charset.class.getName())) {
				firstOpenOptionArgumentIndex++;
			}
		}
		for (int i = firstOpenOptionArgumentIndex; i < argumentsToCopy.size(); i++) {
			ITypeBinding argumentTypeBinding = argumentsToCopy.get(i)
				.resolveTypeBinding();
			if (!ClassRelationUtil.isContentOfType(argumentTypeBinding,
					java.nio.file.OpenOption.class.getName())) {
				return Optional.empty();
			}
		}
		argumentsToCopy.add(1, writeInvocationAnalyzer.getCharSequenceArgument());
		return Optional.of(
				new FilesNewBufferedIOTransformationData(tryStatement, resourceToRemove,
						writeInvocationAnalyzer.getWriteInvocationStatementToReplace(), argumentsToCopy));

	}

	private Optional<UseFilesWriteStringAnalysisResult> findResult(Expression writeStringArgument,
			Expression bufferedIOInitializer, TryStatement tryStatement) {
		if (ClassRelationUtil.isNewInstanceCreationOf(bufferedIOInitializer, java.io.BufferedWriter.class.getName())) {
			ClassInstanceCreation bufferedWriterInstanceCreation = (ClassInstanceCreation) bufferedIOInitializer;
			return findResultByBufferedWriterInstanceCreation(tryStatement, bufferedWriterInstanceCreation,
					writeStringArgument);
		}
		return Optional.empty();
	}

	Optional<UseFilesWriteStringAnalysisResult> findResultByBufferedWriterInstanceCreation(TryStatement tryStatement,
			ClassInstanceCreation bufferedWriterInstanceCreation, Expression writeStringArgument) {
		Expression bufferedWriterInstanceCreationArgument = FilesUtil
			.findBufferedIOArgument(bufferedWriterInstanceCreation, java.io.FileWriter.class.getName())
			.orElse(null);
		if (bufferedWriterInstanceCreationArgument == null) {
			return Optional.empty();
		}
		if (bufferedWriterInstanceCreationArgument.getNodeType() == ASTNode.CLASS_INSTANCE_CREATION) {
			NewBufferedIOArgumentsAnalyzer newBufferedIOArgumentsAnalyzer = new NewBufferedIOArgumentsAnalyzer();
			ClassInstanceCreation writerInstanceCreation = (ClassInstanceCreation) bufferedWriterInstanceCreationArgument;
			if (newBufferedIOArgumentsAnalyzer.analyzeInitializer(writerInstanceCreation)) {
				Expression charsetExpression = newBufferedIOArgumentsAnalyzer.getCharsetExpression()
					.orElse(null);
				if (charsetExpression != null) {
					return Optional
						.of(new UseFilesWriteStringAnalysisResult(tryStatement, bufferedWriterInstanceCreation,
								newBufferedIOArgumentsAnalyzer.getPathExpressions(), writeStringArgument,
								charsetExpression));
				}
				return Optional.of(new UseFilesWriteStringAnalysisResult(tryStatement, bufferedWriterInstanceCreation,
						newBufferedIOArgumentsAnalyzer.getPathExpressions(), writeStringArgument));
			}
		} else if (bufferedWriterInstanceCreationArgument.getNodeType() == ASTNode.SIMPLE_NAME) {
			return createTransformationDataUsingFileIOResource(tryStatement, bufferedWriterInstanceCreation,
					(SimpleName) bufferedWriterInstanceCreationArgument,
					writeStringArgument);
		}
		return Optional.empty();
	}

	private Optional<UseFilesWriteStringAnalysisResult> createTransformationDataUsingFileIOResource(
			TryStatement tryStatement,
			ClassInstanceCreation bufferedWriterInstanceCreation,
			SimpleName bufferedIOArgAsSimpleName, Expression writeStringArgument) {
		VariableDeclarationFragment fileIOResource = FilesUtil
			.findVariableDeclarationFragmentAsResource(bufferedIOArgAsSimpleName,
					tryStatement)
			.orElse(null);
		if (fileIOResource == null) {
			return Optional.empty();
		}

		FileIOAnalyzer fileIOAnalyzer = new FileIOAnalyzer(java.io.FileWriter.class.getName());
		if (!fileIOAnalyzer.analyzeFileIO((VariableDeclarationExpression) fileIOResource.getParent())) {
			return Optional.empty();
		}

		LocalVariableUsagesASTVisitor visitor = new LocalVariableUsagesASTVisitor(fileIOResource.getName());
		tryStatement.accept(visitor);
		List<SimpleName> usages = visitor.getUsages();
		usages.remove(fileIOResource.getName());
		usages.remove(bufferedIOArgAsSimpleName);
		if (!usages.isEmpty()) {
			return Optional.empty();
		}

		List<Expression> pathExpressions = fileIOAnalyzer.getPathExpressions();
		Expression charsetExpression = fileIOAnalyzer.getCharset()
			.orElse(null);

		if (charsetExpression != null) {
			return Optional.of(new UseFilesWriteStringAnalysisResult(tryStatement,
					bufferedWriterInstanceCreation,
					pathExpressions, writeStringArgument, charsetExpression,
					fileIOResource));
		}
		return Optional.of(new UseFilesWriteStringAnalysisResult(tryStatement,
				bufferedWriterInstanceCreation, pathExpressions,
				writeStringArgument,
				fileIOResource));
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

	private Optional<VariableDeclarationFragment> findFragmentDeclaringBufferedWriter(
			SimpleName writerVariableSimpleName, CompilationUnit compilationUnit) {
		ASTNode declaringNode = compilationUnit.findDeclaringNode(writerVariableSimpleName.resolveBinding());
		if (declaringNode == null || declaringNode.getNodeType() != ASTNode.VARIABLE_DECLARATION_FRAGMENT) {
			return Optional.empty();
		}
		VariableDeclarationFragment fragmentDeclaringBufferedWriter = (VariableDeclarationFragment) declaringNode;
		return Optional.of(fragmentDeclaringBufferedWriter);
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
					methodInvocation.getParent(), writeStringMethodInvocation);
			astRewrite.replace(transformationData.getTryStatement(), tryStatementReplacement, null);
		}
		onRewrite();
	}

	private void transform(MethodInvocation methodInvocation, UseFilesWriteStringAnalysisResult transformationData) {
		/**
		 * TODO:
		 * <p>
		 * 1. use the method createNewTryStatement to completely replace the try
		 * statement with a new one. This is a JDT bug that cannot handle TWR
		 * statements properly. For this reason, the TryStatement has to be
		 * provided from the transformationData. I tried to track it back but it
		 * is very complicated process. Maybe you can do it faster.
		 * <p>
		 * 2. If you have a transformationData object, that should provide
		 * everything you need for the transformation. You should not have more
		 * logic here that gets the parents of a node until you reach the node
		 * to transform.
		 */
		Expression bufferedIOInitializer = transformationData.getBufferedIOInitializer();
		ASTNode bufferedIODeclarationFragment = bufferedIOInitializer.getParent();
		ASTNode bufferedIOResourceToRemove = bufferedIODeclarationFragment.getParent();
		MethodInvocation writeStringMethodInvocation = createFilesWriteStringMethodInvocation(transformationData);
		if (calculateResourcesSizeAfterTransformation(transformationData) > 0) {

			astRewrite.replace(methodInvocation, writeStringMethodInvocation, null);
			astRewrite.remove(bufferedIOResourceToRemove, null);
			transformationData.getFileIOResource()
				.ifPresent(resource -> astRewrite.remove(resource.getParent(),
						null));
		} else {
			TryStatement tryStatementReplacement = createNewTryStatementWithoutResources(
					transformationData.getTryStatement(),
					methodInvocation.getParent(), writeStringMethodInvocation);
			astRewrite.replace(transformationData.getTryStatement(), tryStatementReplacement, null);
		}
		onRewrite();
	}

	private int calculateResourcesSizeAfterTransformation(UseFilesWriteStringAnalysisResult transformationData) {
		int sizeBefore = transformationData.getTryStatement()
			.resources()
			.size();
		if (transformationData.getFileIOResource()
			.isPresent()) {
			return sizeBefore - 2;
		}
		return sizeBefore - 1;
	}

	private MethodInvocation createFilesWriteStringMethodInvocation(
			FilesNewBufferedIOTransformationData transformationData) {
		List<Expression> arguments = new ArrayList<>();
		transformationData.getArgumentsToCopy()
			.stream()
			.forEach(arg -> arguments.add((Expression) astRewrite.createCopyTarget(arg)));

		Name filesTypeName = addImport(FilesUtil.FILES_QUALIFIED_NAME,
				transformationData.getInvocationStatementToReplace());
		AST ast = astRewrite.getAST();
		return NodeBuilder.newMethodInvocation(ast, filesTypeName,
				ast.newSimpleName("writeString"), arguments); //$NON-NLS-1$
	}

	private MethodInvocation createFilesWriteStringMethodInvocation(
			UseFilesWriteStringAnalysisResult transformationData) {
		AST ast = astRewrite.getAST();
		Name pathsTypeName = addImport(FilesUtil.PATHS_QUALIFIED_NAME,
				transformationData.getBufferedIOInitializer());
		List<Expression> pathsGetArguments = transformationData.getPathExpressions()
			.stream()
			.map(pathExpression -> (Expression) astRewrite.createCopyTarget(pathExpression))
			.collect(Collectors.toList());
		Expression pathArgument = NodeBuilder.newMethodInvocation(ast, pathsTypeName,
				ast.newSimpleName(FilesUtil.GET), pathsGetArguments);

		Expression writeStringArgumentCopy = (Expression) astRewrite
			.createCopyTarget(transformationData.getWriteStringArgument());

		Expression charset = transformationData.getCharSet()
			.map(exp -> (Expression) astRewrite.createCopyTarget(exp))
			.orElse(null);
		if (charset == null) {
			Name charsetTypeName = addImport(FilesUtil.CHARSET_QUALIFIED_NAME,
					transformationData.getBufferedIOInitializer());
			charset = NodeBuilder.newMethodInvocation(ast, charsetTypeName, FilesUtil.DEFAULT_CHARSET);
		}

		List<Expression> arguments = new ArrayList<>();
		arguments.add(pathArgument);
		arguments.add(writeStringArgumentCopy);
		arguments.add(charset);
		Name filesTypeName = addImport(FilesUtil.FILES_QUALIFIED_NAME,
				transformationData.getBufferedIOInitializer());
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
