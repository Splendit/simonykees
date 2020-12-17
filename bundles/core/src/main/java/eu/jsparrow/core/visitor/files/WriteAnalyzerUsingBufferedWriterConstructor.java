package eu.jsparrow.core.visitor.files;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.helper.LocalVariableUsagesVisitor;

public class WriteAnalyzerUsingBufferedWriterConstructor {
	
	Optional<UseFilesWriteStringAnalysisResult> findTransformationDataUsingBufferedWriterConstructor(
			ExpressionStatement writeInvocationStatementToReplace, Expression charSequenceArgument,
			TryResourceAnalyzer bufferedWriterResourceAnalyzer) {

		Expression bufferedWriterResourceInitializer = bufferedWriterResourceAnalyzer.getResourceInitializer();
		if (!ClassRelationUtil.isNewInstanceCreationOf(bufferedWriterResourceInitializer,
				java.io.BufferedWriter.class.getName())) {
			return Optional.empty();
		}
		ClassInstanceCreation bufferedWriterInstanceCreation = (ClassInstanceCreation) bufferedWriterResourceInitializer;

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

		TryStatement tryStatement = bufferedWriterResourceAnalyzer.getTryStatement();
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

}
