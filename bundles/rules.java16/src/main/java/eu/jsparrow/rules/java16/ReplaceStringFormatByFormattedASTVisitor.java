package eu.jsparrow.rules.java16;

import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * This visitor looks for {@link MethodInvocation}-nodes which represent
 * invocations of the method {@link String#format(String, Object...)} and replaces them by
 * invocations of the Java 15 method {@code String#formatted(Object...)}
 * 
 * Example:
 * 
 * <pre>
 * String output = String.format(
 * 		"Name: %s, Phone: %s, Address: %s, Salary: $%.2f",
 * 		name, phone, address, salary);
 * </pre>
 * 
 * is transformed to
 * 
 * <pre>
 * String output = "Name: %s, Phone: %s, Address: %s, Salary: $%.2f"
 * 	.formatted(name, phone, address, salary);
 * </pre>
 * 
 * @since 4.3.0
 * 
 */
public class ReplaceStringFormatByFormattedASTVisitor extends AbstractASTRewriteASTVisitor {

	@Override
	public boolean visit(MethodInvocation invocation) {

		return true;
	}
}