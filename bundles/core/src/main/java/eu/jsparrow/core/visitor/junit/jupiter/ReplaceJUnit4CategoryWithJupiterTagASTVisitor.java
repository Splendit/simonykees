package eu.jsparrow.core.visitor.junit.jupiter;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import eu.jsparrow.core.visitor.sub.FirstSimpleTypeOccurrenceVisitor;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;

/**
 * Replaces each JUnit 4 annotation of the type
 * {@code org.junit.experimental.categories.Category} with one or more JUnit
 * Jupiter annotations of the type {@code org.junit.jupiter.api.Tag}.
 * 
 * @since 3.30.0
 * 
 */
public class ReplaceJUnit4CategoryWithJupiterTagASTVisitor extends AbstractAddImportASTVisitor {

	private static final String ORG_JUNIT_EXPERIMENTAL_CATEGORIES_CATEGORY = "org.junit.experimental.categories.Category"; //$NON-NLS-1$
	private static final String ORG_JUNIT_JUPITER_API_TAG = "org.junit.jupiter.api.Tag"; //$NON-NLS-1$

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		super.visit(compilationUnit);
		verifyImport(compilationUnit, ORG_JUNIT_JUPITER_API_TAG); // $NON-NLS-1$

		AnnotationCollectorVisitor annotationCollectorVisitor = new AnnotationCollectorVisitor();
		compilationUnit.accept(annotationCollectorVisitor);
		List<Annotation> categoryAnnotations = annotationCollectorVisitor.getAnnotations()
			.stream()
			.filter(this::isCategoryAnnotation)
			.collect(Collectors.toList());

		List<JUnit4CategoryReplacementData> categoryAnnotationReplacementdataList = categoryAnnotations.stream()
			.map(this::findJUnit4CategoryReplacementData)
			.filter(Optional::isPresent)
			.map(Optional::get)
			.collect(Collectors.toList());

		findUnusedCategoryImports(compilationUnit, categoryAnnotations, categoryAnnotationReplacementdataList)
			.ifPresent(unusedCategoryImports -> unusedCategoryImports
				.forEach(importDeclaration -> astRewrite.remove(importDeclaration, null)));
		categoryAnnotationReplacementdataList.forEach(this::replaceCategoryAnnotation);

