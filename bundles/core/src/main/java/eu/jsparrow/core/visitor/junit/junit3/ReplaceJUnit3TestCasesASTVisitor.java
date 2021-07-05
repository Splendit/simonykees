package eu.jsparrow.core.visitor.junit.junit3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.TypeDeclaration;
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

		JUnit3DataCollectorVisitor junit3DataCollectorVisitor = new JUnit3DataCollectorVisitor();
		compilationUnit.accept(junit3DataCollectorVisitor);
		
		if(!junit3DataCollectorVisitor.isTransformationPossible()) {
			return false;
		}

		JUnit3TestCaseDeclarationsAnalyzer jUnit3TestCaseDeclarationsAnalyzer = new JUnit3TestCaseDeclarationsAnalyzer();
		boolean transformationPossible = jUnit3TestCaseDeclarationsAnalyzer
			.collectTestCaseDeclarationAnalysisData(junit3DataCollectorVisitor);

		if (!transformationPossible) {
			return false;
		}

		JUnit3TestMethodDeclarationsAnalyzer jUnit3TestMethodDeclarationsAnalyzer = new JUnit3TestMethodDeclarationsAnalyzer();
		transformationPossible = jUnit3TestMethodDeclarationsAnalyzer
			.collectMethodDeclarationAnalysisData(junit3DataCollectorVisitor, jUnit3TestCaseDeclarationsAnalyzer,
					migrationConfiguration);

		if (!transformationPossible) {
			return false;
		}

		List<MethodInvocation> methodInvocationsToAnalyze = junit3DataCollectorVisitor.getMethodInvocationsToAnalyze();
		migrationConfiguration.getAssertionClassQualifiedName();
		JUnit3AssertionAnalyzer assertionAnalyzer = new JUnit3AssertionAnalyzer(jUnit3TestMethodDeclarationsAnalyzer,
				classDeclaringMethodReplacement);
		List<JUnit3AssertionAnalysisResult> jUnit3AssertionAnalysisResults = new ArrayList<>();

		for (MethodInvocation methodinvocation : methodInvocationsToAnalyze) {
			IMethodBinding methodBinding = methodinvocation.resolveMethodBinding();
			if (methodBinding == null) {
				return false;
			}
			JUnit3AssertionAnalysisResult assertionAnalysisResult = assertionAnalyzer
				.findAssertionAnalysisResult(methodinvocation, methodBinding)
				.orElse(null);
			if (assertionAnalysisResult != null) {
				jUnit3AssertionAnalysisResults.add(assertionAnalysisResult);
			} else if (UnexpectedJunit3References.hasUnexpectedJUnitReference(methodBinding)) {
				ASTNode declaringNode = compilationUnit.findDeclaringNode(methodBinding);
				if (declaringNode == null) {
					return false;
				}
				if (declaringNode.getNodeType() != ASTNode.METHOD_DECLARATION) {
					return false;
				}
				if (declaringNode.getLocationInParent() != TypeDeclaration.BODY_DECLARATIONS_PROPERTY) {
					return false;
				}
				if (!jUnit3TestCaseDeclarationsAnalyzer.getJUnit3TestCaseDeclarations()
					.contains(declaringNode.getParent())) {
					return false;
				}
			}
		}

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

		List<TestMethodAnnotationData> testMethodAnnotationDataList = jUnit3TestMethodDeclarationsAnalyzer
			.getTestMethodAnnotationDataList();

		List<SimpleType> jUnit3TestCaseSuperTypesToRemove = jUnit3TestCaseDeclarationsAnalyzer
			.getJUnit3TestCaseSuperTypesToRemove();

		List<Annotation> overrideAnnotationsToRemove = jUnit3TestMethodDeclarationsAnalyzer
			.getOverrideAnnotationsToRemove();

		MethodDeclaration mainMethodToRemove = junit3DataCollectorVisitor.getMainMethodToRemove()
			.orElse(null);

		List<ImportDeclaration> importDeclarationsToRemove = collectImportDeclarationsToRemove(compilationUnit);

		if (mainMethodToRemove != null) {
			transform(newAssertionStaticImports, testMethodAnnotationDataList, assertionReplacementData,
					jUnit3AssertionAnalysisResults,
					importDeclarationsToRemove,
					jUnit3TestCaseSuperTypesToRemove, overrideAnnotationsToRemove, mainMethodToRemove);
		} else {
			transform(newAssertionStaticImports, testMethodAnnotationDataList, assertionReplacementData,
					jUnit3AssertionAnalysisResults,
					importDeclarationsToRemove,
					jUnit3TestCaseSuperTypesToRemove, overrideAnnotationsToRemove);
		}
		return false;
	}

	private String getNewAssertionMethodFullyQualifiedName(String assertionMethodSimpleName) {
		String classDeclaringMethodReplacement = migrationConfiguration.getAssertionClassQualifiedName();
		return classDeclaringMethodReplacement + '.' + assertionMethodSimpleName;
	}

	private List<ImportDeclaration> collectImportDeclarationsToRemove(CompilationUnit compilationUnit) {
		List<ImportDeclaration> allImportDeclarations = ASTNodeUtil.convertToTypedList(compilationUnit.imports(),
				ImportDeclaration.class);
		List<ImportDeclaration> importDeclarationsToRemove = new ArrayList<>();
		for (ImportDeclaration importDeclaration : allImportDeclarations) {
			String fullyQualifiedName = importDeclaration.getName()
				.getFullyQualifiedName();
			if (fullyQualifiedName.startsWith("junit.")) { //$NON-NLS-1$
				importDeclarationsToRemove.add(importDeclaration);
			}
		}
		return importDeclarationsToRemove;
	}

	private void transform(
			Set<String> newAssertionStaticImports,
			List<TestMethodAnnotationData> testMethodAnnotationDataList,
			List<JUnit3AssertionReplacementData> assertionReplacementData,
			List<JUnit3AssertionAnalysisResult> assertionAnalysisResults,
			List<ImportDeclaration> importDeclarationsToRemove,
			List<SimpleType> jUnit3TestCaseSuperTypesToRemove,
			List<Annotation> overrideAnnotationsToRemove,
			MethodDeclaration mainMethodToRemove) {

		astRewrite.remove(mainMethodToRemove, null);
		onRewrite();
		transform(newAssertionStaticImports, testMethodAnnotationDataList, assertionReplacementData,
				assertionAnalysisResults,
				importDeclarationsToRemove, jUnit3TestCaseSuperTypesToRemove, overrideAnnotationsToRemove);

	}

	private void transform(
			Set<String> newAssertionStaticImports,
			List<TestMethodAnnotationData> testMethodAnnotationDataList,
			List<JUnit3AssertionReplacementData> assertionReplacementData,
			List<JUnit3AssertionAnalysisResult> assertionAnalysisResults,
			List<ImportDeclaration> importDeclarationsToRemove,
			List<SimpleType> jUnit3TestCaseSuperTypesToRemove,
			List<Annotation> overrideAnnotationsToRemove) {

		newAssertionStaticImports.forEach(qualifiedName -> {
			AST ast = astRewrite.getAST();
			ImportDeclaration newImportDeclaration = ast.newImportDeclaration();
			newImportDeclaration.setName(ast.newName(qualifiedName));
			newImportDeclaration.setStatic(true);
			ListRewrite listRewrite = astRewrite.getListRewrite(getCompilationUnit(),
					CompilationUnit.IMPORTS_PROPERTY);
			listRewrite.insertFirst(newImportDeclaration, null);
		});

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

		jUnit3TestCaseSuperTypesToRemove.forEach(supertypeToRemove -> {
			astRewrite.remove(supertypeToRemove, null);
			onRewrite();
		});

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