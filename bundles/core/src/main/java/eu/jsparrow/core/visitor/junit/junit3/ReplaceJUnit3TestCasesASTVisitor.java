package eu.jsparrow.core.visitor.junit.junit3;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.core.visitor.junit.jupiter.common.MethodInvocationsCollectorVisitor;
import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;

public class ReplaceJUnit3TestCasesASTVisitor extends AbstractAddImportASTVisitor {
	private final Junit3MigrationConfiguration migrationConfiguration;

	public ReplaceJUnit3TestCasesASTVisitor(Junit3MigrationConfiguration migrationConfiguration) {
		this.migrationConfiguration = migrationConfiguration;
	}

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		super.visit(compilationUnit);
		String classDeclaringMethodReplacement = migrationConfiguration.getAssertionClassQualifiedName();
		verifyImport(compilationUnit, classDeclaringMethodReplacement);

		JUnit3TestMethodsStore testMethodStore = new JUnit3TestMethodsStore(compilationUnit);
		JUnit3AssertionAnalyzer assertionAnalyzer = new JUnit3AssertionAnalyzer(testMethodStore,
				classDeclaringMethodReplacement);
		List<JUnit3AssertionAnalysisResult> assertionAnalysisResults = new ArrayList<>();
		MethodInvocationsCollectorVisitor invocationCollectorVisitor = new MethodInvocationsCollectorVisitor();
		compilationUnit.accept(invocationCollectorVisitor);
		for (MethodInvocation methodInvocation : invocationCollectorVisitor.getMethodInvocations()) {
			IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
			if (methodBinding == null) {
				return false;
			}
			if (isJUnit3Method(methodBinding)) {
				JUnit3AssertionAnalysisResult assertionAnalysisResult = assertionAnalyzer
					.findAssertionAnalysisResult(methodInvocation, methodBinding)
					.orElse(null);
				if (assertionAnalysisResult != null) {
					assertionAnalysisResults.add(assertionAnalysisResult);
				} else {
					return false;
				}
			}
		}
		return true;
	}

	private boolean isJUnit3Method(IMethodBinding methodBinding) {
		ITypeBinding declaringClass = methodBinding.getDeclaringClass();
		String declaringClassQualifiedName = declaringClass
			.getQualifiedName();
		return declaringClassQualifiedName.startsWith("junit.extensions.") || //$NON-NLS-1$
				declaringClassQualifiedName.startsWith("junit.framework."); //$NON-NLS-1$
	}
}