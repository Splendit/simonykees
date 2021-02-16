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
 * Replaces JUnit 4 annotations with the corresponding JUnit Jupiter. The
 * following annotations:
 * <ul>
 * <li>{@code org.junit.Ignore}</li>
 * <li>{@code org.junit.Test}</li>
 * <li>{@code org.junit.After}</li>
 * <li>{@code org.junit.AfterClass}</li>
 * <li>{@code org.junit.Before}</li>
 * <li>{@code org.junit.BeforeClass}</li>
 * </ul>
 * are mapped respectively to:
 * <ul>
 * <li>{@code org.junit.jupiter.api.Disabled}</li>
 * <li>{@code org.junit.jupiter.api.Test}</li>
 * <li>{@code org.junit.jupiter.api.AfterEach}</li>
 * <li>{@code org.junit.jupiter.api.AfterAll}</li>
 * <li>{@code org.junit.jupiter.api.BeforeEach}</li>
 * <li>{@code org.junit.jupiter.api.BeforeAll}</li>
 * </ul>
 * The transformation in one class is atomic. If any unsupported annotation
 * occurs, it would prevent the transformation on the entire class.
 * 
 * @since 3.27.0
 * 
 */
public class ReplaceJUnit4AnnotationsWithJupiterASTVisitor extends AbstractAddImportASTVisitor {

	static final Map<String, String> ANNOTATION_QUALIFIED_NAMES_REPLACEMENT_MAP;

	private static final int ORG_J_UNIT_JUPITER_API_PACKAGE_LENGTH = "org.junit.jupiter.api.".length(); //$NON-NLS-1$

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

	@Override
	public boolean visit(CompilationUnit compilationUnit) {

		boolean continueVisiting = super.visit(compilationUnit);
		if (!continueVisiting) {
			return false;
		}

		ReplaceJUnit4AnnotationsWithJupiterVisitor jUnit4ReferencesVisitor = new ReplaceJUnit4AnnotationsWithJupiterVisitor();
		compilationUnit.accept(jUnit4ReferencesVisitor);
		if (!jUnit4ReferencesVisitor.isTransformationPossible()) {
			return false;
		}

		List<AnnotationTransformationData> transformationDataList = createAnnotationDataList(compilationUnit);

		List<ImportDeclaration> importsToRemove = ASTNodeUtil
			.convertToTypedList(compilationUnit.imports(), ImportDeclaration.class)
			.stream()
			.filter(this::isJUnit4AnnotationImport)
			.collect(Collectors.toList());

		Set<String> safeNewAnnotationImports = transformationDataList.stream()
			.map(AnnotationTransformationData::getSafeNewTypeImport)
			.filter(Optional::isPresent)
			.map(Optional::get)
			.collect(Collectors.toSet());

		List<AnnotationTransformationData> annotationNameReplacementDataList = transformationDataList
			.stream()
			.filter(data -> !isTestAnnotationSimpleName(data.getOriginalTypeName()))
			.collect(Collectors.toList());

		transform(importsToRemove, safeNewAnnotationImports, annotationNameReplacementDataList);
		return false;
	}

	private List<AnnotationTransformationData> createAnnotationDataList(CompilationUnit compilationUnit) {

		AnnotationCollectorVisitor annotationsCollectorVisitor = new AnnotationCollectorVisitor();
		compilationUnit.accept(annotationsCollectorVisitor);

		List<AnnotationTransformationData> jUnit4AnnotationsDataList = new ArrayList<>();
		annotationsCollectorVisitor.getAnnotations()
			.stream()
			.forEach(annotation -> {
				ITypeBinding typeBinding = annotation.resolveTypeBinding();
				String originalQualifiedTypeName = typeBinding.getQualifiedName();
				if (ANNOTATION_QUALIFIED_NAMES_REPLACEMENT_MAP.containsKey(originalQualifiedTypeName)) {
					String newQualifiedTypeName = ANNOTATION_QUALIFIED_NAMES_REPLACEMENT_MAP
						.get(originalQualifiedTypeName);

					Name originalTypeName = annotation.getTypeName();
					boolean safeToImportNewType = isSafeToImportNewType(newQualifiedTypeName, originalTypeName);
					AnnotationTransformationData jUnit4AnnotationData;
					if (safeToImportNewType) {
						String newTypeUnqualifiedName = newQualifiedTypeName
							.substring(ORG_J_UNIT_JUPITER_API_PACKAGE_LENGTH);
						jUnit4AnnotationData = new AnnotationTransformationData(
								originalTypeName, newTypeUnqualifiedName, newQualifiedTypeName);
					} else {
						jUnit4AnnotationData = new AnnotationTransformationData(
								originalTypeName, newQualifiedTypeName);
					}
					jUnit4AnnotationsDataList.add(jUnit4AnnotationData);
				}
			});
		return jUnit4AnnotationsDataList;
	}

	private boolean isSafeToImportNewType(String newQualifiedTypeName, Name originalTypeName) {
		if (isTestAnnotationSimpleName(originalTypeName)) {
			return true;
		}
		verifyImport(getCompilationUnit(), newQualifiedTypeName);
		return isSimpleTypeNameUnivocal(newQualifiedTypeName, originalTypeName);

	}

	private boolean isTestAnnotationSimpleName(Name annotationTypeName) {
		if (!annotationTypeName.isSimpleName()) {
			return false;
		}
		String identifier = ((SimpleName) annotationTypeName).getIdentifier();
		return identifier.equals("Test"); //$NON-NLS-1$
	}

	private boolean isJUnit4AnnotationImport(ImportDeclaration importDeclaration) {
		IBinding importBinding = importDeclaration.resolveBinding();
		if (importBinding.getKind() != IBinding.TYPE) {
			return false;
		}
		ITypeBinding typeBinding = (ITypeBinding) importBinding;
		String qualifiedName = typeBinding.getQualifiedName();
		return ANNOTATION_QUALIFIED_NAMES_REPLACEMENT_MAP.containsKey(qualifiedName);
	}

	private void transform(List<ImportDeclaration> importsToRemove, Set<String> safeNewAnnotationImports,
			List<AnnotationTransformationData> annotationNameReplacementDataList) {
		importsToRemove.forEach(importDeclaration -> astRewrite.remove(importDeclaration, null));

		AST ast = astRewrite.getAST();
		ListRewrite newImportsListRewrite = astRewrite.getListRewrite(getCompilationUnit(),
				CompilationUnit.IMPORTS_PROPERTY);

		safeNewAnnotationImports
			.stream()
			.forEach(newAnnotationImport -> {
				ImportDeclaration newImportDeclaration = ast.newImportDeclaration();
				newImportDeclaration.setName(ast.newName(newAnnotationImport));
				newImportsListRewrite.insertLast(newImportDeclaration, null);
			});

		annotationNameReplacementDataList.stream()
			.forEach(data -> {
				Name originalTypeName = data.getOriginalTypeName();
				String newTapeNameAsString = data.getNewTypeName();
				Name newAnnotationTypeName = ast.newName(newTapeNameAsString);
				astRewrite.replace(originalTypeName, newAnnotationTypeName, null);
			});
		if(!importsToRemove.isEmpty() 
				|| !safeNewAnnotationImports.isEmpty() 
				|| !annotationNameReplacementDataList.isEmpty()) {
			onRewrite();
		}
	}
}
