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

import eu.jsparrow.core.visitor.sub.SimpleTypeReferencingImportVisitor;
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

		List<ReplaceJUnit4CategoryData> categoryAnnotationReplacementdataList = categoryAnnotations.stream()
			.map(this::findReplaceJUnit4CategoryData)
			.filter(Optional::isPresent)
			.map(Optional::get)
			.collect(Collectors.toList());

		boolean allAnnotationsTransformed = categoryAnnotations.size() == categoryAnnotationReplacementdataList.size();
		boolean removeCategoryImport;
		if (allAnnotationsTransformed) {
			SimpleTypeReferencingImportVisitor simpleTypeReferencingImportVisitor = new SimpleTypeReferencingImportVisitor(
					ORG_JUNIT_EXPERIMENTAL_CATEGORIES_CATEGORY);
			compilationUnit.accept(simpleTypeReferencingImportVisitor);
			removeCategoryImport = !simpleTypeReferencingImportVisitor
				.isSimpleTypeReferencingImport();
		} else {
			removeCategoryImport = false;
		}

		if (removeCategoryImport) {
			ASTNodeUtil.convertToTypedList(compilationUnit.imports(), ImportDeclaration.class)
				.stream()
				.filter(importDeclaration -> importDeclaration.getName()
					.getFullyQualifiedName()
					.equals(ORG_JUNIT_EXPERIMENTAL_CATEGORIES_CATEGORY))
				.forEach(importDeclaration -> astRewrite.remove(importDeclaration, null));
		}
		categoryAnnotationReplacementdataList.forEach(this::replaceCategoryAnnotation);

		return false;
	}

	private Optional<ReplaceJUnit4CategoryData> findReplaceJUnit4CategoryData(Annotation annotation) {
		ChildListPropertyDescriptor locationInParent = findLocationInParent(annotation).orElse(null);
		if (locationInParent != null) {
			List<String> categoryNames = findCategoryNames(annotation).orElse(null);
			if (categoryNames != null) {
				return Optional.of(new ReplaceJUnit4CategoryData(annotation, categoryNames, locationInParent));
			}
		}
		return Optional.empty();
	}

	private Optional<ChildListPropertyDescriptor> findLocationInParent(Annotation annotation) {
		if (annotation.getLocationInParent() == MethodDeclaration.MODIFIERS2_PROPERTY) {
			return Optional.of(MethodDeclaration.MODIFIERS2_PROPERTY);
		}
		if (annotation.getLocationInParent() == TypeDeclaration.MODIFIERS2_PROPERTY) {
			return Optional.of(TypeDeclaration.MODIFIERS2_PROPERTY);
		}
		return Optional.empty();
	}

	private boolean isCategoryAnnotation(Annotation annotation) {
		IAnnotationBinding annotationBinding = annotation.resolveAnnotationBinding();
		ITypeBinding typeBinding = annotationBinding.getAnnotationType();
		return ClassRelationUtil.isContentOfType(typeBinding, ORG_JUNIT_EXPERIMENTAL_CATEGORIES_CATEGORY);
	}

	private Optional<List<String>> findCategoryNames(Annotation annotation) {

		if (annotation.getNodeType() == ASTNode.SINGLE_MEMBER_ANNOTATION) {
			Expression value = ((SingleMemberAnnotation) annotation).getValue();
			return findCategoryNames(value);
		}
		if (annotation.getNodeType() == ASTNode.NORMAL_ANNOTATION) {
			List<MemberValuePair> values = ASTNodeUtil.convertToTypedList(((NormalAnnotation) annotation).values(),
					MemberValuePair.class);
			if (values.size() == 1) {
				MemberValuePair memberValuePair = values.get(0);
				Expression value = memberValuePair.getValue();
				return findCategoryNames(value);
			}
		}
		return Optional.empty();
	}

	private Optional<List<String>> findCategoryNames(Expression value) {

		if (value.getNodeType() == ASTNode.TYPE_LITERAL) {
			TypeLiteral typeLiteral = (TypeLiteral) value;

			String qualifiedName = typeLiteral.getType()
				.resolveBinding()
				.getQualifiedName();
			return Optional.of(Arrays.asList(qualifiedName));
		}
		if (value.getNodeType() == ASTNode.ARRAY_INITIALIZER) {
			ArrayInitializer arrayInitializer = (ArrayInitializer) value;
			List<Expression> expressions = ASTNodeUtil.convertToTypedList(arrayInitializer.expressions(),
					Expression.class);
			List<String> categoryNames = expressions.stream()
				.filter(expression -> expression.getNodeType() == ASTNode.TYPE_LITERAL)
				.map(TypeLiteral.class::cast)
				.map(TypeLiteral::getType)
				.map(Type::resolveBinding)
				.map(ITypeBinding::getQualifiedName)
				.collect(Collectors.toList());

			if (categoryNames.size() == expressions.size()) {
				return Optional.of(categoryNames);
			}
		}
		return Optional.empty();
	}

	private void replaceCategoryAnnotation(ReplaceJUnit4CategoryData replacementData) {

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
