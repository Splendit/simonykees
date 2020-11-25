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

	Expression writeStringArgument;

	Expression bufferedIOInitializer;

	Expression bufferedWriterInstanceCreationArgument;

	List<Expression> filesNewBufferedWriterInvocationArguments;

	TryStatement tryStatement;

	Optional<UseFilesWriteStringAnalysisResult> findAnalysisResult(MethodInvocation methodInvocation,
			CompilationUnit compilationUnit) {
		if (!write.isEquivalentTo(methodInvocation.resolveMethodBinding())) {
			return Optional.empty();
		}
		writeStringArgument = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(), Expression.class)
			.get(0);

		if (methodInvocation.getLocationInParent() != ExpressionStatement.EXPRESSION_PROPERTY) {
			return Optional.empty();
		}
		ExpressionStatement writeInvocationStatement = (ExpressionStatement) methodInvocation.getParent();

		if (writeInvocationStatement.getLocationInParent() != Block.STATEMENTS_PROPERTY) {
			return Optional.empty();
		}
		Block blockOfInvocationStatement = (Block) writeInvocationStatement.getParent();

		SimpleName writerVariableSimpleName = findWriterVariableNameUsedOnce(methodInvocation,
				blockOfInvocationStatement)
					.orElse(null);
		if (writerVariableSimpleName == null) {
			return Optional.empty();
		}
		ASTNode declaringNode = compilationUnit.findDeclaringNode(writerVariableSimpleName.resolveBinding());
		if (declaringNode == null || declaringNode.getNodeType() != ASTNode.VARIABLE_DECLARATION_FRAGMENT) {
			return Optional.empty();
		}
		VariableDeclarationFragment fragmentDeclaringBufferedWriter = (VariableDeclarationFragment) declaringNode;
		tryStatement = findSurroundingTryStatement(fragmentDeclaringBufferedWriter, blockOfInvocationStatement)
			.orElse(null);
		if (tryStatement == null) {
			return Optional.empty();
		}

		bufferedIOInitializer = fragmentDeclaringBufferedWriter.getInitializer();
		if (bufferedIOInitializer == null) {
			return Optional.empty();
		}
		if (ClassRelationUtil.isNewInstanceCreationOf(bufferedIOInitializer, java.io.BufferedWriter.class.getName())) {
			ClassInstanceCreation bufferedWriterInstanceCreation = (ClassInstanceCreation) bufferedIOInitializer;
			bufferedWriterInstanceCreationArgument = FilesUtil
				.findBufferedIOArgument(bufferedWriterInstanceCreation, java.io.FileWriter.class.getName())
				.orElse(null);
			if (bufferedWriterInstanceCreationArgument == null) {
				return Optional.empty();
			}
		} else if (isFilesNewBufferedWriterInvocation(bufferedIOInitializer)) {
			MethodInvocation filesNewBufferedWriterInvocation = (MethodInvocation) bufferedIOInitializer;
			filesNewBufferedWriterInvocationArguments = ASTNodeUtil
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
		}

		if (bufferedWriterInstanceCreationArgument != null) {
			if (bufferedWriterInstanceCreationArgument.getNodeType() == ASTNode.CLASS_INSTANCE_CREATION) {
				NewBufferedIOArgumentsAnalyzer newBufferedIOArgumentsAnalyzer = new NewBufferedIOArgumentsAnalyzer();
				ClassInstanceCreation writerInstanceCreation = (ClassInstanceCreation) bufferedWriterInstanceCreationArgument;
				if (newBufferedIOArgumentsAnalyzer.analyzeInitializer(writerInstanceCreation)) {
					return Optional.of(createUseFilesWriteStringAnalysisResult(newBufferedIOArgumentsAnalyzer));
				}
			} else if (bufferedWriterInstanceCreationArgument.getNodeType() == ASTNode.SIMPLE_NAME) {

				return createTransformationDataUsingFileIOResource((SimpleName) bufferedWriterInstanceCreationArgument);
			}
		} else if (bufferedIOInitializer.getNodeType() == ASTNode.METHOD_INVOCATION) {
			MethodInvocation filesNewBufferedWriterInvocatioon = (MethodInvocation) bufferedIOInitializer;
			Expression pathExpression = filesNewBufferedWriterInvocationArguments.get(0);
			if (filesNewBufferedWriterInvocationArguments.size() == 2) {
				Expression charSetExpression = filesNewBufferedWriterInvocationArguments.get(1);
				return Optional.of(new UseFilesWriteStringAnalysisResult(filesNewBufferedWriterInvocatioon,
						pathExpression, charSetExpression, writeStringArgument));
			} else {
				return Optional.of(new UseFilesWriteStringAnalysisResult(filesNewBufferedWriterInvocatioon,
						pathExpression, writeStringArgument));
			}
		}
		return Optional.empty();
	}

	UseFilesWriteStringAnalysisResult createUseFilesWriteStringAnalysisResult(
			NewBufferedIOArgumentsAnalyzer newBufferedIOArgumentsAnalyzer) {
		Expression charsetExpression = newBufferedIOArgumentsAnalyzer.getCharsetExpression()
			.orElse(null);
		if (charsetExpression != null) {
			return new UseFilesWriteStringAnalysisResult(bufferedIOInitializer,
					newBufferedIOArgumentsAnalyzer.getPathExpressions(), writeStringArgument,
					charsetExpression);
		}
		return new UseFilesWriteStringAnalysisResult(bufferedIOInitializer,
				newBufferedIOArgumentsAnalyzer.getPathExpressions(), writeStringArgument);
	}

	private Optional<UseFilesWriteStringAnalysisResult> createTransformationDataUsingFileIOResource(
			SimpleName bufferedIOArgAsSimpleName) {
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
		UseFilesWriteStringAnalysisResult transformationData = fileIOAnalyzer.getCharset()
			.map(charSet -> new UseFilesWriteStringAnalysisResult(bufferedIOInitializer,
					pathExpressions, writeStringArgument, charSet,
					fileIOResource))
			.orElse(new UseFilesWriteStringAnalysisResult(bufferedIOInitializer, pathExpressions,
					writeStringArgument,
					fileIOResource));
		return Optional.of(transformationData);
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

	private Optional<SimpleName> findWriterVariableNameUsedOnce(MethodInvocation methodInvocation,
			Block blockOfInvocationStatement) {
		Expression methodInvocationExpression = methodInvocation.getExpression();
		if (methodInvocationExpression == null || methodInvocationExpression.getNodeType() != ASTNode.SIMPLE_NAME) {
			return Optional.empty();
		}
		SimpleName writerVariableName = (SimpleName) methodInvocationExpression;
		ITypeBinding typeBinding = writerVariableName.resolveTypeBinding();
		if (!ClassRelationUtil.isContentOfType(typeBinding, java.io.BufferedWriter.class.getName())) {
			return Optional.empty();
		}
		LocalVariableUsagesASTVisitor visitor = new LocalVariableUsagesASTVisitor(
				writerVariableName);
		blockOfInvocationStatement.accept(visitor);
		if (visitor.getUsages()
			.size() != 1) {
			return Optional.empty();
		}
		return Optional.of(writerVariableName);
	}

	private Optional<TryStatement> findSurroundingTryStatement(
			VariableDeclarationFragment fragmentDeclaringBufferedWriter, Block blockOfInvocationStatement) {
		if (fragmentDeclaringBufferedWriter.getLocationInParent() == VariableDeclarationExpression.FRAGMENTS_PROPERTY
				&& fragmentDeclaringBufferedWriter.getParent()
					.getLocationInParent() == TryStatement.RESOURCES2_PROPERTY
				&& fragmentDeclaringBufferedWriter.getParent()
					.getParent() == blockOfInvocationStatement.getParent()) {
			VariableDeclarationExpression parentVariableDeclarationExpression = (VariableDeclarationExpression) fragmentDeclaringBufferedWriter
				.getParent();
			if (parentVariableDeclarationExpression.fragments()
				.size() != 1) {
				return Optional.empty();
			}
			return Optional.of((TryStatement) parentVariableDeclarationExpression.getParent());
		} else {
			return Optional.empty();
		}
	}
}
