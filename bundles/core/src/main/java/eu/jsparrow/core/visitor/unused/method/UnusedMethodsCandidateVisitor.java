package eu.jsparrow.core.visitor.unused.method;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;

public class UnusedMethodsCandidateVisitor extends ASTVisitor {

	private Map<String, Boolean> options;
	private List<UnusedMethodWrapper> unusedPrivateMethods = new ArrayList<>();
	private List<UnusedMethodsCandidateVisitor> nonPrivateCandidates = new ArrayList<>();

	public UnusedMethodsCandidateVisitor(Map<String, Boolean> options) {
		this.options = options;
	}

	@Override
	public boolean visit(MethodDeclaration methodDeclaration) {

		int modifiers = methodDeclaration.getModifiers();
		if (Modifier.isPrivate(modifiers)) {
			/*
			 * search only inside the compilation unit
			 */

		} else {
			/*
			 * Search in the compilation unit. If no references are found, then
			 * use the 'UnusedMethodsEngine' to search for external references.
			 */
		}

		return false;
	}

	public List<UnusedMethodWrapper> getUnusedPrivateMethods() {
		return Collections.emptyList();
	}

	public List<NonPrivateUnusedMethodCandidate> getNonPrivateCandidates() {
		return Collections.emptyList();
	}
}
