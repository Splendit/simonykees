package eu.jsparrow.core.visitor.files;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
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
import eu.jsparrow.rules.common.visitor.helper.LocalVariableUsagesVisitor;

/**
 * Helper class to determine whether a method invocation is an invocation of
 * <br>
 * {@link java.io.Writer#write(String)} <br>
 * which can be transformed by the <br>
 * {@link eu.jsparrow.core.visitor.files.UseFilesWriteStringASTVisitor}.
 *
 * @since 3.24.0
 */
class WriteMethodInvocationAnalyzer {

	private final SignatureData write = new SignatureData(java.io.Writer.class, "write", java.lang.String.class); //$NON-NLS-1$
	private ExpressionStatement writeInvocationStatementToReplace;
	private Expression charSequenceArgument;
	private Block blockOfInvocationStatement;
	private TryResourceAnalyzer bufferedWriterResourceAnalyzer;
	private FilesNewBufferedIOTransformationData invocationReplecementDataWithFilesMethod;
	private UseFilesWriteStringAnalysisResult invocationReplacementDataWithConstructor;
	private List<VariableDeclarationExpression> resourcesToRemove = new ArrayList<>();

	boolean analyze(TryStatement tryStatement, MethodInvocation methodInvocation) {
		Expression methodInvocationExpression = methodInvocation.getExpression();
		if (methodInvocationExpression == null || methodInvocationExpression
			.getNodeType() != ASTNode.SIMPLE_NAME) {
			return false;
		}
		SimpleName writerVariableSimpleName = (SimpleName) methodInvocationExpression;

		if (methodInvocation.getLocationInParent() != ExpressionStatement.EXPRESSION_PROPERTY) {
			return false;
		}
		writeInvocationStatementToReplace = (ExpressionStatement) methodInvocation.getParent();

		if (!write.isEquivalentTo(methodInvocation.resolveMethodBinding())) {
			return false;
		}
		charSequenceArgument = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(), Expression.class)
			.get(0);

		if (writeInvocationStatementToReplace.getLocationInParent() != Block.STATEMENTS_PROPERTY) {
			return false;
		}
		blockOfInvocationStatement = (Block) writeInvocationStatementToReplace.getParent();

		if (blockOfInvocationStatement.getParent() != tryStatement) {
			return false;
		}

		bufferedWriterResourceAnalyzer = new TryResourceAnalyzer();
		if (!bufferedWriterResourceAnalyzer.analyze(tryStatement, writerVariableSimpleName)) {
			return false;
		}

		if (!checkWriterVariableUsage(writerVariableSimpleName, blockOfInvocationStatement)) {
			return false;
		}

		Expression bufferedIOInitializer = bufferedWriterResourceAnalyzer.getResourceInitializer();
		invocationReplecementDataWithFilesMethod = findFilesNewBufferedIOTransformationData(
				bufferedWriterResourceAnalyzer).orElse(null);
		
		if(invocationReplecementDataWithFilesMethod != null) {
			resourcesToRemove.add(bufferedWriterResourceAnalyzer.getResource());
			return true;
		}

		if (!ClassRelationUtil.isNewInstanceCreationOf(bufferedIOInitializer,
				java.io.BufferedWriter.class.getName())) {
			return false;
		}

		ClassInstanceCreation bufferedWriterInstanceCreation = (ClassInstanceCreation) bufferedIOInitializer;
		Expression bufferedWriterInstanceCreationArgument = FilesUtil
			.findBufferedIOArgument(bufferedWriterInstanceCreation, java.io.FileWriter.class.getName())
			.orElse(null);
		if (bufferedWriterInstanceCreationArgument == null) {
			return false;
		}

		if (bufferedWriterInstanceCreationArgument.getNodeType() == ASTNode.CLASS_INSTANCE_CREATION) {
			invocationReplacementDataWithConstructor = findInvocationStatementReplacementData(
					(ClassInstanceCreation) bufferedWriterInstanceCreationArgument, bufferedWriterResourceAnalyzer)
						.orElse(null);
			resourcesToRemove.add(bufferedWriterResourceAnalyzer.getResource());
		}

