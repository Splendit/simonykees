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
 * @since 3.27.0
 *
 */
class UseFilesWriteStringTWRStatementAnalyzer {
	private final SignatureData write = new SignatureData(java.io.Writer.class, "write", java.lang.String.class); //$NON-NLS-1$

	List<WriteInvocationData> createTransformationDataList(TryStatement tryStatement) {
		List<WriteInvocationData> invocationDataList = new ArrayList<>();
		List<Statement> tryBodyStatements = ASTNodeUtil.convertToTypedList(tryStatement.getBody()
			.statements(), Statement.class);
		for (Statement statement : tryBodyStatements) {
			findTransformationData(tryStatement, statement).ifPresent(invocationDataList::add);
		}
		return invocationDataList;
	}

	private Optional<WriteInvocationData> findTransformationData(TryStatement tryStatement,
			Statement statement) {
		Optional<WriteInvocationData> resultUsingFilesNewBufferedWriter = findResultUsingFilesNewBufferedWriter(
				tryStatement, statement);
		if (resultUsingFilesNewBufferedWriter.isPresent()) {
			return resultUsingFilesNewBufferedWriter;
		}
		return findResultUsingBufferedWriterConstructor(tryStatement, statement);
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
	 * @return list of {@link WriteInvocationData} objects used for code
	 *         transformation by {@link UseFilesWriteStringASTVisitor}
	 */
	private Optional<WriteInvocationData> findResultUsingFilesNewBufferedWriter(TryStatement tryStatement,
			Statement statement) {

		WriteInvocationData writeInvocationData = findWriteInvocationData(tryStatement, statement).orElse(null);
		if (writeInvocationData == null) {
			return Optional.empty();
		}

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
			writeInvocationData.addAdditionalArguments(additionalArguments);
			writeInvocationData.setReplacementStatementProducer(
					visitor -> visitor.createFilesWriteStringMethodInvocationStatement(writeInvocationData,
							pathArgument));
			return Optional.of(writeInvocationData);
		}
		return Optional.empty();
	}

	private boolean isFilesNewBufferedWriterInvocation(MethodInvocation bufferedIOInitializerMethodInvocation) {

		IMethodBinding methodBinding = bufferedIOInitializerMethodInvocation.resolveMethodBinding();
		if (methodBinding == null) {
			return false;
		}

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
	 * @return list of {@link WriteInvocationData} objects used for code
	 *         transformation by {@link UseFilesWriteStringASTVisitor}
	 */
	private Optional<WriteInvocationData> findResultUsingBufferedWriterConstructor(
			TryStatement tryStatement, Statement statement) {
		WriteInvocationData writeInvocationData = findWriteInvocationData(tryStatement, statement).orElse(null);
		if (writeInvocationData == null) {
			return Optional.empty();
		}
		Expression bufferedWriterResourceInitializer = writeInvocationData.getResourceInitializer();
		if (!ClassRelationUtil.isNewInstanceCreationOf(bufferedWriterResourceInitializer,
				java.io.BufferedWriter.class.getName())) {
			return Optional.empty();
		}
		ClassInstanceCreation bufferedWriterInstanceCreation = (ClassInstanceCreation) bufferedWriterResourceInitializer;

		Expression bufferedWriterInstanceCreationArgument = findBufferedIOArgument(bufferedWriterInstanceCreation)
			.orElse(null);

		if (bufferedWriterInstanceCreationArgument == null) {
			return Optional.empty();
		}

		if (bufferedWriterInstanceCreationArgument.getNodeType() == ASTNode.CLASS_INSTANCE_CREATION) {
			NewBufferedIOArgumentsAnalyzer newBufferedIOArgumentsAnalyzer = new NewBufferedIOArgumentsAnalyzer();
			if (!newBufferedIOArgumentsAnalyzer
				.analyzeInitializer((ClassInstanceCreation) bufferedWriterInstanceCreationArgument)) {
				return Optional.empty();
			}
			newBufferedIOArgumentsAnalyzer.getCharsetExpression()
				.ifPresent(writeInvocationData::setCharsetExpression);
			writeInvocationData.setReplacementStatementProducer(visitor -> visitor
				.createFilesWriteStringMethodInvocationStatement(writeInvocationData,
						newBufferedIOArgumentsAnalyzer.getPathExpressions()));
			return Optional.of(writeInvocationData);

		}

		if (bufferedWriterInstanceCreationArgument.getNodeType() == ASTNode.SIMPLE_NAME) {

			TryResourceAnalyzer fileWriterResourceAnalyzer = new TryResourceAnalyzer();

			if (!fileWriterResourceAnalyzer.analyzeResourceUsedOnce(writeInvocationData.getTryStatement(),
					(SimpleName) bufferedWriterInstanceCreationArgument)) {
				return Optional.empty();
			}
			FileIOAnalyzer fileIOAnalyzer = new FileIOAnalyzer(java.io.FileWriter.class.getName());
			if (!fileIOAnalyzer.analyzeFileIO(fileWriterResourceAnalyzer.getResourceFragment())) {
				return Optional.empty();
			}

			writeInvocationData.addResourcesToRemove(fileWriterResourceAnalyzer.getResource());
			fileIOAnalyzer.getCharset()
				.ifPresent(writeInvocationData::setCharsetExpression);
			writeInvocationData.setReplacementStatementProducer(visitor -> visitor
				.createFilesWriteStringMethodInvocationStatement(writeInvocationData,
						fileIOAnalyzer.getPathExpressions()));
			return Optional.of(writeInvocationData);
		}
		return Optional.empty();
	}

	private Optional<Expression> findBufferedIOArgument(ClassInstanceCreation classInstanceCreation) {

		return ASTNodeUtil
			.findSingletonListElement(classInstanceCreation.arguments(), Expression.class)
			.filter(bufferedIOArg -> ClassRelationUtil.isContentOfType(bufferedIOArg.resolveTypeBinding(),
					java.io.FileWriter.class.getName()));

	}
}
