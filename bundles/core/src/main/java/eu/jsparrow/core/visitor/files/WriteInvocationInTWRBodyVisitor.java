package eu.jsparrow.core.visitor.files;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.helper.LocalVariableUsagesVisitor;

/**
 * This visitor is a helper visitor intended to be used for visiting
 * {@link org.eclipse.jdt.core.dom.TryStatement}-instances.<br>
 * It looks for invocations of {@link java.io.Writer#write(String)} which can be
 * replaced by invocations of methods which have the name "writeString" and are
 * available as static methods of the class {@link java.nio.file.Files} since
 * Java 11.<br>
 * Additionally, this visitor stores all data which are necessary for the
 * corresponding code transformation. <br>
 * 
 * 
 * @since 3.25.0
 *
 */
public class WriteInvocationInTWRBodyVisitor extends ASTVisitor {

	private final TryStatement tryStatement;
	private final List<FilesNewBufferedIOTransformationData> filesNewBufferedWriterInvocationDataList = new ArrayList<>();
	private final List<UseFilesWriteStringAnalysisResult> bufferedWriterInstanceCreationDataList = new ArrayList<>();

	public WriteInvocationInTWRBodyVisitor(TryStatement tryStatement) {
		this.tryStatement = tryStatement;
	}

	@Override
	public boolean visit(MethodInvocation methodInvocation) {

		WriteMethodInvocationAnalyzer writeInvocationAnalyzer = new WriteMethodInvocationAnalyzer();
		if (!writeInvocationAnalyzer.analyze(methodInvocation)) {
			return true;
		}

		if (writeInvocationAnalyzer.getBlockOfInvocationStatement()
			.getParent() != tryStatement) {
			return true;
		}

		TryResourceAnalyzer bufferedWriterResourceAnalyzer = new TryResourceAnalyzer();
		if (!bufferedWriterResourceAnalyzer.analyze(tryStatement,
				writeInvocationAnalyzer.getWriterVariableSimpleName())) {
			return true;
		}

		if (!checkWriterVariableUsage(writeInvocationAnalyzer.getWriterVariableSimpleName(),
				writeInvocationAnalyzer.getBlockOfInvocationStatement())) {
			return true;
		}
		FilesNewBufferedIOTransformationData filesNewBufferedIOTransformationData = findFilesNewBufferedIOTransformationData(
				bufferedWriterResourceAnalyzer, writeInvocationAnalyzer).orElse(null);
		if (filesNewBufferedIOTransformationData != null) {
			filesNewBufferedWriterInvocationDataList.add(filesNewBufferedIOTransformationData);
		} else {
			findResultByBufferedWriterInstanceCreation(bufferedWriterResourceAnalyzer, writeInvocationAnalyzer)
				.ifPresent(bufferedWriterInstanceCreationDataList::add);
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

		LocalVariableUsagesVisitor visitor = new LocalVariableUsagesVisitor(fileWriterResourceFragment.getName());
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
		LocalVariableUsagesVisitor visitor = new LocalVariableUsagesVisitor(
				writerVariableName);
		blockOfInvocationStatement.accept(visitor);
		int usages = visitor.getUsages()
			.size();
		return usages == 1;
	}

	List<FilesNewBufferedIOTransformationData> getFilesNewBufferedWriterInvocationDataList() {
		return filesNewBufferedWriterInvocationDataList;
	}

	List<UseFilesWriteStringAnalysisResult> getBufferedWriterInstanceCreationDataList() {
		return bufferedWriterInstanceCreationDataList;
	}
}
