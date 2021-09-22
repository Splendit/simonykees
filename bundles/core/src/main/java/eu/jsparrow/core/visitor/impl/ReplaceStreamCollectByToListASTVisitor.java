package eu.jsparrow.core.visitor.impl;

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
 * 	.collect(Collectors.toUnmodifiableList());
 * 	.toList();
 * </pre>
 * 
 * @since 4.4.0
 * 
 */
public class ReplaceStreamCollectByToListASTVisitor extends AbstractASTRewriteASTVisitor {

	@Override
	public boolean visit(MethodInvocation invocation) {
		return true;
	}

}