package eu.jsparrow.rules.java16.javarecords;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;

class RecordGettersAnalyzer {

	private List<MethodDeclaration> recordGetterstoRemove = new ArrayList<>();

	boolean analyzeRecordGetters(List<MethodDeclaration> methodDeclarations,
			List<SingleVariableDeclaration> canonicalConstructorParameters) {

		Set<Entry<SingleVariableDeclaration, MethodDeclaration>> parameterToRecordGetterMapEntries = collectParameterToRecordGetterMap(
				methodDeclarations, canonicalConstructorParameters)
					.entrySet();

		for (Entry<SingleVariableDeclaration, MethodDeclaration> entry : parameterToRecordGetterMapEntries) {
			SingleVariableDeclaration parameter = entry.getKey();
			MethodDeclaration recordGetter = entry.getValue();

			List<Annotation> annotations = ASTNodeUtil.convertToTypedList(recordGetter.modifiers(),
					Annotation.class);
			if (!annotations.isEmpty()) {
				return false;
			}
			if (Modifier.isStatic(recordGetter.getModifiers())) {
				return false;
			}
			if (isRecordGetterToRemove(recordGetter, parameter)) {
				recordGetterstoRemove.add(recordGetter);
			} else if (!Modifier.isPublic(recordGetter.getModifiers())) {
				return false;
			}
			ITypeBinding returnType = recordGetter.resolveBinding()
				.getReturnType();
			ITypeBinding parameterTypeBinding = parameter.getType()
				.resolveBinding();
			if (!ClassRelationUtil.compareITypeBinding(returnType, parameterTypeBinding)) {
				return false;
			}
		}

		return true;
	}

	private Map<SingleVariableDeclaration, MethodDeclaration> collectParameterToRecordGetterMap(
			List<MethodDeclaration> methodDeclarations,
			List<SingleVariableDeclaration> canonicalConstructorParameters) {
		Map<SingleVariableDeclaration, MethodDeclaration> parameterToRecordGetterMap = new HashMap<>();
		for (SingleVariableDeclaration parameter : canonicalConstructorParameters) {
			String parameterIdentifier = parameter.getName()
				.getIdentifier();
			methodDeclarations.stream()
				.filter(method -> method.getName()
					.getIdentifier()
					.equals(parameterIdentifier)
						&& method.parameters()
							.isEmpty())
				.findFirst()
				.ifPresent(componentGetter -> parameterToRecordGetterMap.put(parameter, componentGetter));
		}
		return parameterToRecordGetterMap;
	}

	private boolean isRecordGetterToRemove(MethodDeclaration methodDeclaration, SingleVariableDeclaration parameter) {

		String componentIdentifier = parameter.getName()
			.getIdentifier();

		List<ReturnStatement> returnStatements = ASTNodeUtil.returnTypedList(methodDeclaration.getBody()
			.statements(), ReturnStatement.class);
		if (returnStatements.isEmpty()) {
			return false;
		}

		ReturnStatement returnStatement = returnStatements.get(0);
		Expression returnedExpression = returnStatement.getExpression();
		if (returnedExpression == null) {
			return false;
		}

		if (returnedExpression.getNodeType() == ASTNode.SIMPLE_NAME) {
			return ((SimpleName) returnedExpression).getIdentifier()
				.equals(componentIdentifier);
		}
		return BodyDeclarationsAnalyzer.isThisFieldAccessMatchingIdentifier(returnedExpression, componentIdentifier);
	}

	List<MethodDeclaration> getRecordGetterstoRemove() {
		return recordGetterstoRemove;
	}
}
