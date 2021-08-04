package eu.jsparrow.core.visitor.junit.junit3;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;

public class RemoveRunTestInvocationsData {

	private MethodDeclaration mainMethodToRemove;
	private List<ExpressionStatement> expressionStatementsToRemove;
	private List<SimpleName> invocationExpressionsToQualify;

	public RemoveRunTestInvocationsData(MethodDeclaration mainMethodToRemove) {
		this();
		this.mainMethodToRemove = mainMethodToRemove;
	}
	
	public RemoveRunTestInvocationsData() {
		this(Collections.emptyList(), Collections.emptyList());
	}

	public RemoveRunTestInvocationsData(List<ExpressionStatement> expressionStatementsToRemove,
			List<SimpleName> invocationExpressionsToQualify) {
		this.expressionStatementsToRemove = expressionStatementsToRemove;
		this.invocationExpressionsToQualify = invocationExpressionsToQualify;
	}

	public Optional<MethodDeclaration> getMainMethodToRemove() {
		return Optional.ofNullable(mainMethodToRemove);
	}

	public List<ExpressionStatement> getExpressionStatementsToRemove() {
		return expressionStatementsToRemove;
	}

	public List<SimpleName> getInvocationExpressionsToQualify() {
		return invocationExpressionsToQualify;
	}
}
