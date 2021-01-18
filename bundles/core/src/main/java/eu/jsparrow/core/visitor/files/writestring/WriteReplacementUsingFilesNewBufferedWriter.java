package eu.jsparrow.core.visitor.files.writestring;

import java.util.List;

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
	private final Expression charSequenceArgument;
	private final Expression pathArgument;
	private final List<Expression> additionalArguments;

	WriteReplacementUsingFilesNewBufferedWriter(
			WriteInvocationData writeInvocationData,
			Expression pathArgument,
			List<Expression> additionalArguments

	) {
		this.resourceToRemove = writeInvocationData.getResource();
		this.writeInvocationStatementToReplace = writeInvocationData.getWriteInvocationStatementToReplace();
		this.charSequenceArgument = writeInvocationData.getCharSequenceArgument();
		this.pathArgument = pathArgument;
		this.additionalArguments = additionalArguments;
	}

	VariableDeclarationExpression getResourceToRemove() {
		return resourceToRemove;
	}

	ExpressionStatement getWriteInvocationStatementToReplace() {
		return writeInvocationStatementToReplace;
	}

	Expression getPathArgument() {
		return pathArgument;
	}

	Expression getCharSequenceArgument() {
		return charSequenceArgument;
	}

	/**
	 * @return a {@link List} representing all optional resource arguments
	 *         following {@link java.nio.file.Path} - argument:<br>
	 *         <ul>
	 *         <li>an optional argument for
	 *         {@link java.nio.charset.Charset}</li>
	 *         <li>subsequent optional {@link java.nio.file.OpenOption}
	 *         arguments</li>
	 *         </ul>
	 */
	List<Expression> getAdditionalArguments() {
		return additionalArguments;
	}
}
