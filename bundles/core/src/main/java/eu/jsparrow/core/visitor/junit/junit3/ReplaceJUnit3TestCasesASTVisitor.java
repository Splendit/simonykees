package eu.jsparrow.core.visitor.junit.junit3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
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
		verifyStaticMethodImport(compilationUnit, getNewAssertionMethodFullyQualifiedName("assertEquals")); //$NON-NLS-1$
		verifyStaticMethodImport(compilationUnit, getNewAssertionMethodFullyQualifiedName("assertFalse")); //$NON-NLS-1$
		verifyStaticMethodImport(compilationUnit, getNewAssertionMethodFullyQualifiedName("assertTrue")); //$NON-NLS-1$
		verifyStaticMethodImport(compilationUnit, getNewAssertionMethodFullyQualifiedName("assertNotNull")); //$NON-NLS-1$
		verifyStaticMethodImport(compilationUnit, getNewAssertionMethodFullyQualifiedName("assertNull")); //$NON-NLS-1$
		verifyStaticMethodImport(compilationUnit, getNewAssertionMethodFullyQualifiedName("ssertNotSame")); //$NON-NLS-1$
		verifyStaticMethodImport(compilationUnit, getNewAssertionMethodFullyQualifiedName("assertSame")); //$NON-NLS-1$
		verifyStaticMethodImport(compilationUnit, getNewAssertionMethodFullyQualifiedName("fail")); //$NON-NLS-1$

		verifyImport(compilationUnit, migrationConfiguration.getSetupAnnotationQualifiedName());
		verifyImport(compilationUnit, migrationConfiguration.getTeardownAnnotationQualifiedName());
		verifyImport(compilationUnit, migrationConfiguration.getTestAnnotationQualifiedName());
		
		List<ImportDeclaration> importDeclarationsToRemove = new ArrayList<>();
		List<ImportDeclaration> allImportDeclarations = ASTNodeUtil.convertToTypedList(compilationUnit.imports(),
				ImportDeclaration.class);
		
		for (ImportDeclaration importDeclaration : allImportDeclarations) {
			String fullyQualifiedName = importDeclaration.getName()
				.getFullyQualifiedName();
			if (fullyQualifiedName.startsWith("junit.")) { //$NON-NLS-1$
				importDeclarationsToRemove.add(importDeclaration);
			} else if (UnexpectedJunit3References.isUnexpectedJUnitQualifiedName(fullyQualifiedName)) {
				return false;
			}
		}

		JUnit3DataCollectorVisitor junit3DataCollectorVisitor = new JUnit3DataCollectorVisitor();
		compilationUnit.accept(junit3DataCollectorVisitor);

		if (!junit3DataCollectorVisitor.isTransformationPossible()) {
			return false;
		}

		JUnit3TestCaseDeclarationsAnalyzer jUnit3TestCaseDeclarationsAnalyzer = new JUnit3TestCaseDeclarationsAnalyzer();
		boolean transformationPossible = jUnit3TestCaseDeclarationsAnalyzer
			.collectTestCaseDeclarationAnalysisData(junit3DataCollectorVisitor, migrationConfiguration);

		if (!transformationPossible) {
			return false;
		}

		JUnit3AssertionAnalyzer assertionAnalyzer = new JUnit3AssertionAnalyzer();

		transformationPossible = assertionAnalyzer.collectAssertionAnalysisResults(compilationUnit,
				junit3DataCollectorVisitor, jUnit3TestCaseDeclarationsAnalyzer, migrationConfiguration);

		if (!transformationPossible) {
			return false;
		}

		List<JUnit3AssertionAnalysisResult> jUnit3AssertionAnalysisResults = assertionAnalyzer
			.getjUnit3AssertionAnalysisResults();

		Set<String> newAssertionStaticImports = new HashSet<>();
		Set<String> simpleNamesWithStaticImport = new HashSet<>();
		jUnit3AssertionAnalysisResults
			.stream()
			.map(JUnit3AssertionAnalysisResult::getMethodName)
			.forEach(identifier -> {
				String newAssertionMethodFullyQualifiedName = getNewAssertionMethodFullyQualifiedName(identifier);
				if (canAddStaticMethodImport(newAssertionMethodFullyQualifiedName)) {
					newAssertionStaticImports.add(newAssertionMethodFullyQualifiedName);
					simpleNamesWithStaticImport.add(identifier);
				}
			});

		List<JUnit3AssertionReplacementData> assertionReplacementData = collectAssertionReplacementData(
				jUnit3AssertionAnalysisResults, simpleNamesWithStaticImport);

		transform(jUnit3TestCaseDeclarationsAnalyzer, newAssertionStaticImports, assertionReplacementData,
				importDeclarationsToRemove);

		return false;
	}

	private String getNewAssertionMethodFullyQualifiedName(String assertionMethodSimpleName) {
		String classDeclaringMethodReplacement = migrationConfiguration.getAssertionClassQualifiedName();
		return classDeclaringMethodReplacement + '.' + assertionMethodSimpleName;
	}

	private void transform(
			JUnit3TestCaseDeclarationsAnalyzer jUnit3TestCaseDeclarationsAnalyzer,
			Set<String> newAssertionStaticImports,
			List<JUnit3AssertionReplacementData> assertionReplacementData,
			List<ImportDeclaration> importDeclarationsToRemove) {

		jUnit3TestCaseDeclarationsAnalyzer.getMainMethodToRemove()
			.ifPresent(mainMethodToRemove -> {
				astRewrite.remove(mainMethodToRemove, null);
				onRewrite();
			});

		newAssertionStaticImports.forEach(qualifiedName -> {
			AST ast = astRewrite.getAST();
			ImportDeclaration newImportDeclaration = ast.newImportDeclaration();
			newImportDeclaration.setName(ast.newName(qualifiedName));
			newImportDeclaration.setStatic(true);
			ListRewrite listRewrite = astRewrite.getListRewrite(getCompilationUnit(),
					CompilationUnit.IMPORTS_PROPERTY);
			listRewrite.insertFirst(newImportDeclaration, null);
		});

		List<TestMethodAnnotationData> testMethodAnnotationDataList = jUnit3TestCaseDeclarationsAnalyzer
			.getTestMethodAnnotationDataList();
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

		assertionReplacementData.forEach(data -> {
			astRewrite.replace(data.getOriginalMethodInvocation(), data.createMethodInvocationReplecement(), null);
			onRewrite();
		});

		importDeclarationsToRemove.forEach(importDeclarationToRemove -> {
			astRewrite.remove(importDeclarationToRemove, null);
			onRewrite();
		});

		List<SimpleType> jUnit3TestCaseSuperTypesToRemove = jUnit3TestCaseDeclarationsAnalyzer
			.getJUnit3TestCaseSuperTypesToRemove();
		jUnit3TestCaseSuperTypesToRemove.forEach(supertypeToRemove -> {
			astRewrite.remove(supertypeToRemove, null);
			onRewrite();
		});

		List<Annotation> overrideAnnotationsToRemove = jUnit3TestCaseDeclarationsAnalyzer
			.getOverrideAnnotationsToRemove();
		overrideAnnotationsToRemove.forEach(overrideAnnotationToRemove -> {
			astRewrite.remove(overrideAnnotationToRemove, null);
			onRewrite();
		});
	}

	List<JUnit3AssertionReplacementData> collectAssertionReplacementData(
			List<JUnit3AssertionAnalysisResult> jUnit3AssertionAnalysisResults,
			Set<String> simpleNamesWithStaticImport) {

		List<JUnit3AssertionReplacementData> replacementDataList = new ArrayList<>();

		for (JUnit3AssertionAnalysisResult analysisResult : jUnit3AssertionAnalysisResults) {
			MethodInvocation originalMethodnvocation = analysisResult.getMethodInvocation();

			boolean originalInvocationQualified = originalMethodnvocation.getExpression() != null;
			boolean newInvocationQualified = !simpleNamesWithStaticImport.contains(analysisResult.getMethodName());
			Expression messageMovingToLastPosition = analysisResult
				.getMessageMovingToLastPosition()
				.orElse(null);
			if (originalInvocationQualified || newInvocationQualified || messageMovingToLastPosition != null) {
				List<Expression> argumentsToCopy = new ArrayList<>(analysisResult.getOriginalArguments());
				if (messageMovingToLastPosition != null) {
					argumentsToCopy.remove(messageMovingToLastPosition);
					argumentsToCopy.add(messageMovingToLastPosition);
				}
				Supplier<MethodInvocation> newMethodInvocationSupplier;
				if (newInvocationQualified) {
					newMethodInvocationSupplier = () -> createQualifiedAssertion(originalMethodnvocation,
							analysisResult.getMethodName(), argumentsToCopy);
				} else {
					newMethodInvocationSupplier = () -> createAssertionWithoutQualifier(analysisResult.getMethodName(),
							argumentsToCopy);
				}
				replacementDataList
					.add(new JUnit3AssertionReplacementData(originalMethodnvocation, newMethodInvocationSupplier));
			}
		}
		return replacementDataList;
	}

	private MethodInvocation createQualifiedAssertion(MethodInvocation context, String methodName,
			List<Expression> argumentsToCopy) {
		MethodInvocation newMethodInvocation = createAssertionWithoutQualifier(methodName, argumentsToCopy);
		String classDeclaringMethodReplacement = migrationConfiguration.getAssertionClassQualifiedName();
		Name qualifier = addImport(classDeclaringMethodReplacement, context);
		newMethodInvocation.setExpression(qualifier);
		return newMethodInvocation;
	}

	private MethodInvocation createAssertionWithoutQualifier(String methodName, List<Expression> argumentsToCopy) {
		AST ast = astRewrite.getAST();
		MethodInvocation newMethodInvocation = ast
			.newMethodInvocation();
		newMethodInvocation.setName(ast.newSimpleName(methodName));

		@SuppressWarnings("unchecked")
		List<Expression> newArguments = newMethodInvocation.arguments();
		argumentsToCopy.stream()
			.map(arg -> (Expression) astRewrite.createCopyTarget(arg))
			.forEach(newArguments::add);
		return newMethodInvocation;
	}
}