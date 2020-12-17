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
 * 
 * {@link java.io.Writer#write(String)} which can be transformed by the
 * {@link eu.jsparrow.core.visitor.files.UseFilesWriteStringASTVisitor}.
 *
 * @since 3.24.0
 */
class WriteMethodInvocationAnalyzer {

	private final SignatureData write = new SignatureData(java.io.Writer.class, "write", java.lang.String.class); //$NON-NLS-1$
	private final TryStatement tryStatement;
	private final List<FilesNewBufferedIOTransformationData> filesNewBufferedWriterInvocationDataList = new ArrayList<>();
	private final List<UseFilesWriteStringAnalysisResult> bufferedWriterInstanceCreationDataList = new ArrayList<>();

	public WriteMethodInvocationAnalyzer(TryStatement tryStatement) {
		this.tryStatement = tryStatement;
	}

	void analyze(MethodInvocation methodInvocation) {
		Expression methodInvocationExpression = methodInvocation.getExpression();
		if (methodInvocationExpression == null || methodInvocationExpression
			.getNodeType() != ASTNode.SIMPLE_NAME) {
			return;
		}
		SimpleName writerVariableSimpleName = (SimpleName) methodInvocationExpression;

		if (methodInvocation.getLocationInParent() != ExpressionStatement.EXPRESSION_PROPERTY) {
			return;
		}
		ExpressionStatement writeInvocationStatementToReplace = (ExpressionStatement) methodInvocation.getParent();

		if (!write.isEquivalentTo(methodInvocation.resolveMethodBinding())) {
			return;
		}
		Expression charSequenceArgument = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(), Expression.class)
			.get(0);

		if (writeInvocationStatementToReplace.getLocationInParent() != Block.STATEMENTS_PROPERTY) {
			return;
		}
		Block blockOfInvocationStatement = (Block) writeInvocationStatementToReplace.getParent();

		if (blockOfInvocationStatement.getParent() != tryStatement) {
			return;
		}

		TryResourceAnalyzer bufferedWriterResourceAnalyzer = new TryResourceAnalyzer();
		if (!bufferedWriterResourceAnalyzer.analyze(tryStatement, writerVariableSimpleName)) {
			return;
		}

		if (!checkWriterVariableUsage(writerVariableSimpleName, blockOfInvocationStatement)) {
			return;
		}

		Expression bufferedIOInitializer = bufferedWriterResourceAnalyzer.getResourceInitializer();
		if (bufferedIOInitializer.getNodeType() == ASTNode.METHOD_INVOCATION) {
			MethodInvocation bufferedIOInitializerMethodInvocation = (MethodInvocation) bufferedIOInitializer;
			VariableDeclarationExpression resourceToRemove = bufferedWriterResourceAnalyzer.getResource();
			findTransformationDataUsingFilesNewBufferedWriter(
					writeInvocationStatementToReplace, charSequenceArgument,
					bufferedIOInitializerMethodInvocation, resourceToRemove)
						.ifPresent(filesNewBufferedWriterInvocationDataList::add);
		} else if (ClassRelationUtil.isNewInstanceCreationOf(bufferedIOInitializer,
				java.io.BufferedWriter.class.getName())) {
			findTransformationDataUsingBufferedWriterConstructor(
					writeInvocationStatementToReplace, charSequenceArgument,
					(ClassInstanceCreation) bufferedIOInitializer, bufferedWriterResourceAnalyzer)
						.ifPresent(bufferedWriterInstanceCreationDataList::add);
		}
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

	private Optional<FilesNewBufferedIOTransformationData> findTransformationDataUsingFilesNewBufferedWriter(
			ExpressionStatement writeInvocationStatementToReplace, Expression charSequenceArgument,
			MethodInvocation bufferedIOInitializerMethodInvocation,
			VariableDeclarationExpression resourceToRemove) {

		IMethodBinding methodBinding = bufferedIOInitializerMethodInvocation.resolveMethodBinding();

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

		List<Expression> argumentsToCopy = ASTNodeUtil.convertToTypedList(
				bufferedIOInitializerMethodInvocation.arguments(),
				Expression.class);

		argumentsToCopy.add(1, charSequenceArgument);

		return Optional.of(
				new FilesNewBufferedIOTransformationData(resourceToRemove,
						writeInvocationStatementToReplace, argumentsToCopy));

	}

	private Optional<UseFilesWriteStringAnalysisResult> findTransformationDataUsingBufferedWriterConstructor(
			ExpressionStatement writeInvocationStatementToReplace, Expression charSequenceArgument,
			ClassInstanceCreation bufferedWriterInstanceCreation, TryResourceAnalyzer bufferedWriterResourceAnalyzer) {

		Expression bufferedWriterInstanceCreationArgument = FilesUtil
			.findBufferedIOArgument(bufferedWriterInstanceCreation, java.io.FileWriter.class.getName())
			.orElse(null);

		if (bufferedWriterInstanceCreationArgument != null) {
			if (bufferedWriterInstanceCreationArgument.getNodeType() == ASTNode.CLASS_INSTANCE_CREATION) {
				return findTransformationDataUsingWriterInstanceCreation(writeInvocationStatementToReplace,
						charSequenceArgument, bufferedWriterResourceAnalyzer,
						(ClassInstanceCreation) bufferedWriterInstanceCreationArgument);

			}
			if (bufferedWriterInstanceCreationArgument.getNodeType() == ASTNode.SIMPLE_NAME) {
				return findTransformationDataUsingWriterResource(writeInvocationStatementToReplace,
						charSequenceArgument, bufferedWriterResourceAnalyzer,
						(SimpleName) bufferedWriterInstanceCreationArgument);
			}
		}
		return Optional.empty();
	}

	private Optional<UseFilesWriteStringAnalysisResult> findTransformationDataUsingWriterInstanceCreation(
			ExpressionStatement writeInvocationStatementToReplace, Expression charSequenceArgument,
			TryResourceAnalyzer bufferedWriterResourceAnalyzer,
			ClassInstanceCreation writerInstanceCreation) {
		NewBufferedIOArgumentsAnalyzer newBufferedIOArgumentsAnalyzer = new NewBufferedIOArgumentsAnalyzer();
		if (!newBufferedIOArgumentsAnalyzer.analyzeInitializer(writerInstanceCreation)) {
			return Optional.empty();
		}

		return Optional.of(new UseFilesWriteStringAnalysisResult(
				Arrays.asList(bufferedWriterResourceAnalyzer.getResource()), writeInvocationStatementToReplace,
				charSequenceArgument, newBufferedIOArgumentsAnalyzer));
	}

	private Optional<UseFilesWriteStringAnalysisResult> findTransformationDataUsingWriterResource(
			ExpressionStatement writeInvocationStatementToReplace, Expression charSequenceArgument,
			TryResourceAnalyzer bufferedWriterResourceAnalyzer,
			SimpleName bufferedIOArgAsSimpleName) {
		TryResourceAnalyzer fileWriterResourceAnalyzer = new TryResourceAnalyzer();

		if (!fileWriterResourceAnalyzer.analyze(tryStatement, bufferedIOArgAsSimpleName)) {
			return Optional.empty();
		}

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

		List<VariableDeclarationExpression> resourcesToRemoveList = Arrays
			.asList(bufferedWriterResourceAnalyzer.getResource(), fileWriterResourceAnalyzer.getResource());
		return Optional.of(new UseFilesWriteStringAnalysisResult(resourcesToRemoveList,
				writeInvocationStatementToReplace, charSequenceArgument, fileIOAnalyzer));
	}
	
	boolean hasTransformationData() {
		return !filesNewBufferedWriterInvocationDataList.isEmpty() || !bufferedWriterInstanceCreationDataList.isEmpty();
	}

	List<FilesNewBufferedIOTransformationData> getFilesNewBufferedWriterInvocationDataList() {
		return filesNewBufferedWriterInvocationDataList;
	}

	List<UseFilesWriteStringAnalysisResult> getBufferedWriterInstanceCreationDataList() {
		return bufferedWriterInstanceCreationDataList;
	}

	List<VariableDeclarationExpression> getResourcesToRemove() {
		List<VariableDeclarationExpression> resourcesToRemove = new ArrayList<>();
		filesNewBufferedWriterInvocationDataList.stream()
			.map(FilesNewBufferedIOTransformationData::getResourceToRemove)
			.forEach(resourcesToRemove::add);

		bufferedWriterInstanceCreationDataList.stream()
			.map(UseFilesWriteStringAnalysisResult::getResourcesToRemove)
			.forEach(resourcesToRemove::addAll);

		return resourcesToRemove;
	}
}