		return false;
	}

	private boolean isCategoryAnnotation(Annotation annotation) {
		IAnnotationBinding annotationBinding = annotation.resolveAnnotationBinding();
		ITypeBinding typeBinding = annotationBinding.getAnnotationType();
		return ClassRelationUtil.isContentOfType(typeBinding, ORG_JUNIT_EXPERIMENTAL_CATEGORIES_CATEGORY);
	}

	private Optional<JUnit4CategoryReplacementData> findJUnit4CategoryReplacementData(Annotation categoryAnnotation) {

		if (categoryAnnotation.getLocationInParent() == MethodDeclaration.MODIFIERS2_PROPERTY) {
			MethodDeclaration methodDeclaration = (MethodDeclaration) categoryAnnotation.getParent();
			if (isValidJUnitTestMethod(methodDeclaration)) {
				return findJUnit4CategoryReplacementData(categoryAnnotation, MethodDeclaration.MODIFIERS2_PROPERTY);
			}
		} else if (categoryAnnotation.getLocationInParent() == TypeDeclaration.MODIFIERS2_PROPERTY) {
			TypeDeclaration typeDeclaration = (TypeDeclaration) categoryAnnotation.getParent();
			if (!typeDeclaration.isInterface() && !typeDeclaration.isLocalTypeDeclaration()) {
				return findJUnit4CategoryReplacementData(categoryAnnotation, TypeDeclaration.MODIFIERS2_PROPERTY);
			}
		}
		return Optional.empty();
	}

	private Optional<JUnit4CategoryReplacementData> findJUnit4CategoryReplacementData(Annotation categoryAnnotation,
			ChildListPropertyDescriptor locationInParent) {

		Expression value = findCategoryAnnotationValue(categoryAnnotation).orElse(null);
		if (value != null) {
			if (value.getNodeType() == ASTNode.TYPE_LITERAL) {
				TypeLiteral typeLiteral = (TypeLiteral) value;
				String qualifiedName = typeLiteral.getType()
					.resolveBinding()
					.getQualifiedName();
				return Optional.of(new JUnit4CategoryReplacementData(categoryAnnotation, Arrays.asList(qualifiedName),
						locationInParent));
			}

			if (value.getNodeType() == ASTNode.ARRAY_INITIALIZER) {
				ArrayInitializer arrayInitializer = (ArrayInitializer) value;
				List<Expression> expressions = ASTNodeUtil.convertToTypedList(arrayInitializer.expressions(),
						Expression.class);
				List<String> categoryNames = mapToTypeLiterals(expressions);
				if (categoryNames.size() == expressions.size()) {
					return Optional
						.of(new JUnit4CategoryReplacementData(categoryAnnotation, categoryNames, locationInParent));
				}
			}
		}
		return Optional.empty();
	}

	private boolean isValidJUnitTestMethod(MethodDeclaration methodDeclaration) {
		if (methodDeclaration.isConstructor()) {
			return false;
		}
		return ASTNodeUtil
			.convertToTypedList(methodDeclaration.modifiers(), IExtendedModifier.class)
			.stream()
			.filter(IExtendedModifier::isAnnotation)
			.map(Annotation.class::cast)
			.map(Annotation::resolveAnnotationBinding)
			.map(IAnnotationBinding::getAnnotationType)
			.filter(typeBinding -> ClassRelationUtil.isContentOfType(typeBinding, "org.junit.Test") || //$NON-NLS-1$
					ClassRelationUtil.isContentOfType(typeBinding, "org.junit.jupiter.api.Test")) //$NON-NLS-1$
			.count() == 1;
	}

	private Optional<Expression> findCategoryAnnotationValue(Annotation categoryAnnotation) {

		if (categoryAnnotation.getNodeType() == ASTNode.SINGLE_MEMBER_ANNOTATION) {
			SingleMemberAnnotation singleMemberAnnotation = (SingleMemberAnnotation) categoryAnnotation;
			return Optional.of(singleMemberAnnotation.getValue());
		}
		if (categoryAnnotation.getNodeType() == ASTNode.NORMAL_ANNOTATION) {
			NormalAnnotation normalAnnotation = (NormalAnnotation) categoryAnnotation;
			List<MemberValuePair> values = ASTNodeUtil.convertToTypedList(normalAnnotation.values(),
					MemberValuePair.class);
			if (values.size() == 1) {
				MemberValuePair memberValuePair = values.get(0);
				return Optional.of(memberValuePair.getValue());
			}
		}
		return Optional.empty();
	}

	private List<String> mapToTypeLiterals(List<Expression> expressions) {
		return expressions.stream()
			.filter(expression -> expression.getNodeType() == ASTNode.TYPE_LITERAL)
			.map(TypeLiteral.class::cast)
			.map(TypeLiteral::getType)
			.map(Type::resolveBinding)
			.map(ITypeBinding::getQualifiedName)
			.collect(Collectors.toList());
	}

	private Optional<List<ImportDeclaration>> findUnusedCategoryImports(CompilationUnit compilationUnit,
			List<Annotation> categoryAnnotations,
			List<JUnit4CategoryReplacementData> categoryAnnotationReplacementdataList) {

		boolean allAnnotationsTransformed = categoryAnnotations.size() == categoryAnnotationReplacementdataList.size();
		if (allAnnotationsTransformed) {
			FirstSimpleTypeOccurrenceVisitor simpleTypeReferencingImportVisitor = new FirstSimpleTypeOccurrenceVisitor(
					ORG_JUNIT_EXPERIMENTAL_CATEGORIES_CATEGORY);
			compilationUnit.accept(simpleTypeReferencingImportVisitor);

			if (!simpleTypeReferencingImportVisitor.isSimpleTypeReferencingImport()) {
				List<ImportDeclaration> unusedCategoryImports = ASTNodeUtil
					.convertToTypedList(compilationUnit.imports(), ImportDeclaration.class)
					.stream()
					.filter(importDeclaration -> importDeclaration.getName()
						.getFullyQualifiedName()
						.equals(ORG_JUNIT_EXPERIMENTAL_CATEGORIES_CATEGORY))
					.collect(Collectors.toList());
				return Optional.of(unusedCategoryImports);
			}
		}
		return Optional.empty();
	}

	private void replaceCategoryAnnotation(JUnit4CategoryReplacementData replacementData) {

		Annotation categoryAnnotation = replacementData.getCategoryAnnotation();
		ChildListPropertyDescriptor locationInParent = replacementData.getLocationInParent();
		List<String> categoryNames = replacementData.getCategoryNames();

		ListRewrite listRewrite = astRewrite.getListRewrite(categoryAnnotation.getParent(), locationInParent);
		Annotation previousAnnotation = categoryAnnotation;
		for (String categoryName : categoryNames) {
			SingleMemberAnnotation newTagAnnotation = createTagAnnotation(categoryAnnotation, categoryName);
			listRewrite.insertAfter(newTagAnnotation, previousAnnotation, null);
			previousAnnotation = newTagAnnotation;
		}
		listRewrite.remove(categoryAnnotation, null);
		onRewrite();
	}

	private SingleMemberAnnotation createTagAnnotation(Annotation categoryAnnotation, String categoryName) {
		AST ast = astRewrite.getAST();
		SingleMemberAnnotation tagAnnotation = ast.newSingleMemberAnnotation();
		Name typeName = addImport(ORG_JUNIT_JUPITER_API_TAG, categoryAnnotation);
		StringLiteral stringLiteral = ast.newStringLiteral();
		stringLiteral.setLiteralValue(categoryName);
		tagAnnotation.setTypeName(typeName);
		tagAnnotation.setValue(stringLiteral);
		return tagAnnotation;
	}
}