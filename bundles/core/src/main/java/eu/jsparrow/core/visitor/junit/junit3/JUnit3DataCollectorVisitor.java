package eu.jsparrow.core.visitor.junit.junit3;

import static eu.jsparrow.core.visitor.utils.MainMethodMatches.findMainMethodMatches;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import eu.jsparrow.core.visitor.junit.jupiter.common.MethodDeclarationsCollectorVisitor;
import eu.jsparrow.core.visitor.utils.MethodDeclarationUtils;

/**
 * Visitor collecting all type declarations, method declarations and method
 * invocations which will have to be analyzed. Additionally, this visitor
 * determines whether there is a main method which can be removed.
 *
 */
public class JUnit3DataCollectorVisitor extends ASTVisitor {

	private MethodDeclaration mainMethodToRemove;

	private final List<TypeDeclaration> typeDeclarationsToAnalyze = new ArrayList<>();
	private final List<SimpleType> simpleTypesToAnalyze = new ArrayList<>();
	private final List<MethodDeclaration> methodDeclarationsToAnalyze = new ArrayList<>();
	private final List<MethodInvocation> methodInvocationsToAnalyze = new ArrayList<>();

	static Optional<MethodDeclaration> findMainMethodToRemove(CompilationUnit compilationUnit) {
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
			try {
				if (findMainMethodMatches(declaringClass).isEmpty()) {
					return Optional.of(mainMethodDeclaration);
				}
			} catch (CoreException e) {
				throw new RuntimeException(e);
			}
		}
		return Optional.empty();
	}

	@Override
	public boolean visit(CompilationUnit node) {
		mainMethodToRemove = findMainMethodToRemove(node).orElse(null);
		return true;
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		typeDeclarationsToAnalyze.add(node);
		return true;
	}
	
	@Override
	public boolean visit(SimpleType node) {
		simpleTypesToAnalyze.add(node);
		return true;
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		if (mainMethodToRemove != null && mainMethodToRemove == node) {
			return false;
		}
		methodDeclarationsToAnalyze.add(node);
		return true;
	}

	@Override
	public boolean visit(MethodInvocation node) {
		methodInvocationsToAnalyze.add(node);
		return true;
	}

	public Optional<MethodDeclaration> getMainMethodToRemove() {
		return Optional.ofNullable(mainMethodToRemove);
	}

	public List<TypeDeclaration> getTypeDeclarationsToAnalyze() {
		return typeDeclarationsToAnalyze;
	}

	public List<SimpleType> getSimpleTypesToAnalyze() {
		return simpleTypesToAnalyze;
	}

	public List<MethodDeclaration> getMethodDeclarationsToAnalyze() {
		return methodDeclarationsToAnalyze;
	}

	public List<MethodInvocation> getMethodInvocationsToAnalyze() {
		return methodInvocationsToAnalyze;
	}
}
