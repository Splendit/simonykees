package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodInvocation;

public class MethodInvocationsCollectorVisitor extends ASTVisitor {

	private final List<MethodInvocation> methodInvocations = new ArrayList<>();

	@Override
	public boolean visit(MethodInvocation node) {
		methodInvocations.add(node);
		return true;
	}

	public List<MethodInvocation> getMethodInvocations() {
		return methodInvocations;
	}

}