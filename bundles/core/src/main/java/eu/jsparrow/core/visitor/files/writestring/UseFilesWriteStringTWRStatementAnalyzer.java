package eu.jsparrow.core.visitor.files.writestring;

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

import eu.jsparrow.core.visitor.files.FileIOAnalyzer;
import eu.jsparrow.core.visitor.files.NewBufferedIOArgumentsAnalyzer;
import eu.jsparrow.core.visitor.files.TryResourceAnalyzer;
import eu.jsparrow.core.visitor.sub.SignatureData;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;

/**
 * Helper class used exclusively by
 * {@link eu.jsparrow.core.visitor.files.writestring.UseFilesWriteStringASTVisitor}
 * for providing transformation data. It looks for
 * {@link org.eclipse.jdt.core.dom.ExpressionStatement}-nodes which are child
 * nodes of the TWR-body of a given
 * {@link org.eclipse.jdt.core.dom.TryStatement} and call the method
 * {@link java.io.Writer#write(String)} on a resource variable declared in the
 * TWR-header.
 * 
 * 
 * @since 3.25.0
 *
 */
class UseFilesWriteStringTWRStatementAnalyzer {
	private final SignatureData write = new SignatureData(java.io.Writer.class, "write", java.lang.String.class); //$NON-NLS-1$

	List<WriteInvocationData> createWriteInvocationDataList(TryStatement tryStatement) {
		List<WriteInvocationData> invocationDataList = new ArrayList<>();
		List<Statement> tryBodyStatements = ASTNodeUtil.convertToTypedList(tryStatement.getBody()
			.statements(), Statement.class);
		for (Statement statement : tryBodyStatements) {
			findWriteInvocationData(tryStatement, statement).ifPresent(invocationDataList::add);
		}
		return invocationDataList;
	}

	private Optional<WriteInvocationData> findWriteInvocationData(TryStatement tryStatement, Statement statement) {
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
		TryResourceAnalyzer bufferedWriterResourceAnalyzer = new TryResourceAnalyzer();
		if (!bufferedWriterResourceAnalyzer.analyzeResourceUsedOnce(tryStatement, writerVariableSimpleName)) {
			return Optional.empty();
		}

		return Optional
			.of(new WriteInvocationData(expressionStatement, writerVariableSimpleName, charSequenceArgument,
					bufferedWriterResourceAnalyzer));
	}

