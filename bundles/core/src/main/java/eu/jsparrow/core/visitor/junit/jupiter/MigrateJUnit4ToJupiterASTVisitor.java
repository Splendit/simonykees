package eu.jsparrow.core.visitor.junit.jupiter;

import java.util.List;
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

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		boolean continueVisiting = super.visit(compilationUnit);

		if (!continueVisiting) {
			return false;
		}

		AnnotationCollectorVisitor collectorVisitor = new AnnotationCollectorVisitor();
		compilationUnit.accept(collectorVisitor);
		List<Annotation> jUnit4Annotations = collectorVisitor.getAnnotations();

		List<ImportDeclaration> orgJUnitAnnotationImports = ASTNodeUtil.convertToTypedList(this.getCompilationUnit()
			.imports(), ImportDeclaration.class)
			.stream()
			.filter(this::isOrgJUnitAnnotationImport)
			.collect(Collectors.toList());

		boolean analysisOK = jUnit4Annotations.stream()
			.allMatch(annotation -> this.checkAnnotation(annotation, orgJUnitAnnotationImports));

		if (analysisOK) {
			transform(jUnit4Annotations);
		}
		return true;
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
		IPackageBinding packageBinding = typeBinding.getPackage();

		if (packageBinding == null) {
			return false;

		}
		return packageBinding.getName()
			.equals("org.junit"); //$NON-NLS-1$

	}

	private boolean isJUnit4AnnotationType(ITypeBinding typeBinding) {
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
		if (simpleTypeName.equals("Ignore") //$NON-NLS-1$
				|| simpleTypeName.equals("Test") //$NON-NLS-1$
				|| simpleTypeName.equals("Before") //$NON-NLS-1$
				|| simpleTypeName.equals("BeforeClass") //$NON-NLS-1$
				|| simpleTypeName.equals("After") //$NON-NLS-1$
				|| simpleTypeName.equals("AfterClass") //$NON-NLS-1$
		) {
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

	private void transform(List<Annotation> jUnit4Annotations) {
		List<SimpleName> simpleAnnotationTypeNames = jUnit4Annotations.stream()
			.filter(annotation -> isJUnit4AnnotationType(annotation.resolveTypeBinding()))
			.map(annotation -> annotation.getTypeName())
			.filter(Name::isSimpleName)
			.map(SimpleName.class::cast)
			.collect(Collectors.toList());

		transformImports(simpleAnnotationTypeNames);

		simpleAnnotationTypeNames.forEach(simpleName -> {
			findIdentifierReplacement(simpleName).ifPresent(newIdentifier -> {
				SimpleName newSimpleName = astRewrite.getAST()
					.newSimpleName(newIdentifier);
				this.astRewrite.replace(simpleName, newSimpleName, null);
				onRewrite();
			});
		});
	}

	private void transformImports(List<SimpleName> simpleAnnotationTypeNames) {

		Set<String> supportedJUnit4AnnotationsUsed = simpleAnnotationTypeNames.stream()
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
