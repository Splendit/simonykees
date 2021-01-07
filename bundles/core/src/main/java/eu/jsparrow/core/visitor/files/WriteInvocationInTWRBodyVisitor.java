package eu.jsparrow.core.visitor.files;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import eu.jsparrow.core.visitor.sub.SignatureData;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.helper.LocalVariableUsagesVisitor;

/**
 * This helper class analyzes a {@link org.eclipse.jdt.core.dom.TryStatement} by
 * looking for child expression statements which contain an invocation of
 * {@link java.io.Writer#write(String)} and can be replaced by an invocation of
 * one of the methods which have the name "writeString" and are available as
 * static methods of the class {@link java.nio.file.Files} since Java 11.<br>
 * 
 * 
 * @since 3.25.0
 *
 */
public class WriteInvocationInTWRBodyVisitor {
	private final SignatureData write = new SignatureData(java.io.Writer.class, "write", java.lang.String.class); //$NON-NLS-1$
	private final TryStatement tryStatement;
	private final List<TransformationDataUsingFilesNewBufferedWriter> filesNewBufferedWriterInvocationDataList = new ArrayList<>();
	private final List<TransformationDataUsingBufferedWriterConstructor> bufferedWriterInstanceCreationDataList = new ArrayList<>();

	WriteInvocationInTWRBodyVisitor(TryStatement tryStatement) {
		this.tryStatement = tryStatement;
	}

	boolean analyze() {
		List<Statement> tryBodyStatements = ASTNodeUtil.convertToTypedList(tryStatement.getBody()
			.statements(), Statement.class);
		for (Statement statement : tryBodyStatements) {
			WriteInvocationData writeInvocationData = findWriteInvocationData(statement).orElse(null);
			if (writeInvocationData != null) {
				TryResourceAnalyzer bufferedWriterResourceAnalyzer = new TryResourceAnalyzer();
				SimpleName writerVariableSimpleName = writeInvocationData.getWriterVariableSimpleName();
				if (bufferedWriterResourceAnalyzer.analyzeCheckingUsage(tryStatement, writerVariableSimpleName,
						writerVariableSimpleName)) {

					TransformationDataUsingFilesNewBufferedWriter dataUsingFilesNewBufferedWriter = findTransformationDataUsingFilesNewBufferedWriter(
							writeInvocationData.getWriteInvocationStatementToReplace(),
							writeInvocationData.getCharSequenceArgument(), bufferedWriterResourceAnalyzer)
								.orElse(null);
					if (dataUsingFilesNewBufferedWriter != null) {
						filesNewBufferedWriterInvocationDataList.add(dataUsingFilesNewBufferedWriter);

					} else {
						findTransformationDataUsingBufferedWriterConstructor(
									writeInvocationData.getWriteInvocationStatementToReplace(),
									writeInvocationData.getCharSequenceArgument(), bufferedWriterResourceAnalyzer)
							.ifPresent(bufferedWriterInstanceCreationDataList::add);
					}
				}
			}
		}
		return !filesNewBufferedWriterInvocationDataList.isEmpty() || !bufferedWriterInstanceCreationDataList.isEmpty();
	}

	private Optional<WriteInvocationData> findWriteInvocationData(Statement statement) {
		if (statement.getNodeType() != ASTNode.EXPRESSION_STATEMENT) {
			return Optional.empty();
		}
		ExpressionStatement expressionStatement = (ExpressionStatement) statement;
		Expression expression = expressionStatement.getExpression();
		if (expression.getNodeType() != ASTNode.METHOD_INVOCATION) {
			return Optional.empty();
		}
		MethodInvocation methodInvocation = (MethodInvocation) expression;
		if (!write.isEquivalentTo(methodInvocation.resolveMethodBinding())) {
			return Optional.empty();
		}
		Expression charSequenceArgument = ASTNodeUtil
			.convertToTypedList(methodInvocation.arguments(), Expression.class)
			.get(0);
		Expression methodInvocationExpression = methodInvocation.getExpression();

		if (methodInvocationExpression == null || methodInvocationExpression.getNodeType() != ASTNode.SIMPLE_NAME) {
			return Optional.empty();
		}
		SimpleName writerVariableSimpleName = (SimpleName) methodInvocationExpression;
		return Optional
			.of(new WriteInvocationData(expressionStatement, writerVariableSimpleName, charSequenceArgument));
	}

