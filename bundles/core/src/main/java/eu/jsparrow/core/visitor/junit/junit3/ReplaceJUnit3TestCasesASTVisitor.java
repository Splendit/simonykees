package eu.jsparrow.core.visitor.junit.junit3;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

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
		verifyImport(compilationUnit, migrationConfiguration.getSetupAnnotationQualifiedName());
		verifyImport(compilationUnit, migrationConfiguration.getTeardownAnnotationQualifiedName());
		verifyImport(compilationUnit, migrationConfiguration.getTestAnnotationQualifiedName());

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

		List<TestMethodAnnotationData> testMethodAnnotationDataList = testMethodStore.getJUnit3TestMethods()
			.stream()
			.map(this::createTestMethodAnnotationData)
			.collect(Collectors.toList());

		transform(assertionAnalysisResults, testMethodAnnotationDataList);

		return true;
	}

	private void transform(List<JUnit3AssertionAnalysisResult> assertionAnalysisResults,
			List<TestMethodAnnotationData> testMethodAnnotationDataList) {

		testMethodAnnotationDataList.forEach(data -> {
			MethodDeclaration methodDeclaration = data.getMethodDeclaration();
			Name annotationName = addImport(data.getAnnotationQualifiedName(), methodDeclaration);
			AST ast = astRewrite.getAST();
			MarkerAnnotation testMethodAnnotation = ast.newMarkerAnnotation();
			testMethodAnnotation.setTypeName(annotationName);

			ListRewrite listRewrite = astRewrite.getListRewrite(methodDeclaration,
					MethodDeclaration.MODIFIERS2_PROPERTY);
			listRewrite.insertFirst(testMethodAnnotation, null);
			onRewrite();
		});

		assertionAnalysisResults.forEach(data -> {
			MethodInvocation methodInvocation = data.getMethodInvocation();
			Expression oldQualifier = methodInvocation.getExpression();
			Name newQualifier = addImport(data.getClassDeclaringMethodReplacement(), methodInvocation);
			if (oldQualifier != null) {
				astRewrite.replace(oldQualifier, newQualifier, null);
			} else {
				astRewrite.set(methodInvocation, MethodInvocation.EXPRESSION_PROPERTY, newQualifier, null);
			}
			Expression messageMovingToLastPosition = data.getMessageMovingToLastPosition()
				.orElse(null);
			if (messageMovingToLastPosition != null) {
				ASTNode messageMoveTarget = astRewrite.createMoveTarget(messageMovingToLastPosition);
				astRewrite.getListRewrite(methodInvocation, MethodInvocation.ARGUMENTS_PROPERTY)
					.insertLast(messageMoveTarget, null);
			}
			onRewrite();
		});
	}

	private TestMethodAnnotationData createTestMethodAnnotationData(MethodDeclaration methodDeclaration) {
		String methodName = methodDeclaration.getName()
			.getIdentifier();
		String annotationQualifiedName;
		if (methodName.equals(JUnit3TestMethodsStore.SET_UP)) {
			annotationQualifiedName = migrationConfiguration.getSetupAnnotationQualifiedName();
		} else if (methodName.equals(JUnit3TestMethodsStore.TEAR_DOWN)) {
			annotationQualifiedName = migrationConfiguration.getTeardownAnnotationQualifiedName();
		} else {
			annotationQualifiedName = migrationConfiguration.getTestAnnotationQualifiedName();
		}
		return new TestMethodAnnotationData(methodDeclaration, annotationQualifiedName);
	}

	private boolean isJUnit3Method(IMethodBinding methodBinding) {
		ITypeBinding declaringClass = methodBinding.getDeclaringClass();
		String declaringClassQualifiedName = declaringClass
			.getQualifiedName();
		return declaringClassQualifiedName.startsWith("junit.extensions.") || //$NON-NLS-1$
				declaringClassQualifiedName.startsWith("junit.framework."); //$NON-NLS-1$
	}
}