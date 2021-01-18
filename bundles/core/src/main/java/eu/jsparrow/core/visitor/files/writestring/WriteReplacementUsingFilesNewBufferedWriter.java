package eu.jsparrow.core.visitor.files.writestring;

import java.util.List;
import java.util.function.Function;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;

import eu.jsparrow.core.visitor.files.writestring.UseFilesWriteStringTWRStatementAnalyzer.WriteInvocationData;

/**
 * Stores all informations in connection with the replacement of invocation
 * statements calling {@link java.io.Writer#write(String)} on a resource which
 * is initialized by <br>
 * {@link java.nio.file.Files#newBufferedWriter(java.nio.file.Path, java.nio.charset.Charset, java.nio.file.OpenOption...)}
 * or <br>
 * {@link java.nio.file.Files#newBufferedWriter(java.nio.file.Path, java.nio.file.OpenOption...)}.
 * 
 * @since 3.24.0
 * 
 */
class WriteReplacementUsingFilesNewBufferedWriter {
	private final VariableDeclarationExpression resourceToRemove;
	private final ExpressionStatement writeInvocationStatementToReplace;
	private final Function<UseFilesWriteStringASTVisitor, ExpressionStatement> functionCreatingExpressionStatementReplacement;

	WriteReplacementUsingFilesNewBufferedWriter(
			WriteInvocationData writeInvocationData,
			Expression pathArgument,
			List<Expression> additionalArguments

	) {
		this.resourceToRemove = writeInvocationData.getResource();
		this.writeInvocationStatementToReplace = writeInvocationData.getWriteInvocationStatementToReplace();
		this.functionCreatingExpressionStatementReplacement = visitor -> visitor
			.createFilesWriteStringMethodInvocationStatement(writeInvocationData, pathArgument, additionalArguments);
	}

	VariableDeclarationExpression getResourceToRemove() {
		return resourceToRemove;
	}

	ExpressionStatement getWriteInvocationStatementToReplace() {
		return writeInvocationStatementToReplace;
	}

	ExpressionStatement createWriteInvocationStatementReplacement(UseFilesWriteStringASTVisitor visitor) {
		return functionCreatingExpressionStatementReplacement.apply(visitor);
	}

}
