package eu.jsparrow.core.visitor.junit.jupiter;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeLiteral;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;

public class JUnit4CategoryAnnotationAnalyzer {

	Optional<JUnit4CategoryReplacementData> analyze(Annotation categoryAnnotation) {

		if (categoryAnnotation.getLocationInParent() == MethodDeclaration.MODIFIERS2_PROPERTY) {
			MethodDeclaration methodDeclaration = (MethodDeclaration) categoryAnnotation.getParent();
			if (isValidJUnitTestMethod(methodDeclaration)) {
				return analyze(categoryAnnotation, MethodDeclaration.MODIFIERS2_PROPERTY);
			}
		} else if (categoryAnnotation.getLocationInParent() == TypeDeclaration.MODIFIERS2_PROPERTY) {
			TypeDeclaration typeDeclaration = (TypeDeclaration) categoryAnnotation.getParent();
			if (!typeDeclaration.isInterface() && !typeDeclaration.isLocalTypeDeclaration()) {
				return analyze(categoryAnnotation, TypeDeclaration.MODIFIERS2_PROPERTY);
			}
		}
		return Optional.empty();
	}

	private Optional<JUnit4CategoryReplacementData> analyze(Annotation categoryAnnotation,
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
}