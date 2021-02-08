package eu.jsparrow.core.visitor.files.writestring;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;

import eu.jsparrow.core.visitor.files.TryResourceAnalyzer;

/**
 * Acts as a "Record" making possible to return all results of
 * {@link #findWriteInvocationData}.
 * 
 * @since 3.27.0
 *
 */
class WriteInvocationData {

	private final ExpressionStatement writeInvocationStatementToReplace;
	private final Expression charSequenceArgument;
	private final TryStatement tryStatement;
	private final Expression resourceInitializer;
	private List<VariableDeclarationExpression> resourcesToRemove = new ArrayList<>();
	private Function<UseFilesWriteStringASTVisitor, ExpressionStatement> replacementStatementProducer;

	private Expression charsetExpression;
	private List<Expression> additionalArguments = new ArrayList<>();

	WriteInvocationData(ExpressionStatement writeInvocationStatementToReplace,
			Expression charSequenceArgument,
			TryResourceAnalyzer bufferedWriterResourceAnalyzer) {

		this.writeInvocationStatementToReplace = writeInvocationStatementToReplace;
		this.charSequenceArgument = charSequenceArgument;
		tryStatement = bufferedWriterResourceAnalyzer.getTryStatement();
		addResourcesToRemove(bufferedWriterResourceAnalyzer.getResource());
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

	public Expression getResourceInitializer() {
		return resourceInitializer;
	}

	public void addResourcesToRemove(VariableDeclarationExpression variableDeclarationExpression) {
		this.resourcesToRemove.add(variableDeclarationExpression);
	}

	public List<VariableDeclarationExpression> getResourcesToRemove() {
		return this.resourcesToRemove;
	}

	public Optional<Expression> getCharsetExpression() {
		return Optional.ofNullable(charsetExpression);
	}

	public void setCharsetExpression(Expression charsetExpression) {
		this.charsetExpression = charsetExpression;
	}

	public List<Expression> getAdditionalArguments() {
		return this.additionalArguments;
	}

	public void addAdditionalArguments(List<Expression> additionalArguments) {
		this.additionalArguments.addAll(additionalArguments);
	}

	public void setReplacementStatementProducer(
			Function<UseFilesWriteStringASTVisitor, ExpressionStatement> replacementStatementProducer) {
		this.replacementStatementProducer = replacementStatementProducer;
	}

	ExpressionStatement createWriteInvocationStatementReplacement(UseFilesWriteStringASTVisitor visitor) {
		return replacementStatementProducer.apply(visitor);
	}
}