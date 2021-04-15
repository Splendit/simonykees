package eu.jsparrow.core.visitor.junit.jupiter.common;

import java.util.ArrayList;
import java.util.List;

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
public class MethodDeclarationsCollectorVisitor extends ASTVisitor {

	private final List<MethodDeclaration> methodDeclarations = new ArrayList<>();

	@Override
	public boolean visit(MethodDeclaration node) {
		methodDeclarations.add(node);
		return true;
	}

	public List<MethodDeclaration> getMethodDeclarations() {
		return methodDeclarations;
	}
}