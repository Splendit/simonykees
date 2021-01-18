package eu.jsparrow.core.visitor.files.writestring;

import java.util.ArrayList;
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
			.of(new WriteInvocationData(expressionStatement, charSequenceArgument,
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

		Expression bufferedIOInitializer = writeInvocationData.getResourceInitializer();
		if (bufferedIOInitializer.getNodeType() != ASTNode.METHOD_INVOCATION) {
			return Optional.empty();
		}

		MethodInvocation filesNewBufferedWriterInvocation = (MethodInvocation) bufferedIOInitializer;
		List<Expression> arguments = ASTNodeUtil.convertToTypedList(filesNewBufferedWriterInvocation.arguments(),
				Expression.class);
		
		if (!arguments.isEmpty() && isFilesNewBufferedWriterInvocation(filesNewBufferedWriterInvocation)) {
			Expression pathArgument = arguments.get(0);
			List<Expression> additionalArguments = new ArrayList<>();
			for (int i = 1; i < arguments.size(); i++) {
				additionalArguments.add(arguments.get(i));
			}
			return Optional.of(
					new WriteReplacementUsingFilesNewBufferedWriter(writeInvocationData, pathArgument,
							additionalArguments));
		}
		return Optional.empty();
	}

	private boolean isFilesNewBufferedWriterInvocation(MethodInvocation bufferedIOInitializerMethodInvocation) {

		IMethodBinding methodBinding = bufferedIOInitializerMethodInvocation.resolveMethodBinding();

		if (!ClassRelationUtil.isContentOfType(methodBinding
			.getDeclaringClass(), java.nio.file.Files.class.getName())) {
			return false;
		}
		if (!Modifier.isStatic(methodBinding.getModifiers())) {
			return false;
		}
		if (!methodBinding.getName()
			.equals("newBufferedWriter")) { //$NON-NLS-1$
			return false;
		}
		return checkFilesNewBufferedWriterParameterTypes(methodBinding);
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
		Expression bufferedWriterResourceInitializer = writeInvocationData.getResourceInitializer();
		if (!ClassRelationUtil.isNewInstanceCreationOf(bufferedWriterResourceInitializer,
				java.io.BufferedWriter.class.getName())) {
			return Optional.empty();
		}
		ClassInstanceCreation bufferedWriterInstanceCreation = (ClassInstanceCreation) bufferedWriterResourceInitializer;

		Expression bufferedWriterInstanceCreationArgument = findBufferedIOArgument(bufferedWriterInstanceCreation)
			.orElse(null);

		if (bufferedWriterInstanceCreationArgument != null) {
			if (bufferedWriterInstanceCreationArgument.getNodeType() == ASTNode.CLASS_INSTANCE_CREATION) {
				return findResultUsingWriterInstanceCreation(writeInvocationData,
						(ClassInstanceCreation) bufferedWriterInstanceCreationArgument);

			}
			if (bufferedWriterInstanceCreationArgument.getNodeType() == ASTNode.SIMPLE_NAME) {
				return findResultUsingWriterResource(writeInvocationData,
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
			WriteInvocationData writeInvocationData,
			ClassInstanceCreation writerInstanceCreation) {

		NewBufferedIOArgumentsAnalyzer newBufferedIOArgumentsAnalyzer = new NewBufferedIOArgumentsAnalyzer();
		if (!newBufferedIOArgumentsAnalyzer.analyzeInitializer(writerInstanceCreation)) {
			return Optional.empty();
		}
		return Optional.of(new WriteReplacementUsingBufferedWriterConstructor(writeInvocationData,
				newBufferedIOArgumentsAnalyzer));
	}

	private Optional<WriteReplacementUsingBufferedWriterConstructor> findResultUsingWriterResource(
			WriteInvocationData writeInvocationData,
			SimpleName bufferedIOArgAsSimpleName) {

		TryResourceAnalyzer fileWriterResourceAnalyzer = new TryResourceAnalyzer();

		if (!fileWriterResourceAnalyzer.analyzeResourceUsedOnce(writeInvocationData.getTryStatement(),
				bufferedIOArgAsSimpleName)) {
			return Optional.empty();
		}

		FileIOAnalyzer fileIOAnalyzer = new FileIOAnalyzer(java.io.FileWriter.class.getName());
		if (!fileIOAnalyzer.analyzeFileIO(fileWriterResourceAnalyzer.getResourceFragment())) {
			return Optional.empty();
		}

		return Optional.of(new WriteReplacementUsingBufferedWriterConstructor(writeInvocationData,
				fileWriterResourceAnalyzer, fileIOAnalyzer));
	}

	/**
	 * Acts as a "Record" making possible to return all results of
	 * {@link #findWriteInvocationData}.
	 *
	 */
	class WriteInvocationData {
		private final ExpressionStatement writeInvocationStatementToReplace;
		private final Expression charSequenceArgument;
		private final TryStatement tryStatement;
		private final VariableDeclarationExpression resource;
		private final Expression resourceInitializer;

		private WriteInvocationData(ExpressionStatement writeInvocationStatementToReplace,
				Expression charSequenceArgument,
				TryResourceAnalyzer bufferedWriterResourceAnalyzer) {

			this.writeInvocationStatementToReplace = writeInvocationStatementToReplace;
			this.charSequenceArgument = charSequenceArgument;
			tryStatement = bufferedWriterResourceAnalyzer.getTryStatement();
			resource = bufferedWriterResourceAnalyzer.getResource();
			resourceInitializer = bufferedWriterResourceAnalyzer.getResourceInitializer();
		}

		ExpressionStatement getWriteInvocationStatementToReplace() {
			return writeInvocationStatementToReplace;
		}

		Expression getCharSequenceArgument() {
			return charSequenceArgument;
		}

		public TryStatement getTryStatement() {
			return tryStatement;
		}

		public VariableDeclarationExpression getResource() {
			return resource;
		}

		public Expression getResourceInitializer() {
			return resourceInitializer;
		}
	}
}
