package eu.jsparrow.core.visitor.files;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import eu.jsparrow.core.visitor.sub.SignatureData;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.helper.LocalVariableUsagesASTVisitor;

public class UseFilesWriteStringAnalyzer {

	private final SignatureData write = new SignatureData(java.io.Writer.class, "write", java.lang.String.class); //$NON-NLS-1$

	Optional<UseFilesWriteStringAnalysisResult> analyze(MethodInvocation methodInvocation,
			CompilationUnit compilationUnit) {

		Expression methodInvocationExpression = methodInvocation.getExpression();
		if (methodInvocationExpression == null || methodInvocationExpression
			.getNodeType() != ASTNode.SIMPLE_NAME) {
			return Optional.empty();
		}
		SimpleName writerVariableSimpleName = (SimpleName) methodInvocationExpression;

		if (methodInvocation.getLocationInParent() != ExpressionStatement.EXPRESSION_PROPERTY) {
			return Optional.empty();
		}
		ExpressionStatement writeInvocationStatement = (ExpressionStatement) methodInvocation.getParent();

		if (!write.isEquivalentTo(methodInvocation.resolveMethodBinding())) {
			return Optional.empty();
		}
		Expression writeStringArgument = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(), Expression.class)
			.get(0);

		if (writeInvocationStatement.getLocationInParent() != Block.STATEMENTS_PROPERTY) {
			return Optional.empty();
		}
		
		Block blockOfInvocationStatement = (Block) writeInvocationStatement.getParent();
		if (blockOfInvocationStatement.getLocationInParent() != TryStatement.BODY_PROPERTY) {
			return Optional.empty();
		}
		TryStatement tryStatement = (TryStatement) blockOfInvocationStatement.getParent();

		VariableDeclarationFragment fragmentDeclaringBufferedWriter = findFragmentDeclaringBufferedWriter(
				writerVariableSimpleName, blockOfInvocationStatement, compilationUnit).orElse(null);
		if (fragmentDeclaringBufferedWriter == null) {
			return Optional.empty();
		}

		if (fragmentDeclaringBufferedWriter
			.getLocationInParent() != VariableDeclarationExpression.FRAGMENTS_PROPERTY) {
			return Optional.empty();
		}
		VariableDeclarationExpression parentVariableDeclarationExpression = (VariableDeclarationExpression) fragmentDeclaringBufferedWriter
			.getParent();

		if (parentVariableDeclarationExpression.fragments()
			.size() != 1) {
			return Optional.empty();
		}

		if (parentVariableDeclarationExpression.getLocationInParent() != TryStatement.RESOURCES2_PROPERTY) {
			return Optional.empty();
		}

		if (parentVariableDeclarationExpression.getParent() != tryStatement) {
			return Optional.empty();
		}

