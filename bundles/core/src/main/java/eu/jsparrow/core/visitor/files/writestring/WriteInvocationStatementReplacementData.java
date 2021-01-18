package eu.jsparrow.core.visitor.files.writestring;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;

import eu.jsparrow.core.visitor.files.FileIOAnalyzer;
import eu.jsparrow.core.visitor.files.NewBufferedIOArgumentsAnalyzer;
import eu.jsparrow.core.visitor.files.TryResourceAnalyzer;

/**
 * Stores all informations in connection with the replacement of invocation
 * statements calling {@link java.io.Writer#write(String)} on a resource which
 * is initialized by the invocation of a constructor of
 * {@link java.io.BufferedWriter}.
 * 
 * 
 * @since 3.24.0
 *
 */
class WriteInvocationStatementReplacementData {
	private final List<VariableDeclarationExpression> resourcesToRemove;
	private final ExpressionStatement writeInvocationStatementToReplace;
	private final Function<UseFilesWriteStringASTVisitor, ExpressionStatement> functionCreatingExpressionStatementReplacement;

	WriteInvocationStatementReplacementData(
			WriteInvocationData writeInvocationData,
			Expression pathArgument,
			List<Expression> additionalArguments

	) {
		this.resourcesToRemove = Arrays.asList(writeInvocationData.getResource());
		this.writeInvocationStatementToReplace = writeInvocationData.getWriteInvocationStatementToReplace();
		this.functionCreatingExpressionStatementReplacement = visitor -> visitor
			.createFilesWriteStringMethodInvocationStatement(writeInvocationData, pathArgument, additionalArguments);
	}

	WriteInvocationStatementReplacementData(WriteInvocationData writeInvocationData,
			NewBufferedIOArgumentsAnalyzer newBufferedIOArgumentsAnalyzer) {
		this.resourcesToRemove = Arrays.asList(writeInvocationData.getResource());
		this.writeInvocationStatementToReplace = writeInvocationData.getWriteInvocationStatementToReplace();

		List<Expression> pathExpressions = newBufferedIOArgumentsAnalyzer.getPathExpressions();
		Supplier<Optional<Expression>> charSetExpressionSupplier = newBufferedIOArgumentsAnalyzer::getCharsetExpression;
		this.functionCreatingExpressionStatementReplacement = visitor -> visitor
			.createFilesWriteStringMethodInvocationStatement(writeInvocationData, pathExpressions,
					charSetExpressionSupplier);
	}

	WriteInvocationStatementReplacementData(WriteInvocationData writeInvocationData,
			TryResourceAnalyzer fileWriterResourceAnalyzer, FileIOAnalyzer fileIOAnalyzer) {
		this.resourcesToRemove = Arrays
			.asList(writeInvocationData.getResource(), fileWriterResourceAnalyzer.getResource());
		this.writeInvocationStatementToReplace = writeInvocationData.getWriteInvocationStatementToReplace();

		List<Expression> pathExpressions = fileIOAnalyzer.getPathExpressions();
		Supplier<Optional<Expression>> charSetExpressionSupplier = fileIOAnalyzer::getCharset;
		this.functionCreatingExpressionStatementReplacement = visitor -> visitor
			.createFilesWriteStringMethodInvocationStatement(writeInvocationData, pathExpressions,
					charSetExpressionSupplier);
	}

	List<VariableDeclarationExpression> getResourcesToRemove() {
		return resourcesToRemove;
	}

	ExpressionStatement getWriteInvocationStatementToReplace() {
		return writeInvocationStatementToReplace;
	}

	ExpressionStatement createWriteInvocationStatementReplacement(UseFilesWriteStringASTVisitor visitor) {
		return functionCreatingExpressionStatementReplacement.apply(visitor);
	}
}