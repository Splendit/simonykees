package eu.jsparrow.core.visitor.impl.trycatch;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;

class TwrCloseStatementsVisitor extends ASTVisitor {

	private List<MethodInvocation> methodInvocationList;
	private ASTMatcher astMatcher;
	private List<Statement> closeInvocations;

	public TwrCloseStatementsVisitor(List<MethodInvocation> methodInvocationList) {
		this.methodInvocationList = methodInvocationList;
		this.astMatcher = new ASTMatcher();
		this.closeInvocations = new ArrayList<>();
	}

	@Override
	public boolean visit(MethodInvocation node) {
		if (methodInvocationList.stream()
			.anyMatch(methodInvocation -> astMatcher.match(node, methodInvocation)
					&& node.getParent() instanceof Statement)) {
			closeInvocations.add((Statement)node.getParent());
		}
		return false;
	}
	
	public List<Statement> getCloseInvocationStatements() {
		return this.closeInvocations;
	}

}