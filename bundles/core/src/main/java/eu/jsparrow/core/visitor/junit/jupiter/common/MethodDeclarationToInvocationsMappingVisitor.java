package eu.jsparrow.core.visitor.junit.jupiter.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;

/**
 * Creates a mapping of {@link MethodDeclaration} as key and a list of all
 * {@link MethodInvocation}- nodes as value which are carried out in the given
 * {@link MethodDeclaration}.
 * 
 * 
 * @since 3.29.0
 *
 */
public class MethodDeclarationToInvocationsMappingVisitor extends ASTVisitor {

	private final Map<MethodDeclaration, List<MethodInvocation>> methodDeclarationToInvocationsMap = new HashMap<>();

	@Override
	public boolean visit(MethodDeclaration node) {
		MethodInvocationsCollectorVisitor invocationsCollectorVisitor = new MethodInvocationsCollectorVisitor();
		node.accept(invocationsCollectorVisitor);
		methodDeclarationToInvocationsMap.put(node, invocationsCollectorVisitor.getMethodInvocations());
		return true;
	}

	public Map<MethodDeclaration, List<MethodInvocation>> getMethodDeclarationToInvocationsMap() {
		return methodDeclarationToInvocationsMap;
	}
}