package eu.jsparrow.rules.java16.javarecords;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;

public class BodyDeclarationsAnalyzer {

	Optional<BodyDeclarationsAnalysisResult> analyzeBodyDeclarations(TypeDeclaration typeDeclaration) {

		List<BodyDeclaration> bodyDeclarations = ASTNodeUtil
			.convertToTypedList(typeDeclaration.bodyDeclarations(), BodyDeclaration.class);
		ArrayList<MethodDeclaration> methods = new ArrayList<>();
		ArrayList<FieldDeclaration> staticFields = new ArrayList<>();
		ArrayList<FieldDeclaration> privateFinalInstanceFields = new ArrayList<>();

		for (BodyDeclaration declaration : bodyDeclarations) {
			if (declaration.getNodeType() == ASTNode.METHOD_DECLARATION) {
				methods.add((MethodDeclaration) declaration);
			} else if (declaration.getNodeType() == ASTNode.FIELD_DECLARATION) {
				FieldDeclaration fieldDeclaration = (FieldDeclaration) declaration;
				int modifiers = fieldDeclaration.getModifiers();
				if (Modifier.isStatic(modifiers)) {
					staticFields.add((FieldDeclaration) declaration);
				} else if (Modifier.isPrivate(modifiers) && Modifier.isFinal(modifiers)) {
					privateFinalInstanceFields.add((FieldDeclaration) declaration);
				} else {
					return Optional.empty();
				}
			} else {
				return Optional.empty();
			}
		}

		MethodDeclaration assumedCanonicalConstructor = findAssumedCanonicalConstructor(methods)
			.orElse(null);
		if (assumedCanonicalConstructor == null) {
			return Optional.empty();
		}
		List<SingleVariableDeclaration> canonicalConstructorParameters = ASTNodeUtil
			.convertToTypedList(assumedCanonicalConstructor.parameters(), SingleVariableDeclaration.class);

		if (!checkInstanceFieldsMatchingConstructorParameters(privateFinalInstanceFields,
				canonicalConstructorParameters)) {
			return Optional.empty();
		}

		if (canRemoveCanonicalConstructor(assumedCanonicalConstructor, canonicalConstructorParameters)) {
			methods.remove(assumedCanonicalConstructor);
		}
		methods.removeAll(collectRecordGettersToRemove(methods, canonicalConstructorParameters));

		ArrayList<BodyDeclaration> recordBodyDeclarations = new ArrayList<>();
		recordBodyDeclarations.addAll(staticFields);
		recordBodyDeclarations.addAll(methods);
		return Optional.of(new BodyDeclarationsAnalysisResult(typeDeclaration, canonicalConstructorParameters,
				recordBodyDeclarations));
	}

	private boolean checkInstanceFieldsMatchingConstructorParameters(List<FieldDeclaration> instanceFields,
			List<SingleVariableDeclaration> formalParameters) {

		Map<String, ITypeBinding> fieldNameToTypeMap = new HashMap<>();
		for (FieldDeclaration field : instanceFields) {
			List<VariableDeclarationFragment> fragments = ASTNodeUtil.convertToTypedList(field.fragments(),
					VariableDeclarationFragment.class);
			if (fragments.stream()
				.map(VariableDeclarationFragment::getInitializer)
				.anyMatch(Objects::nonNull)) {
				return false;
			}
			fragments.stream()
				.map(VariableDeclarationFragment::getName)
				.map(SimpleName::getIdentifier)
				.forEach(identifier -> fieldNameToTypeMap.put(identifier, field.getType()
					.resolveBinding()));
		}

		if (formalParameters.size() != fieldNameToTypeMap.size()) {
			return false;
		}

		for (SingleVariableDeclaration parameter : formalParameters) {
			String parameterIdentifier = parameter.getName()
				.getIdentifier();
			if (!fieldNameToTypeMap.containsKey(parameterIdentifier)) {
				return false;
			}
			ITypeBinding fieldTypeBinding = fieldNameToTypeMap.get(parameterIdentifier);
			if (!ClassRelationUtil.compareITypeBinding(fieldTypeBinding, parameter.getType()
				.resolveBinding())) {
				return false;
			}
		}
		return true;
	}

	private Optional<MethodDeclaration> findAssumedCanonicalConstructor(List<MethodDeclaration> methodDeclarations) {
		List<MethodDeclaration> constructorsWithoutThisInvocation = methodDeclarations
			.stream()
			.filter(MethodDeclaration::isConstructor)
			.filter(constructor -> ASTNodeUtil.convertToTypedList(constructor.getBody()
				.statements(), ConstructorInvocation.class)
				.isEmpty())
			.collect(Collectors.toList());

		if (constructorsWithoutThisInvocation.size() != 1) {
			return Optional.empty();
		}
		MethodDeclaration assumedCanonicalConstructor = constructorsWithoutThisInvocation.get(0);
		return Optional.of(assumedCanonicalConstructor);
	}

	private boolean canRemoveCanonicalConstructor(MethodDeclaration canonicalConstructor,
			List<SingleVariableDeclaration> formalParameters) {

		List<Assignment> assignments = ASTNodeUtil.returnTypedList(canonicalConstructor.getBody()
			.statements(), ExpressionStatement.class)
			.stream()
			.map(ExpressionStatement::getExpression)
			.filter(Assignment.class::isInstance)
			.map(Assignment.class::cast)
			.collect(Collectors.toList());

		if (assignments.size() != formalParameters.size()) {
			return false;
		}
		List<String> componentIdentifiers = formalParameters
			.stream()
			.map(SingleVariableDeclaration::getName)
			.map(SimpleName::getIdentifier)
			.collect(Collectors.toList());

		for (String identifier : componentIdentifiers) {
			if (assignments
				.stream()
				.noneMatch(assignment -> isAssigningParameterToFieldWithSameName(assignment, identifier))) {
				return false;
			}
		}
		return true;
	}

	private boolean isAssigningParameterToFieldWithSameName(Assignment assignment, String expectedIdentifier) {
		Expression leftHandSide = assignment.getLeftHandSide();
		if (leftHandSide.getNodeType() != ASTNode.FIELD_ACCESS) {
			return false;
		}
		FieldAccess fieldAccess = (FieldAccess) leftHandSide;
		if (fieldAccess.getExpression()
			.getNodeType() != ASTNode.THIS_EXPRESSION) {
			return false;
		}
		String fieldNameIdentifier = fieldAccess.getName()
			.getIdentifier();
		if (!fieldNameIdentifier.equals(expectedIdentifier)) {
			return false;
		}
		Expression rightHandSide = assignment.getRightHandSide();
		if (rightHandSide.getNodeType() != ASTNode.SIMPLE_NAME) {
			return false;
		}
		String parameterIdentifier = ((SimpleName) rightHandSide).getIdentifier();
		return parameterIdentifier.equals(expectedIdentifier);
	}

	private List<MethodDeclaration> collectRecordGettersToRemove(List<MethodDeclaration> methodDeclarations,
			List<SingleVariableDeclaration> formalParameters) {
		return methodDeclarations.stream()
			.filter(methodDeclaration -> isRecordGetterToRemove(methodDeclaration, formalParameters))
			.collect(Collectors.toList());
	}

	private boolean isRecordGetterToRemove(MethodDeclaration methodDeclaration,
			List<SingleVariableDeclaration> formalParameters) {
		return !methodDeclaration.isConstructor();
	}

}
