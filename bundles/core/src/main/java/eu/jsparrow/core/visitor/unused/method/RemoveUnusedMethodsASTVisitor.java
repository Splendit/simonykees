package eu.jsparrow.core.visitor.unused.method;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.text.edits.TextEditGroup;

import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * Removes the provided unused method declarations.
 * 
 * @since 4.9.0
 */
public class RemoveUnusedMethodsASTVisitor extends AbstractASTRewriteASTVisitor {

	private List<UnusedMethodWrapper> unusedMethods;
	private Map<MethodDeclaration, UnusedMethodWrapper> relevantDeclarations;
	private Map<MethodDeclaration, UnusedMethodWrapper> relevantTestUsages;

	public RemoveUnusedMethodsASTVisitor(List<UnusedMethodWrapper> unusedMethods) {
		this.unusedMethods = unusedMethods;
	}

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		IPath currentPath = compilationUnit.getJavaElement()
			.getPath();

		Map<MethodDeclaration, UnusedMethodWrapper> declarations = new HashMap<>();
		Map<MethodDeclaration, UnusedMethodWrapper> tests = new HashMap<>();
		for (UnusedMethodWrapper unusedMethod : unusedMethods) {
			IPath path = unusedMethod.getDeclarationPath();
			if (path.equals(currentPath)) {
				MethodDeclaration declaration = unusedMethod.getMethodDeclaration();
				declarations.put(declaration, unusedMethod);
			} else {
				unusedMethod.getTestReferences()
					.stream()
					.filter(test -> currentPath.equals(test.getICompilationUnit()
						.getPath()))
					.flatMap(test -> test.getTestDeclarations()
						.stream())
					.forEach(testDeclaration -> tests.put(testDeclaration, unusedMethod));
			}
		}
		this.relevantDeclarations = declarations;
		this.relevantTestUsages = tests;
		return super.visit(compilationUnit);
	}

	@Override
	public boolean visit(MethodDeclaration methodDeclaration) {
		UnusedMethodWrapper designated = isDesignatedForRemoval(methodDeclaration, relevantDeclarations)
			.orElse(null);
		if (designated != null) {
			TextEditGroup editGroup = designated.getTextEditGroup((ICompilationUnit) getCompilationUnit()
				.getJavaElement());
			astRewrite.remove(methodDeclaration, editGroup);
			onRewrite();
		} else {
			UnusedMethodWrapper designatedTest = isDesignatedForRemoval(methodDeclaration, relevantTestUsages)
				.orElse(null);
			if (designatedTest != null) {
				TextEditGroup editGroup = designatedTest.getTextEditGroup((ICompilationUnit) getCompilationUnit()
					.getJavaElement());
				astRewrite.remove(methodDeclaration, editGroup);
				onRewrite();
			}
		}
		return true;
	}

	private Optional<UnusedMethodWrapper> isDesignatedForRemoval(MethodDeclaration methodDeclaration,
			Map<MethodDeclaration, UnusedMethodWrapper> map) {
		int start = methodDeclaration.getStartPosition();
		int length = methodDeclaration.getLength();
		for (Map.Entry<MethodDeclaration, UnusedMethodWrapper> entry : map.entrySet()) {
			MethodDeclaration relevantDeclaration = entry.getKey();
			UnusedMethodWrapper unusedMethod = entry.getValue();
			int candidateStart = relevantDeclaration.getStartPosition();
			int candidateLength = relevantDeclaration.getLength();
			if (candidateStart == start && candidateLength == length) {
				return Optional.of(unusedMethod);

			}
		}
		return Optional.empty();
	}
}
