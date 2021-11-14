package eu.jsparrow.rules.java16.javarecords;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
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
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;

class BodyDeclarationsAnalyzer {

	Optional<BodyDeclarationsAnalysisResult> analyzeBodyDeclarations(TypeDeclaration typeDeclaration) {

		List<BodyDeclaration> allSupportedBodyDeclarations = collectAllSupportedBodyDeclarations(typeDeclaration);
		if (allSupportedBodyDeclarations.isEmpty()) {
			return Optional.empty();
		}

		List<FieldDeclaration> privateFinalInstanceFields = collectInstanceFieldsForComponents(typeDeclaration);
		if (privateFinalInstanceFields.isEmpty()) {
			return Optional.empty();
		}

		List<MethodDeclaration> methods = ASTNodeUtil
			.convertToTypedList(typeDeclaration.bodyDeclarations(), MethodDeclaration.class);

		MethodDeclaration assumedCanonicalConstructor = findAssumedCanonicalConstructor(methods)
			.orElse(null);
		if (assumedCanonicalConstructor == null) {
			return Optional.empty();
		}
		List<SingleVariableDeclaration> canonicalConstructorParameters = ASTNodeUtil
			.convertToTypedList(assumedCanonicalConstructor.parameters(), SingleVariableDeclaration.class);
		for (SingleVariableDeclaration parameter : canonicalConstructorParameters) {
			List<Annotation> annotations = ASTNodeUtil.convertToTypedList(parameter.modifiers(), Annotation.class);
			if (!annotations.isEmpty()) {
				return Optional.empty();
			}
		}

		if ((Modifier.isPrivate(assumedCanonicalConstructor.getModifiers())
				&& !Modifier.isPrivate(typeDeclaration.getModifiers()))) {
			return Optional.empty();
		}

		if (!analyzeQualificationForRecordComponents(privateFinalInstanceFields,
				canonicalConstructorParameters)) {
			return Optional.empty();
		}

		RecordGettersAnalyzer recordGettersAnalyzer = new RecordGettersAnalyzer();
		if (!recordGettersAnalyzer.analyzeRecordGetters(methods, canonicalConstructorParameters)) {
			return Optional.empty();
		}
		ArrayList<MethodDeclaration> methodsToRemove = new ArrayList<>();
		methodsToRemove.addAll(recordGettersAnalyzer.getRecordGetterstoRemove());

		boolean canRemoveCanonicalConstructor = canRemoveCanonicalConstructor(assumedCanonicalConstructor,
				canonicalConstructorParameters);
		if (canRemoveCanonicalConstructor) {
			methodsToRemove.add(assumedCanonicalConstructor);
		}
		methods.stream()
			.filter(this::isEqualsMethodToRemove)
			.findFirst()
			.ifPresent(methodsToRemove::add);

		methods.stream()
			.filter(this::isHashCodeMethodToRemove)
			.findFirst()
			.ifPresent(methodsToRemove::add);

		ArrayList<BodyDeclaration> recordBodyDeclarations = new ArrayList<>(allSupportedBodyDeclarations);
		recordBodyDeclarations.removeAll(privateFinalInstanceFields);
		recordBodyDeclarations.removeAll(methodsToRemove);

		return Optional.of(new BodyDeclarationsAnalysisResult(typeDeclaration, canonicalConstructorParameters,
				recordBodyDeclarations));
	}

	private List<FieldDeclaration> collectInstanceFieldsForComponents(TypeDeclaration typeDeclaration) {

		List<FieldDeclaration> privateFinalInstanceFields = new ArrayList<>();
		List<FieldDeclaration> allFields = ASTNodeUtil.convertToTypedList(typeDeclaration.bodyDeclarations(),
				FieldDeclaration.class);
		for (FieldDeclaration field : allFields) {
			int modifiers = field.getModifiers();
			if (!Modifier.isStatic(modifiers)) {
				List<Annotation> annotations = ASTNodeUtil.convertToTypedList(field.modifiers(), Annotation.class);
				if (annotations.isEmpty() && Modifier.isPrivate(modifiers) && Modifier.isFinal(modifiers)) {
					privateFinalInstanceFields.add(field);
				} else {
					return Collections.emptyList();
				}
			}
		}
		return privateFinalInstanceFields;
	}

