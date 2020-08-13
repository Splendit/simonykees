package eu.jsparrow.core.visitor.impl.indexof;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

public class UseOffsetBasedStringMethodsASTVisitor extends AbstractASTRewriteASTVisitor {

	@Override
	public boolean visit(MethodInvocation node) {
		SubstringIndexOfInvocationChainAnalyzer analyzer = new SubstringIndexOfInvocationChainAnalyzer();
		if (analyzer.analyze(node)) {
			transform(analyzer, node);
		}
		return true;
	}

	private void transform(SubstringIndexOfInvocationChainAnalyzer analyzer, MethodInvocation methodInvocation) {
		ASTNode offsetArgument = astRewrite.createCopyTarget(analyzer.getOffsetArgument());
		astRewrite.getListRewrite(methodInvocation, MethodInvocation.ARGUMENTS_PROPERTY)
			.insertLast(offsetArgument, null);

		ASTNode stringExpression = astRewrite.createCopyTarget(analyzer.getStringExpression());
		astRewrite.replace(methodInvocation.getExpression(), stringExpression, null);
		onRewrite();
	}
}
