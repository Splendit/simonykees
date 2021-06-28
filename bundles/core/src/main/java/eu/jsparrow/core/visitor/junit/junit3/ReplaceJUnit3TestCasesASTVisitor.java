package eu.jsparrow.core.visitor.junit.junit3;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

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

		ReplaceJUnit3TestCasesAnalyzer replaceJUnit3TestCasesAnalyzer = new ReplaceJUnit3TestCasesAnalyzer();

		ReplaceJUnit3TestCasesAnalysisData analysisData;
		try {
			analysisData = replaceJUnit3TestCasesAnalyzer.analyzeCompilationUnit(
					compilationUnit, migrationConfiguration, classDeclaringMethodReplacement)
				.orElse(null);
		} catch (CoreException e) {
			return false;
		}

		if (analysisData == null) {
			return false;
		}

		MethodDeclaration mainMethodToRemove = analysisData.getMainMethodToRemove()
			.orElse(null);

		if (mainMethodToRemove != null) {
			astRewrite.remove(mainMethodToRemove, null);
			onRewrite();
		}
		transform(analysisData.getAssertionAnalysisResults(), analysisData.getTestMethodAnnotationDataList());

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
}