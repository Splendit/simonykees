package eu.jsparrow.core.visitor.impl.trycatch;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

class TwrRemoveCloseASTVisitor extends ASTVisitor {

	/**
	 * 
	 */
	List<MethodInvocation> methodInvocationList;
	ASTMatcher astMatcher;
	ASTRewrite astRewrite;

	public TwrRemoveCloseASTVisitor(ASTRewrite astRewrite, List<MethodInvocation> methodInvocationList) {
		this.astRewrite = astRewrite;
		this.methodInvocationList = methodInvocationList;
		this.astMatcher = new ASTMatcher();
	}

	@Override
	public boolean visit(MethodInvocation node) {
		if (methodInvocationList.stream()
			.anyMatch(methodInvocation -> astMatcher.match(node, methodInvocation)
					&& node.getParent() instanceof Statement)) {
			node.resolveMethodBinding()
				.getExceptionTypes();
			this.astRewrite.remove(node.getParent(), null);
		}
		return false;
	}

}