package eu.jsparrow.core.visitor.junit.jupiter;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;

/**
 * O
 * 
 * @since 3.27.0
 *
 */
public class MigrateJUnit4ToJupiterASTVisitor extends AbstractAddImportASTVisitor {

	private final Map<String, String> annotationQualifiedNamesReplacementMap;
	private final Map<String, String> annotationSimpleNamesReplacementMap;

	public MigrateJUnit4ToJupiterASTVisitor() {

		Map<String, String> tmpMap = new HashMap<>();
		tmpMap.put("org.junit.Ignore", "org.junit.jupiter.api.Disabled"); //$NON-NLS-1$//$NON-NLS-2$
		tmpMap.put("org.junit.Test", "org.junit.jupiter.api.Test"); //$NON-NLS-1$//$NON-NLS-2$
		tmpMap.put("org.junit.After", "org.junit.jupiter.api.AfterEach"); //$NON-NLS-1$//$NON-NLS-2$
		tmpMap.put("org.junit.AfterClass", "org.junit.jupiter.api.AfterAll"); //$NON-NLS-1$//$NON-NLS-2$
		tmpMap.put("org.junit.Before", "org.junit.jupiter.api.BeforeEach"); //$NON-NLS-1$//$NON-NLS-2$
		tmpMap.put("org.junit.BeforeClass", "org.junit.jupiter.api.BeforeAll"); //$NON-NLS-1$//$NON-NLS-2$

		annotationQualifiedNamesReplacementMap = Collections.unmodifiableMap(tmpMap);

		tmpMap = new HashMap<>();
		tmpMap.put("Ignore", "Disabled"); //$NON-NLS-1$//$NON-NLS-2$
		tmpMap.put("Test", "Test"); //$NON-NLS-1$//$NON-NLS-2$
		tmpMap.put("After", "AfterEach"); //$NON-NLS-1$//$NON-NLS-2$
		tmpMap.put("AfterClass", "AfterAll"); //$NON-NLS-1$//$NON-NLS-2$
		tmpMap.put("Before", "BeforeEach"); //$NON-NLS-1$//$NON-NLS-2$
		tmpMap.put("BeforeClass", "BeforeAll"); //$NON-NLS-1$//$NON-NLS-2$

		annotationSimpleNamesReplacementMap = Collections.unmodifiableMap(tmpMap);
	}

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		boolean continueVisiting = super.visit(compilationUnit);

		if (!continueVisiting) {
			return false;
		}
		
		JUnit4ReferencesCollectorVisitor jUnit4ReferencesVisitor = new JUnit4ReferencesCollectorVisitor();
		compilationUnit.accept(jUnit4ReferencesVisitor);
		
		if(!jUnit4ReferencesVisitor.isTransformationPossible()) {
			return false;
		}
		
		AnnotationCollectorVisitor annotationsCollectorVisitor = new AnnotationCollectorVisitor();
		compilationUnit.accept(annotationsCollectorVisitor);

		List<Annotation> jUnit4Annotations = annotationsCollectorVisitor.getAnnotations();

		List<ImportDeclaration> orgJUnitAnnotationImports = ASTNodeUtil.convertToTypedList(this.getCompilationUnit()
			.imports(), ImportDeclaration.class)
			.stream()
			.filter(this::isOrgJUnitAnnotationImport)
			.collect(Collectors.toList());

		boolean annotationsOK = jUnit4Annotations.stream()
			.allMatch(annotation -> this.checkAnnotation(annotation, orgJUnitAnnotationImports));

		List<SimpleName> annotationNamesToTransform = jUnit4Annotations.stream()
			.filter(annotation -> isAnnotationTypeInPackageOrgJUnit(annotation.resolveTypeBinding()))
			.map(annotation -> annotation.getTypeName())
			.filter(Name::isSimpleName)
			.map(SimpleName.class::cast)
			.filter(simpleName -> annotationSimpleNamesReplacementMap.containsKey(simpleName.getIdentifier()))
			.collect(Collectors.toList());

