package eu.jsparrow.rules.java16.switchexpression;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;

/**
 * A visitor 
 * 
 * @since 4.3.0
 *
 */
/**
 * A visitor to collect {@link ReturnStatement}s of a {@link SwitchStatement}.
 * The {@link ReturnStatement}s belonging to embedded local types, anonymous
 * classes and lambda expressions are not collected.
 * 
 * @since 4.3.0
 *
 */
public class SwitchCaseReturnStatementsVisitor extends ASTVisitor {

	private List<ReturnStatement> returnStatements = new ArrayList<>();

	@Override
	public boolean visit(ReturnStatement returnStatement) {
		returnStatements.add(returnStatement);
		return true;
	}

	@Override
	public boolean visit(AnonymousClassDeclaration acd) {
		return false;
	}

	@Override
	public boolean visit(LambdaExpression lambda) {
		return false;
	}

	@Override
	public boolean visit(TypeDeclaration typeDeclaration) {
		return false;
	}

	public boolean hasMultipleReturnStatements() {
		return returnStatements.size() > 1;
	}

	public boolean hasAnyReturnStatement() {
		return !returnStatements.isEmpty();
	}
}
