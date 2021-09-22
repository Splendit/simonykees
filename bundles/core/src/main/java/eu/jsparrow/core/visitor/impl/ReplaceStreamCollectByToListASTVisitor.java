package eu.jsparrow.core.visitor.impl;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * This visitor looks for {@link MethodInvocation} nodes which represent
 * invocations of the method {@code Stream#collect(Collector)} and replaces them
 * by invocations of the Java 16 method {@code Stream#toList()}
 * <p>
 * Example:
 * 
 * <pre>
 * collection
 * 	.stream()
 * 	.map(function)
 * 	.filter(predicate)
 * 	.collect(Collectors.toUnmodifiableList());
 * </pre>
 * 
 * is transformed to
 * 
 * <pre>
 * collection
 * 	.stream()
 * 	.map(function)
 * 	.filter(predicate)
 * 	.toList();
 * </pre>
 * 
 * @since 4.4.0
 * 
 */
public class ReplaceStreamCollectByToListASTVisitor extends AbstractASTRewriteASTVisitor {

	/**
	 * This is a prototype with almost no validation. The only condition of
	 * validation is that the method invocation must have the method name "collect".
	 */
	@Override
	public boolean visit(MethodInvocation invocation) {
		if (isValidStreamCollectInvocation(invocation)) {
			Expression streamExpression = (Expression) astRewrite.createCopyTarget(invocation.getExpression());
			AST ast = astRewrite.getAST();
			MethodInvocation streamToListInvocation = ast.newMethodInvocation();
			streamToListInvocation.setName(ast.newSimpleName("toList")); //$NON-NLS-1$
			streamToListInvocation.setExpression(streamExpression);
			astRewrite.replace(invocation, streamToListInvocation, null);
			return false;
		}
		return true;
	}

	private boolean isValidStreamCollectInvocation(MethodInvocation invocation) {
		if (!"collect".equals(invocation.getName() //$NON-NLS-1$
			.getIdentifier())) {
			return false;
		}
		return true;
	}

}