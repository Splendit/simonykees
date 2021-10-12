package eu.jsparrow.rules.java16.javarecords;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;

public class CanonicalConstructorAnalyzer {

	private boolean removeCanonicalConstructor;

	boolean isCanonicalConstructor(MethodDeclaration methodDeclaration,
			List<RecordComponentData> recordComponentDataList) {
		if (!methodDeclaration.isConstructor()) {
			return false;
		}
		List<SingleVariableDeclaration> formalParameters = ASTNodeUtil
			.convertToTypedList(methodDeclaration.parameters(), SingleVariableDeclaration.class);

		if (!matchFormalParametersWithComponentDataList(formalParameters, recordComponentDataList)) {
			return false;
		}

		Block constructorBody = methodDeclaration.getBody();
		List<Assignment> assignments = new ArrayList<>();
		List<Statement> additionalStatements = new ArrayList<>();

		ASTNodeUtil.convertToTypedList(constructorBody.statements(), Statement.class)
			.forEach(statement -> {
				Assignment assignment = findAssignmentAsChild(statement).orElse(null);
				if (assignment != null) {
					assignments.add(assignment);
				} else {
					additionalStatements.add(statement);
				}
			});

		if (!matchAssignmentsWithFormalParameters(assignments, formalParameters)) {
			return false;
		}
		removeCanonicalConstructor = additionalStatements.isEmpty();

		return true;
	}

	private Optional<Assignment> findAssignmentAsChild(Statement statement) {
		if (statement.getNodeType() != ASTNode.EXPRESSION_STATEMENT) {
			return Optional.empty();
		}
		Expression expression = ((ExpressionStatement) statement).getExpression();
		if (expression.getNodeType() != ASTNode.ASSIGNMENT) {
			return Optional.empty();
		}
		Assignment assignment = (Assignment) expression;
		return Optional.of(assignment);
	}

	private boolean matchFormalParametersWithComponentDataList(List<SingleVariableDeclaration> formalParameters,
			List<RecordComponentData> recordComponentDataList) {
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

	private boolean matchAssignmentsWithFormalParameters(List<Assignment> assignments,
			List<SingleVariableDeclaration> formalParameters) {
		for (Assignment assignment : assignments) {
			Expression leftHandSide = assignment.getLeftHandSide();
			if (leftHandSide.getNodeType() != ASTNode.FIELD_ACCESS) {

			}
		}
		return true;
	}

	public boolean canRemoveCanonicalConstructor() {
		return removeCanonicalConstructor;
	}
}