	/**
	 * Collects data in connection with a resource initialized by calling
	 * {@link java.nio.file.Files#newBufferedWriter(java.nio.file.Path, java.nio.charset.Charset, java.nio.file.OpenOption...)}
	 * or
	 * {@link java.nio.file.Files#newBufferedWriter(java.nio.file.Path, java.nio.file.OpenOption...)}.
	 * 
	 * @return list of {@link WriteReplacementUsingFilesNewBufferedWriter}
	 *         objects used for code transformation by
	 *         {@link UseFilesWriteStringASTVisitor}
	 */
	Optional<WriteReplacementUsingFilesNewBufferedWriter> findResultUsingFilesNewBufferedWriter(
			WriteInvocationData writeInvocationData) {

		TryResourceAnalyzer bufferedWriterResourceAnalyzer = writeInvocationData.getBufferedWriterResourceAnalyzer();

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

		argumentsToCopy.add(1, writeInvocationData.getCharSequenceArgument());
		return Optional.of(
				new WriteReplacementUsingFilesNewBufferedWriter(resourceToRemove,
						writeInvocationData.getWriteInvocationStatementToReplace(), argumentsToCopy));
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

	/**
	 * Collects data in connection with a resource initialized by calling a
	 * constructor of {@link java.io.BufferedWriter}.
	 * 
	 * @return list of {@link WriteReplacementUsingBufferedWriterConstructor}
	 *         objects used for code transformation by
	 *         {@link UseFilesWriteStringASTVisitor}
	 */
	Optional<WriteReplacementUsingBufferedWriterConstructor> findResultUsingBufferedWriterConstructor(
			WriteInvocationData writeInvocationData) {
		TryResourceAnalyzer bufferedWriterResourceAnalyzer = writeInvocationData.getBufferedWriterResourceAnalyzer();
		Expression bufferedWriterResourceInitializer = bufferedWriterResourceAnalyzer.getResourceInitializer();
		if (!ClassRelationUtil.isNewInstanceCreationOf(bufferedWriterResourceInitializer,
				java.io.BufferedWriter.class.getName())) {
			return Optional.empty();
		}
		ClassInstanceCreation bufferedWriterInstanceCreation = (ClassInstanceCreation) bufferedWriterResourceInitializer;

		Expression bufferedWriterInstanceCreationArgument = findBufferedIOArgument(bufferedWriterInstanceCreation)
			.orElse(null);

		Expression charSequenceArgument = writeInvocationData.getCharSequenceArgument();
		ExpressionStatement writeInvocationStatementToReplace = writeInvocationData
			.getWriteInvocationStatementToReplace();
		if (bufferedWriterInstanceCreationArgument != null) {
			if (bufferedWriterInstanceCreationArgument.getNodeType() == ASTNode.CLASS_INSTANCE_CREATION) {
				return findResultUsingWriterInstanceCreation(writeInvocationStatementToReplace,
						charSequenceArgument, bufferedWriterResourceAnalyzer,
						(ClassInstanceCreation) bufferedWriterInstanceCreationArgument);

			}
			if (bufferedWriterInstanceCreationArgument.getNodeType() == ASTNode.SIMPLE_NAME) {
				return findResultUsingWriterResource(writeInvocationStatementToReplace,
						charSequenceArgument, bufferedWriterResourceAnalyzer,
						(SimpleName) bufferedWriterInstanceCreationArgument);
			}
		}
		return Optional.empty();
	}

	static Optional<Expression> findBufferedIOArgument(ClassInstanceCreation classInstanceCreation) {

		List<Expression> newBufferedIOArgs = ASTNodeUtil.convertToTypedList(classInstanceCreation.arguments(),
				Expression.class);
		if (newBufferedIOArgs.size() != 1) {
			return Optional.empty();
		}
		Expression bufferedIOArg = newBufferedIOArgs.get(0);
		ITypeBinding firstArgType = bufferedIOArg.resolveTypeBinding();
		if (!ClassRelationUtil.isContentOfType(firstArgType, java.io.FileWriter.class.getName())) {
			return Optional.empty();
		}
		return Optional.of(bufferedIOArg);
	}

	private Optional<WriteReplacementUsingBufferedWriterConstructor> findResultUsingWriterInstanceCreation(
			ExpressionStatement writeInvocationStatementToReplace, Expression charSequenceArgument,
			TryResourceAnalyzer bufferedWriterResourceAnalyzer,
			ClassInstanceCreation writerInstanceCreation) {
		NewBufferedIOArgumentsAnalyzer newBufferedIOArgumentsAnalyzer = new NewBufferedIOArgumentsAnalyzer();
		if (!newBufferedIOArgumentsAnalyzer.analyzeInitializer(writerInstanceCreation)) {
			return Optional.empty();
		}

		return Optional.of(new WriteReplacementUsingBufferedWriterConstructor(
				Arrays.asList(bufferedWriterResourceAnalyzer.getResource()), writeInvocationStatementToReplace,
				charSequenceArgument, newBufferedIOArgumentsAnalyzer));
	}

	private Optional<WriteReplacementUsingBufferedWriterConstructor> findResultUsingWriterResource(
			ExpressionStatement writeInvocationStatementToReplace, Expression charSequenceArgument,
			TryResourceAnalyzer bufferedWriterResourceAnalyzer,
			SimpleName bufferedIOArgAsSimpleName) {
		TryResourceAnalyzer fileWriterResourceAnalyzer = new TryResourceAnalyzer();

		if (!fileWriterResourceAnalyzer.analyzeResourceUsedOnce(bufferedWriterResourceAnalyzer.getTryStatement(),
				bufferedIOArgAsSimpleName)) {
			return Optional.empty();
		}

		FileIOAnalyzer fileIOAnalyzer = new FileIOAnalyzer(java.io.FileWriter.class.getName());
		if (!fileIOAnalyzer.analyzeFileIO(fileWriterResourceAnalyzer.getResourceFragment())) {
			return Optional.empty();
		}

		List<VariableDeclarationExpression> resourcesToRemoveList = Arrays
			.asList(bufferedWriterResourceAnalyzer.getResource(), fileWriterResourceAnalyzer.getResource());
		return Optional.of(new WriteReplacementUsingBufferedWriterConstructor(resourcesToRemoveList,
				writeInvocationStatementToReplace, charSequenceArgument, fileIOAnalyzer));
	}

	/**
	 * Acts as a "Record" making possible to return all results of
	 * {@link #findWriteInvocationData}.
	 *
	 */
	class WriteInvocationData {
		private final ExpressionStatement writeInvocationStatementToReplace;
		private final SimpleName writerVariableSimpleName;
		private final Expression charSequenceArgument;
		private final TryResourceAnalyzer bufferedWriterResourceAnalyzer;

		private WriteInvocationData(ExpressionStatement writeInvocationStatementToReplace,
				SimpleName writerVariableSimpleName, Expression charSequenceArgument,
				TryResourceAnalyzer bufferedWriterResourceAnalyzer) {

			this.writeInvocationStatementToReplace = writeInvocationStatementToReplace;
			this.writerVariableSimpleName = writerVariableSimpleName;
			this.charSequenceArgument = charSequenceArgument;
			this.bufferedWriterResourceAnalyzer = bufferedWriterResourceAnalyzer;
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

		TryResourceAnalyzer getBufferedWriterResourceAnalyzer() {
			return bufferedWriterResourceAnalyzer;
		}

	}
}
