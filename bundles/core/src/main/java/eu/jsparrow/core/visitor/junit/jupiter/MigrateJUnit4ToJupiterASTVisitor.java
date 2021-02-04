package eu.jsparrow.core.visitor.junit.jupiter;

import java.util.ArrayList;
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
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;

/**
 * 
 * 
 * @since 3.27.0
 *
 */
public class MigrateJUnit4ToJupiterASTVisitor extends AbstractAddImportASTVisitor {

	private static final Map<String, String> ANNOTATION_QUALIFIED_NAMES_REPLACEMENT_MAP;

	static {

		Map<String, String> tmpMap = new HashMap<>();
		tmpMap.put("org.junit.Ignore", "org.junit.jupiter.api.Disabled"); //$NON-NLS-1$//$NON-NLS-2$
		tmpMap.put("org.junit.Test", "org.junit.jupiter.api.Test"); //$NON-NLS-1$//$NON-NLS-2$
		tmpMap.put("org.junit.After", "org.junit.jupiter.api.AfterEach"); //$NON-NLS-1$//$NON-NLS-2$
		tmpMap.put("org.junit.AfterClass", "org.junit.jupiter.api.AfterAll"); //$NON-NLS-1$//$NON-NLS-2$
		tmpMap.put("org.junit.Before", "org.junit.jupiter.api.BeforeEach"); //$NON-NLS-1$//$NON-NLS-2$
		tmpMap.put("org.junit.BeforeClass", "org.junit.jupiter.api.BeforeAll"); //$NON-NLS-1$//$NON-NLS-2$

		ANNOTATION_QUALIFIED_NAMES_REPLACEMENT_MAP = Collections.unmodifiableMap(tmpMap);

	}

	public MigrateJUnit4ToJupiterASTVisitor() {

	}

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		boolean continueVisiting = super.visit(compilationUnit);

		if (!continueVisiting) {
			return false;
		}

		MigrateJUnit4ToJupiterAnalyzerVisitor jUnit4ReferencesVisitor = new MigrateJUnit4ToJupiterAnalyzerVisitor();
		compilationUnit.accept(jUnit4ReferencesVisitor);

		if (!jUnit4ReferencesVisitor.isTransformationPossible()) {
			return false;
		}

		AnnotationCollectorVisitor annotationsCollectorVisitor = new AnnotationCollectorVisitor();
		compilationUnit.accept(annotationsCollectorVisitor);

		List<Annotation> allAnnotations = annotationsCollectorVisitor.getAnnotations();
		List<AnnotationTransformationData> annotationTransformationDataList = new ArrayList<>();
		allAnnotations.stream()
			.forEach(annotation -> {
				String qualifiedTypeName = annotation.resolveTypeBinding()
					.getQualifiedName();
				if (ANNOTATION_QUALIFIED_NAMES_REPLACEMENT_MAP.containsKey(qualifiedTypeName)) {
					annotationTransformationDataList
						.add(new AnnotationTransformationData(annotation.getTypeName(), qualifiedTypeName));
				}
			});

		List<SimpleName> annotationNamesToTransform = annotationTransformationDataList.stream()
			.map(data -> data.getOriginalTypeName())
			.filter(Name::isSimpleName)
			.map(SimpleName.class::cast)
			.collect(Collectors.toList());

		transform(annotationNamesToTransform);
		return false;
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