	private Optional<TransformationDataUsingFilesNewBufferedWriter> findTransformationDataUsingFilesNewBufferedWriter(
			ExpressionStatement writeInvocationStatementToReplace, Expression charSequenceArgument,
			TryResourceAnalyzer bufferedWriterResourceAnalyzer) {

		Expression bufferedIOInitializer = bufferedWriterResourceAnalyzer.getResourceInitializer();
		if (bufferedIOInitializer.getNodeType() != ASTNode.METHOD_INVOCATION) {
			return Optional.empty();
		}
		MethodInvocation bufferedIOInitializerMethodInvocation = (MethodInvocation) bufferedIOInitializer;
		VariableDeclarationExpression resourceToRemove = bufferedWriterResourceAnalyzer.getResource();

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
				new TransformationDataUsingFilesNewBufferedWriter(resourceToRemove,
						writeInvocationStatementToReplace, argumentsToCopy));

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
	
	private Optional<TransformationDataUsingBufferedWriterConstructor> findTransformationDataUsingBufferedWriterConstructor(
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
	
	private Optional<TransformationDataUsingBufferedWriterConstructor> findTransformationDataUsingWriterInstanceCreation(
			ExpressionStatement writeInvocationStatementToReplace, Expression charSequenceArgument,
			TryResourceAnalyzer bufferedWriterResourceAnalyzer,
			ClassInstanceCreation writerInstanceCreation) {
		NewBufferedIOArgumentsAnalyzer newBufferedIOArgumentsAnalyzer = new NewBufferedIOArgumentsAnalyzer();
		if (!newBufferedIOArgumentsAnalyzer.analyzeInitializer(writerInstanceCreation)) {
			return Optional.empty();
		}

		return Optional.of(new TransformationDataUsingBufferedWriterConstructor(
				Arrays.asList(bufferedWriterResourceAnalyzer.getResource()), writeInvocationStatementToReplace,
				charSequenceArgument, newBufferedIOArgumentsAnalyzer));
	}
	
	private Optional<TransformationDataUsingBufferedWriterConstructor> findTransformationDataUsingWriterResource(
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
		return Optional.of(new TransformationDataUsingBufferedWriterConstructor(resourcesToRemoveList,
				writeInvocationStatementToReplace, charSequenceArgument, fileIOAnalyzer));
	}


	List<TransformationDataUsingFilesNewBufferedWriter> getFilesNewBufferedWriterInvocationDataList() {
		return filesNewBufferedWriterInvocationDataList;
	}

	List<TransformationDataUsingBufferedWriterConstructor> getBufferedWriterInstanceCreationDataList() {
		return bufferedWriterInstanceCreationDataList;
	}

	List<VariableDeclarationExpression> getResourcesToRemove() {
		List<VariableDeclarationExpression> resourcesToRemove = new ArrayList<>();
		filesNewBufferedWriterInvocationDataList.stream()
			.map(TransformationDataUsingFilesNewBufferedWriter::getResourceToRemove)
			.forEach(resourcesToRemove::add);

		bufferedWriterInstanceCreationDataList.stream()
			.map(TransformationDataUsingBufferedWriterConstructor::getResourcesToRemove)
			.forEach(resourcesToRemove::addAll);

		return resourcesToRemove;
	}

	private class WriteInvocationData {
		private final ExpressionStatement writeInvocationStatementToReplace;
		private final SimpleName writerVariableSimpleName;
		private final Expression charSequenceArgument;

		public WriteInvocationData(ExpressionStatement writeInvocationStatementToReplace,
				SimpleName writerVariableSimpleName, Expression charSequenceArgument) {
			super();
			this.writeInvocationStatementToReplace = writeInvocationStatementToReplace;
			this.writerVariableSimpleName = writerVariableSimpleName;
			this.charSequenceArgument = charSequenceArgument;
		}

		ExpressionStatement getWriteInvocationStatementToReplace() {
			return writeInvocationStatementToReplace;
		}

		SimpleName getWriterVariableSimpleName() {
			return writerVariableSimpleName;
		}

		Expression getCharSequenceArgument() {
			return charSequenceArgument;
		}

	}
}
