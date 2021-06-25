package eu.jsparrow.core.visitor.junit.junit3;

import static eu.jsparrow.core.visitor.utils.MainMethodMatches.findMainMethodMatches;

import java.util.List;
import java.util.Optional;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.search.SearchMatch;

import eu.jsparrow.core.visitor.junit.jupiter.common.MethodDeclarationsCollectorVisitor;
import eu.jsparrow.core.visitor.utils.MethodDeclarationUtils;

public class JavaApplicationMainMethodStore {
	private final MethodDeclaration mainMethodDeclaration;

	public static Optional<JavaApplicationMainMethodStore> findJavaApplicationMainMethodStore(
			CompilationUnit compilationUnit) {
		MethodDeclarationsCollectorVisitor methodDeclarationsCollectorVisitor = new MethodDeclarationsCollectorVisitor();
		compilationUnit.accept(methodDeclarationsCollectorVisitor);
		MethodDeclaration javaApplicaionMainMethod = methodDeclarationsCollectorVisitor.getMethodDeclarations()
			.stream()
			.filter(methodDeclaration -> MethodDeclarationUtils.isJavaApplicationMainMethod(compilationUnit,
					methodDeclaration))
			.findFirst()
			.orElse(null);

		if (javaApplicaionMainMethod != null) {
			return Optional.of(new JavaApplicationMainMethodStore(javaApplicaionMainMethod));
		}
		return Optional.empty();
	}

	private JavaApplicationMainMethodStore(MethodDeclaration mainMethod) {
		this.mainMethodDeclaration = mainMethod;
	}

	public List<SearchMatch> findMatches() throws CoreException {
		ITypeBinding declaringClass = mainMethodDeclaration.resolveBinding()
			.getDeclaringClass();
		return findMainMethodMatches(declaringClass);
	}

	public MethodDeclaration getMainMethodDeclaration() {
		return mainMethodDeclaration;
	}
}