		Expression bufferedIOInitializer;
		bufferedIOInitializer = fragmentDeclaringBufferedWriter.getInitializer();
		if (bufferedIOInitializer == null) {
			return Optional.empty();
		}
		if (ClassRelationUtil.isNewInstanceCreationOf(bufferedIOInitializer, java.io.BufferedWriter.class.getName())) {
			ClassInstanceCreation bufferedWriterInstanceCreation = (ClassInstanceCreation) bufferedIOInitializer;
			return analyze(tryStatement, bufferedWriterInstanceCreation, writeStringArgument);
		} else if (isFilesNewBufferedWriterInvocation(bufferedIOInitializer)) {
			MethodInvocation filesNewBufferedWriterInvocation = (MethodInvocation) bufferedIOInitializer;
			return analyzeNewBufferedWriterInvocation(tryStatement, filesNewBufferedWriterInvocation,
					writeStringArgument);
		}
		return Optional.empty();
	}

	private Optional<UseFilesWriteStringAnalysisResult> analyzeNewBufferedWriterInvocation(
			TryStatement tryStatement,
			MethodInvocation filesNewBufferedWriterInvocation, Expression writeStringArgument) {
		List<Expression> filesNewBufferedWriterInvocationArguments = ASTNodeUtil
			.convertToTypedList(filesNewBufferedWriterInvocation.arguments(), Expression.class);
		if (filesNewBufferedWriterInvocationArguments.isEmpty()
				|| filesNewBufferedWriterInvocationArguments.size() > 2) {
			return Optional.empty();
		}

		ITypeBinding firstArgumentTypeBinding = filesNewBufferedWriterInvocationArguments.get(0)
			.resolveTypeBinding();
		if (!ClassRelationUtil.isContentOfType(firstArgumentTypeBinding, java.nio.file.Path.class.getName())) {
			return Optional.empty();
		}
		if (filesNewBufferedWriterInvocationArguments.size() == 2) {
			ITypeBinding secondArgumentTypeBinding = filesNewBufferedWriterInvocationArguments.get(1)
				.resolveTypeBinding();
			if (!ClassRelationUtil.isContentOfType(secondArgumentTypeBinding,
					java.nio.charset.Charset.class.getName())) {
				return Optional.empty();
			}
		}
		Expression pathExpression = filesNewBufferedWriterInvocationArguments.get(0);
		if (filesNewBufferedWriterInvocationArguments.size() == 2) {
			Expression charSetExpression = filesNewBufferedWriterInvocationArguments.get(1);
			return Optional.of(new UseFilesWriteStringAnalysisResult(tryStatement, filesNewBufferedWriterInvocation,
					pathExpression, charSetExpression, writeStringArgument));
		} else {
			return Optional.of(new UseFilesWriteStringAnalysisResult(tryStatement, filesNewBufferedWriterInvocation,
					pathExpression, writeStringArgument));
		}
	}

	Optional<UseFilesWriteStringAnalysisResult> analyze(TryStatement tryStatement,
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

	private boolean isFilesNewBufferedWriterInvocation(Expression initializer) {
		if (initializer.getNodeType() != ASTNode.METHOD_INVOCATION) {
			return false;
		}
		MethodInvocation invocation = (MethodInvocation) initializer;
		Expression invocationExpression = invocation.getExpression();
		if (invocationExpression == null) {
			return false;
		}
		IMethodBinding methodBinding = invocation.resolveMethodBinding();
		if (!ClassRelationUtil.isContentOfType(methodBinding
			.getDeclaringClass(), java.nio.file.Files.class.getName())) {
			return false;
		}
		if (!Modifier.isStatic(methodBinding.getModifiers())) {
			return false;
		}
		return methodBinding.getName()
			.equals("newBufferedWriter"); //$NON-NLS-1$
	}

	private boolean checkWriterVariableNameTypeAndUsage(SimpleName writerVariableName,
			Block blockOfInvocationStatement) {
		ITypeBinding typeBinding = writerVariableName.resolveTypeBinding();
		if (!ClassRelationUtil.isContentOfType(typeBinding, java.io.BufferedWriter.class.getName())) {
			return false;
		}
		LocalVariableUsagesASTVisitor visitor = new LocalVariableUsagesASTVisitor(
				writerVariableName);
		blockOfInvocationStatement.accept(visitor);
		return visitor.getUsages()
			.size() == 1;
	}

	private Optional<VariableDeclarationFragment> findFragmentDeclaringBufferedWriter(
			SimpleName writerVariableSimpleName,
			Block blockOfInvocationStatement, CompilationUnit compilationUnit) {
		if (!checkWriterVariableNameTypeAndUsage(writerVariableSimpleName,
				blockOfInvocationStatement)) {
			return Optional.empty();
		}

		ASTNode declaringNode = compilationUnit.findDeclaringNode(writerVariableSimpleName.resolveBinding());
		if (declaringNode == null || declaringNode.getNodeType() != ASTNode.VARIABLE_DECLARATION_FRAGMENT) {
			return Optional.empty();
		}
		VariableDeclarationFragment fragmentDeclaringBufferedWriter = (VariableDeclarationFragment) declaringNode;
		return Optional.of(fragmentDeclaringBufferedWriter);
	}
}
