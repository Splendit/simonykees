package eu.jsparrow.core.visitor.junit.junit3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;

/**
 * This visitor carries out re-factorings in connection with the migration of
 * JUnit 3 test cases to either JUnit 4 or JUnit Jupiter, for example:
 * <ul>
 * <li>Removing {@code extends TestCase} - clauses</li>
 * <li>Removing imports like {@code import junit.framework.TestCase; }</li>
 * <li>Introducing static method imports like
 * {@code import static org.junit.Assert.assertEquals;}</li>
 * <li>Annotating test methods with {@code @Test}</li>
 * </ul>
 * 
 * @since 4.1.0
 * 
 */
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

		verifyImport(compilationUnit, migrationConfiguration.getSetUpAnnotationQualifiedName());
		verifyImport(compilationUnit, migrationConfiguration.getTearDownAnnotationQualifiedName());
		verifyImport(compilationUnit, migrationConfiguration.getTestAnnotationQualifiedName());

		JUnit3DataCollectorVisitor jUnit3dataCollectorVisitor = new JUnit3DataCollectorVisitor(
				migrationConfiguration, compilationUnit);
		compilationUnit.accept(jUnit3dataCollectorVisitor);

		if (jUnit3dataCollectorVisitor.isTransformationPossible()) {

			List<JUnit3AssertionAnalysisResult> jUnit3AssertionAnalysisResults = jUnit3dataCollectorVisitor
				.getJUnit3AssertionAnalysisResults();

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

			transform(jUnit3dataCollectorVisitor, newAssertionStaticImports, assertionReplacementData,
					collectRemoveRunTestInvocationsData(jUnit3dataCollectorVisitor));
		}
		return false;
	}

	private String getNewAssertionMethodFullyQualifiedName(String assertionMethodSimpleName) {
		String classDeclaringMethodReplacement = migrationConfiguration.getAssertionClassQualifiedName();
		return classDeclaringMethodReplacement + '.' + assertionMethodSimpleName;
	}

	private RemoveRunTestInvocationsData collectRemoveRunTestInvocationsData(
			JUnit3DataCollectorVisitor jUnit3dataCollectorVisitor) {
		MethodDeclaration mainMethodToRemove = jUnit3dataCollectorVisitor.getMainMethodToRemove()
			.orElse(null);

		if (mainMethodToRemove == null) {
			return new RemoveRunTestInvocationsData();
		}

		Block body = mainMethodToRemove.getBody();
		List<Statement> statements = ASTNodeUtil.convertToTypedList(body.statements(), Statement.class);
		if (statements.isEmpty()) {
			return new RemoveRunTestInvocationsData(mainMethodToRemove);
		}

		List<ITypeBinding> testCaseTypeBindings = jUnit3dataCollectorVisitor.getJUnit3TestCaseDeclarations()
			.stream()
			.map(AbstractTypeDeclaration::resolveBinding)
			.collect(Collectors.toList());

		RunTestInvocationStatementsVisitor runTestInvocationStatementsVisitor = new RunTestInvocationStatementsVisitor();
		mainMethodToRemove.accept(runTestInvocationStatementsVisitor);
		List<ExpressionStatement> expressionStatementsToRemove = new ArrayList<>();
		List<SimpleName> invocationExpressionsToQualify = new ArrayList<>();
		Map<ExpressionStatement, TypeLiteral> runInvocationToTypeLiteralMap = runTestInvocationStatementsVisitor
			.getRunInvocationToTypeLiteralMap();
		runInvocationToTypeLiteralMap
			.forEach((expressionStatement, typeLiteral) -> {
				ITypeBinding typeLiteralTypeBinding = typeLiteral.getType()
					.resolveBinding();
				boolean containedInTestCasetypeBindings = testCaseTypeBindings
					.stream()
					.anyMatch(
							typeBinding -> ClassRelationUtil.compareITypeBinding(typeBinding, typeLiteralTypeBinding));
				if (containedInTestCasetypeBindings) {
					expressionStatementsToRemove.add(expressionStatement);
				} else {
					MethodInvocation methodInvocation = (MethodInvocation) expressionStatement.getExpression();
					if (methodInvocation.getExpression()
						.getNodeType() == ASTNode.SIMPLE_NAME) {
						SimpleName simpleName = (SimpleName) methodInvocation.getExpression();
						invocationExpressionsToQualify.add(simpleName);
					}
				}
			});

		if (statements.size() == 1) {
			Statement onlyStatement = statements.get(0);
			if (expressionStatementsToRemove.contains(onlyStatement)) {
				return new RemoveRunTestInvocationsData(mainMethodToRemove);
			}
		}
		return new RemoveRunTestInvocationsData(expressionStatementsToRemove, invocationExpressionsToQualify);
	}

	private void transform(
			JUnit3DataCollectorVisitor jUnit3dataCollectorVisitor,
			Set<String> newAssertionStaticImports,
			List<JUnit3AssertionReplacementData> assertionReplacementData,
			RemoveRunTestInvocationsData removeRunTestInvocationsData) {

		newAssertionStaticImports.forEach(qualifiedName -> {
			AST ast = astRewrite.getAST();
			ImportDeclaration newImportDeclaration = ast.newImportDeclaration();
			newImportDeclaration.setName(ast.newName(qualifiedName));
			newImportDeclaration.setStatic(true);
			ListRewrite listRewrite = astRewrite.getListRewrite(getCompilationUnit(),
					CompilationUnit.IMPORTS_PROPERTY);
			listRewrite.insertFirst(newImportDeclaration, null);
		});

		List<TestMethodAnnotationData> testMethodAnnotationDataList = jUnit3dataCollectorVisitor
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
		});

		assertionReplacementData.forEach(data -> astRewrite.replace(data.getOriginalMethodInvocation(),
				data.createMethodInvocationReplacement(), null));

		List<ImportDeclaration> importDeclarationsToRemove = jUnit3dataCollectorVisitor
			.getImportDeclarationsToRemove();
		importDeclarationsToRemove
			.forEach(importDeclarationToRemove -> astRewrite.remove(importDeclarationToRemove, null));

		List<SimpleType> jUnit3TestCaseSuperTypesToRemove = jUnit3dataCollectorVisitor
			.getJUnit3TestCaseSuperTypesToRemove();
		jUnit3TestCaseSuperTypesToRemove.forEach(supertypeToRemove -> astRewrite.remove(supertypeToRemove, null));

		List<Annotation> overrideAnnotationsToRemove = jUnit3dataCollectorVisitor
			.getOverrideAnnotationsToRemove();
		overrideAnnotationsToRemove
			.forEach(overrideAnnotationToRemove -> astRewrite.remove(overrideAnnotationToRemove, null));

		jUnit3dataCollectorVisitor.getSuperMethodInvocationsToRemove()
			.forEach(superMethodInvocationToRemove -> astRewrite.remove(superMethodInvocationToRemove, null));

		removeRunTestInvocationsData.getMainMethodToRemove()
			.ifPresent(mainMethodToRemove -> astRewrite.remove(mainMethodToRemove, null));

		removeRunTestInvocationsData.getExpressionStatementsToRemove()
			.forEach(runTestStatementToRemove -> astRewrite.remove(runTestStatementToRemove, null));

		removeRunTestInvocationsData.getInvocationExpressionsToQualify()
			.forEach(unqualifiedInvocationExpression -> {
				Name qualifiedInvocationExpression = astRewrite.getAST()
					.newName("junit.textui.TestRunner"); //$NON-NLS-1$
				astRewrite.replace(unqualifiedInvocationExpression, qualifiedInvocationExpression, null);
			});

		onRewrite();
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