	List<BodyDeclaration> collectAllSupportedBodyDeclarations(TypeDeclaration typeDeclaration) {
		List<BodyDeclaration> bodyDeclarations = ASTNodeUtil
			.convertToTypedList(typeDeclaration.bodyDeclarations(), BodyDeclaration.class);
		if (!bodyDeclarations.isEmpty() && bodyDeclarations.stream()
			.allMatch(this::isSupportedBodyDeclaration)) {
			return bodyDeclarations;
		}
		return Collections.emptyList();
	}

	private boolean isSupportedBodyDeclaration(BodyDeclaration declaration) {

		if (declaration.getNodeType() == ASTNode.METHOD_DECLARATION
				|| declaration.getNodeType() == ASTNode.FIELD_DECLARATION) {
			return true;
		}
		if (declaration.getNodeType() == ASTNode.INITIALIZER) {
			return Modifier.isStatic(declaration.getModifiers());
		}
		return false;
	}

	private boolean analyzeQualificationForRecordComponents(List<FieldDeclaration> instanceFields,
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
			List<SingleVariableDeclaration> canonicalConstructorParameters) {

		List<Statement> statements = ASTNodeUtil.convertToTypedList(canonicalConstructor.getBody()
			.statements(), Statement.class);

		if (statements.size() != canonicalConstructorParameters.size()) {
			return false;
		}

		for (Statement statement : statements) {
			if (statement.getNodeType() != ASTNode.EXPRESSION_STATEMENT) {
				return false;
			}
			Expression expression = ((ExpressionStatement) statement).getExpression();

			if (expression.getNodeType() != ASTNode.ASSIGNMENT) {
				return false;
			}
			Assignment assignment = (Assignment) expression;

			Expression rightHandSide = assignment.getRightHandSide();
			if (rightHandSide.getNodeType() != ASTNode.SIMPLE_NAME) {
				return false;
			}
			String rightHandSideIdentifier = ((SimpleName) rightHandSide).getIdentifier();
			if (!isThisFieldAccessMatchingIdentifier(assignment.getLeftHandSide(), rightHandSideIdentifier)) {
				return false;
			}
		}
		return true;
	}

	static boolean isThisFieldAccessMatchingIdentifier(Expression expression, String expectedIdentifier) {
		if (expression.getNodeType() != ASTNode.FIELD_ACCESS) {
			return false;
		}
		FieldAccess fieldAccess = (FieldAccess) expression;
		if (fieldAccess.getExpression()
			.getNodeType() != ASTNode.THIS_EXPRESSION) {
			return false;
		}
		String fieldNameIdentifier = fieldAccess.getName()
			.getIdentifier();
		return fieldNameIdentifier.equals(expectedIdentifier);
	}

	private boolean isEqualsMethodToRemove(MethodDeclaration methodDeclaration) {
		if (!methodDeclaration.getName()
			.getIdentifier()
			.equals("equals")) { //$NON-NLS-1$
			return false;
		}
		List<SingleVariableDeclaration> parameters = ASTNodeUtil.convertToTypedList(methodDeclaration.parameters(),
				SingleVariableDeclaration.class);
		if (parameters.size() != 1) {
			return false;
		}
		SingleVariableDeclaration parameter = parameters.get(0);
		return ClassRelationUtil.isContentOfType(parameter.getType()
			.resolveBinding(), java.lang.Object.class.getName());
	}

	private boolean isHashCodeMethodToRemove(MethodDeclaration methodDeclaration) {
		if (!methodDeclaration.getName()
			.getIdentifier()
			.equals("hashCode")) { //$NON-NLS-1$
			return false;
		}
		return methodDeclaration.parameters()
			.isEmpty();

	}
}
