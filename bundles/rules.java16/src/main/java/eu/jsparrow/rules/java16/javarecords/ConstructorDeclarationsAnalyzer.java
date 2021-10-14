package eu.jsparrow.rules.java16.javarecords;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;

public class ConstructorDeclarationsAnalyzer {

	private final TypeDeclaration typeDeclaration;
	private final List<RecordComponentData> recordComponentDataList;

	private MethodDeclaration canonicalConstructorToRemove;

	public ConstructorDeclarationsAnalyzer(TypeDeclaration typeDeclaration,
			List<RecordComponentData> recordComponentDataList) {
		this.typeDeclaration = typeDeclaration;
		this.recordComponentDataList = recordComponentDataList;
	}

	boolean analyzeConstructors() {

		List<MethodDeclaration> constructorsWithoutThisInvocation = collectConstructorsWithoutThisInvocation();
		if (constructorsWithoutThisInvocation.size() != 1) {
			return false;
		}

		MethodDeclaration canonicalConstructor = constructorsWithoutThisInvocation.get(0);

		List<SingleVariableDeclaration> formalParameters = ASTNodeUtil
			.convertToTypedList(canonicalConstructor.parameters(), SingleVariableDeclaration.class);

		if (!matchFormalParametersWithComponentDataList(formalParameters)) {
			return false;
		}

		if (canRemoveCanonicalConstructor(canonicalConstructor)) {
			canonicalConstructorToRemove = canonicalConstructor;
		}
		return true;

	}

	private List<MethodDeclaration> collectConstructorsWithoutThisInvocation() {
		return ASTNodeUtil
			.convertToTypedList(typeDeclaration.bodyDeclarations(), MethodDeclaration.class)
			.stream()
			.filter(MethodDeclaration::isConstructor)
			.filter(constructor -> ASTNodeUtil.convertToTypedList(constructor.getBody()
				.statements(), ConstructorInvocation.class)
				.isEmpty())
			.collect(Collectors.toList());
	}

	private boolean matchFormalParametersWithComponentDataList(List<SingleVariableDeclaration> formalParameters) {
		if (formalParameters.size() != recordComponentDataList.size()) {
			return false;
		}
		for (SingleVariableDeclaration formalParameter : formalParameters) {
			if (recordComponentDataList.stream()
				.noneMatch(componentData -> matchFormalParameterWithComponentData(formalParameter, componentData))) {
				return false;
			}
		}
		return true;
	}

	private boolean matchFormalParameterWithComponentData(SingleVariableDeclaration formalParameter,
			RecordComponentData recordComponentData) {

		String formalParameterIdentifier = formalParameter.getName()
			.getIdentifier();
		String componentIdentifier = recordComponentData.getName()
			.getIdentifier();

		if (!formalParameterIdentifier.equals(componentIdentifier)) {
			return false;
		}

		ITypeBinding formalParameterTypeBinding = formalParameter.getType()
			.resolveBinding();
		ITypeBinding recordComponentTypeBinding = recordComponentData.getType()
			.resolveBinding();
		return ClassRelationUtil.compareITypeBinding(formalParameterTypeBinding, recordComponentTypeBinding);
	}

	private boolean canRemoveCanonicalConstructor(MethodDeclaration canonicalConstructor) {

		List<Assignment> assignments = ASTNodeUtil.returnTypedList(canonicalConstructor.getBody()
			.statements(), ExpressionStatement.class)
			.stream()
			.map(ExpressionStatement::getExpression)
			.filter(Assignment.class::isInstance)
			.map(Assignment.class::cast)
			.collect(Collectors.toList());

		if (assignments.size() != recordComponentDataList.size()) {
			return false;
		}
		List<String> componentIdentifiers = recordComponentDataList
			.stream()
			.map(RecordComponentData::getName)
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

	public Optional<MethodDeclaration> getCanonicalConstructorToRemove() {
		return Optional.ofNullable(canonicalConstructorToRemove);
	}

}