		if (annotationsOK) {
			transform(annotationNamesToTransform);
		}
		return false;
	}

	private boolean isOrgJUnitAnnotationImport(ImportDeclaration importDeclaration) {

		IBinding binding = importDeclaration.resolveBinding();
		if (binding.getKind() != IBinding.TYPE) {
			return false;
		}
		ITypeBinding typeBinding = (ITypeBinding) binding;
		if (!typeBinding.isAnnotation()) {
			return false;
		}
		return isAnnotationTypeInPackageOrgJUnit(typeBinding);
	}

	private boolean isAnnotationTypeInPackageOrgJUnit(ITypeBinding typeBinding) {
		IPackageBinding packageBinding = typeBinding.getPackage();

		if (packageBinding == null) {
			return false;

		}
		if (!packageBinding.getName()
			.equals("org.junit")) { //$NON-NLS-1$ )
			return false;
		}
		return true;

	}

	private boolean checkAnnotation(Annotation annotation,
			List<ImportDeclaration> annotationImportsFromOrgJUnitPackage) {

		ITypeBinding typeBinding = annotation.resolveTypeBinding();

		String simpleTypeName = typeBinding.getName();
		if (annotationSimpleNamesReplacementMap.containsKey(simpleTypeName)) {
			if (!annotation.getTypeName()
				.isSimpleName()) {
				return false;
			}
			if (annotationImportsFromOrgJUnitPackage.stream()
				.noneMatch(importDeclaration -> importDeclaration.getName()
					.getFullyQualifiedName()
					.equals(typeBinding.getQualifiedName()))) {
				return false;
			}
			if (annotation.isMarkerAnnotation()) {
				return true;
			}
			if (annotation.isNormalAnnotation()) {
				NormalAnnotation normalAnnotation = (NormalAnnotation) annotation;
				if (normalAnnotation.values()
					.isEmpty()) {
					return true;
				}
			}
			return simpleTypeName.equals("Ignore"); //$NON-NLS-1$
		}

		String qualifiedTypeName = typeBinding.getQualifiedName();
		return !qualifiedTypeName.startsWith("org.junit.") //$NON-NLS-1$
				&& !qualifiedTypeName.startsWith("junit."); //$NON-NLS-1$
	}

	private void transform(List<SimpleName> annotationNamesToTransform) {
		transformImports(annotationNamesToTransform);

		annotationNamesToTransform.forEach(simpleName -> {
			findIdentifierReplacement(simpleName).ifPresent(newIdentifier -> {
				SimpleName newSimpleName = astRewrite.getAST()
					.newSimpleName(newIdentifier);
				this.astRewrite.replace(simpleName, newSimpleName, null);
				onRewrite();
			});
		});
	}

	private void transformImports(List<SimpleName> transformedAnnotationSimpleNames) {

		Set<String> supportedJUnit4AnnotationsUsed = transformedAnnotationSimpleNames.stream()
			.map(SimpleName::getIdentifier)
			.collect(Collectors.toSet());

		if (supportedJUnit4AnnotationsUsed.contains("Ignore")) { //$NON-NLS-1$
			replaceImport("org.junit.Ignore", "org.junit.jupiter.api.Disabled"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (supportedJUnit4AnnotationsUsed.contains("Test")) { //$NON-NLS-1$
			replaceImport("org.junit.Test", "org.junit.jupiter.api.Test"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (supportedJUnit4AnnotationsUsed.contains("After")) { //$NON-NLS-1$
			replaceImport("org.junit.After", "org.junit.jupiter.api.AfterEach"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (supportedJUnit4AnnotationsUsed.contains("AfterClass")) { //$NON-NLS-1$
			replaceImport("org.junit.AfterClass", "org.junit.jupiter.api.AfterAll"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (supportedJUnit4AnnotationsUsed.contains("Before")) { //$NON-NLS-1$
			replaceImport("org.junit.Before", "org.junit.jupiter.api.BeforeEach"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (supportedJUnit4AnnotationsUsed.contains("BeforeClass")) { //$NON-NLS-1$
			replaceImport("org.junit.BeforeClass", "org.junit.jupiter.api.BeforeAll"); //$NON-NLS-1$ //$NON-NLS-2$
		}

	}

	private void replaceImport(String qualifiedTypeNameToReplece, String qualifiedTypeNameReplacement) {

		List<ImportDeclaration> importDeclarations = ASTNodeUtil.convertToTypedList(this.getCompilationUnit()
			.imports(), ImportDeclaration.class);
		for (ImportDeclaration importDeclaration : importDeclarations) {
			IBinding importBinding = importDeclaration.resolveBinding();
			if (importBinding.getKind() == IBinding.TYPE) {
				ITypeBinding typeBinding = (ITypeBinding) importBinding;
				if (typeBinding.getQualifiedName()
					.equals(qualifiedTypeNameToReplece)) {
					astRewrite.remove(importDeclaration, null);
				}
			}
		}
		ListRewrite listRewrite = astRewrite.getListRewrite(getCompilationUnit(), CompilationUnit.IMPORTS_PROPERTY);
		AST ast = astRewrite.getAST();
		ImportDeclaration newImportDeclaration = ast.newImportDeclaration();
		newImportDeclaration.setName(ast.newName(qualifiedTypeNameReplacement));
		listRewrite.insertLast(newImportDeclaration, null);
	}

	private Optional<String> findIdentifierReplacement(SimpleName simpleName) {
		String identifier = simpleName.getIdentifier();
		if (identifier.equals("Ignore")) { //$NON-NLS-1$
			return Optional.of("Disabled"); //$NON-NLS-1$
		}
		if (identifier.equals("Before")) { //$NON-NLS-1$
			return Optional.of("BeforeEach"); //$NON-NLS-1$
		}
		if (identifier.equals("BeforeClass")) { //$NON-NLS-1$
			return Optional.of("BeforeAll"); //$NON-NLS-1$
		}
		if (identifier.equals("After")) { //$NON-NLS-1$
			return Optional.of("AfterEach"); //$NON-NLS-1$
		}
		if (identifier.equals("AfterClass")) { //$NON-NLS-1$
			return Optional.of("AfterAll"); //$NON-NLS-1$
		}
		return Optional.empty();
	}
}
