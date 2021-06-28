package eu.jsparrow.core.visitor.junit.junit3;

import static eu.jsparrow.core.visitor.utils.MainMethodMatches.findMainMethodMatches;

import java.util.List;
import java.util.Optional;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import eu.jsparrow.core.visitor.junit.jupiter.common.MethodDeclarationsCollectorVisitor;
import eu.jsparrow.core.visitor.utils.MethodDeclarationUtils;

/**
 * Helper class storing an optional {@link MethodDeclaration} representing an
 * unreferenced Java main method.
 *
 */
class UnreferencedMainMethodStore {

	private MethodDeclaration unreferencedMainMethod;

	/**
	 * Stores a {@link MethodDeclaration} representing an unreferenced Java main
	 * method if such a {@link MethodDeclaration} can be found.
	 */
	void analyzeMainMethodOccurrence(CompilationUnit compilationUnit) throws CoreException {

		MethodDeclarationsCollectorVisitor methodDeclarationsCollectorVisitor = new MethodDeclarationsCollectorVisitor();
		compilationUnit.accept(methodDeclarationsCollectorVisitor);
		List<MethodDeclaration> allMethodDeclarations = methodDeclarationsCollectorVisitor.getMethodDeclarations();
		MethodDeclaration mainMethodDeclaration = allMethodDeclarations
			.stream()
			.filter(methodDeclaration -> MethodDeclarationUtils.isJavaApplicationMainMethod(compilationUnit,
					methodDeclaration))
			.findFirst()
			.orElse(null);

		if (mainMethodDeclaration != null) {
			ITypeBinding declaringClass = mainMethodDeclaration.resolveBinding()
				.getDeclaringClass();
			if (findMainMethodMatches(declaringClass).isEmpty()) {
				unreferencedMainMethod = mainMethodDeclaration;
			}
		}
	}

	boolean isSurroundedByMethodDeclaration(ASTNode node) {
		if (unreferencedMainMethod == null) {
			return false;
		}
		ASTNode parent = node.getParent();
		while (parent != null) {
			if (parent == unreferencedMainMethod) {
				return true;
			}
			parent = parent.getParent();
		}
		return false;
	}

	Optional<MethodDeclaration> getUnreferencedMainMethod() {
		return Optional.ofNullable(unreferencedMainMethod);
	}
}