		if (bufferedWriterInstanceCreationArgument.getNodeType() == ASTNode.SIMPLE_NAME) {
			SimpleName bufferedIOArgAsSimpleName = (SimpleName) bufferedWriterInstanceCreationArgument;
			TryResourceAnalyzer fileWriterResourceAnalyzer = new TryResourceAnalyzer();
			if (!fileWriterResourceAnalyzer.analyze(tryStatement, bufferedIOArgAsSimpleName)) {
				return false;
			}
			invocationReplacementDataWithConstructor = findInvocationStatementReplacementData(tryStatement,
					bufferedWriterResourceAnalyzer,
					bufferedIOArgAsSimpleName, fileWriterResourceAnalyzer).orElse(null);
			resourcesToRemove.add(bufferedWriterResourceAnalyzer.getResource());
			resourcesToRemove.add(fileWriterResourceAnalyzer.getResource());
		}
		return invocationReplacementDataWithConstructor != null;
	}

	private boolean checkWriterVariableUsage(SimpleName writerVariableName,
			Block blockOfInvocationStatement) {
		LocalVariableUsagesVisitor visitor = new LocalVariableUsagesVisitor(
				writerVariableName);
		blockOfInvocationStatement.accept(visitor);
		int usages = visitor.getUsages()
			.size();
		return usages == 1;
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
			TryResourceAnalyzer bufferedWriterResourceAnalyzer) {

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

		argumentsToCopy.add(1, getCharSequenceArgument());
		VariableDeclarationExpression resourceToRemove = bufferedWriterResourceAnalyzer.getResource();
		return Optional.of(
				new FilesNewBufferedIOTransformationData(resourceToRemove,
						getWriteInvocationStatementToReplace(), argumentsToCopy));

	}

	private Optional<UseFilesWriteStringAnalysisResult> findInvocationStatementReplacementData(
			ClassInstanceCreation writerInstanceCreation, TryResourceAnalyzer bufferedWriterResourceAnalyze) {
		NewBufferedIOArgumentsAnalyzer newBufferedIOArgumentsAnalyzer = new NewBufferedIOArgumentsAnalyzer();
		if (!newBufferedIOArgumentsAnalyzer.analyzeInitializer(writerInstanceCreation)) {
			return Optional.empty();
		}

		VariableDeclarationExpression resourceDeclaringBufferedWriter = bufferedWriterResourceAnalyze
			.getResource();
		List<VariableDeclarationExpression> resourcesToRemove = Arrays
			.asList(resourceDeclaringBufferedWriter);
		return Optional.of(
				new UseFilesWriteStringAnalysisResult(this, newBufferedIOArgumentsAnalyzer, resourcesToRemove));
	}

	private Optional<UseFilesWriteStringAnalysisResult> findInvocationStatementReplacementData(
			TryStatement tryStatement,
			TryResourceAnalyzer bufferedWriterResourceAnalyzer,
			SimpleName bufferedIOArgAsSimpleName,
			TryResourceAnalyzer fileWriterResourceAnalyzer) {

		VariableDeclarationExpression resourceDeclaringBufferedWriter = bufferedWriterResourceAnalyzer
			.getResource();
		VariableDeclarationFragment fileWriterResourceFragment = fileWriterResourceAnalyzer
			.getResourceFragment();
		VariableDeclarationExpression fileWriterResource = fileWriterResourceAnalyzer.getResource();

		FileIOAnalyzer fileIOAnalyzer = new FileIOAnalyzer(java.io.FileWriter.class.getName());
		if (!fileIOAnalyzer.analyzeFileIO(fileWriterResource)) {
			return Optional.empty();
		}

		LocalVariableUsagesVisitor visitor = new LocalVariableUsagesVisitor(
				fileWriterResourceFragment.getName());
		tryStatement.accept(visitor);
		List<SimpleName> usages = visitor.getUsages();
		usages.remove(fileWriterResourceFragment.getName());
		usages.remove(bufferedIOArgAsSimpleName);
		if (!usages.isEmpty()) {
			return Optional.empty();
		}
		List<VariableDeclarationExpression> resourcesToRemove = Arrays.asList(resourceDeclaringBufferedWriter,
				fileWriterResource);

		return Optional.of(new UseFilesWriteStringAnalysisResult(this, fileIOAnalyzer,
				resourcesToRemove));
	}

	ExpressionStatement getWriteInvocationStatementToReplace() {
		return writeInvocationStatementToReplace;
	}

	Expression getCharSequenceArgument() {
		return charSequenceArgument;
	}

	Block getBlockOfInvocationStatement() {
		return blockOfInvocationStatement;
	}

	TryResourceAnalyzer getBufferedWriterResourceAnalyzer() {
		return bufferedWriterResourceAnalyzer;
	}

	Optional<FilesNewBufferedIOTransformationData> getInvocationReplecementDataWithFilesMethod() {
		return Optional.ofNullable(invocationReplecementDataWithFilesMethod);
	}

	Optional<UseFilesWriteStringAnalysisResult> getInvocationReplacementDataWithConstructor() {
		return Optional.ofNullable(invocationReplacementDataWithConstructor);
	}
}
