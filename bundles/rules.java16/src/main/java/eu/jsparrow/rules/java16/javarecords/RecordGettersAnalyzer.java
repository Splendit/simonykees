package eu.jsparrow.rules.java16.javarecords;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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

/**
 * In a class declaration it is possible that methods are declared with the same
 * name as one of the field of the given class.
 * <p>
 * For example, bothe a field like
 * 
 * <pre>
 * private final int x;
 * </pre>
 * 
 * and a method like
 * 
 * <pre>
 * public int x() {
 * 	// ...
 * }
 * </pre>
 * 
 * are possible in the same class.
 * <p>
 * Method declarations like this must fulfill certain conditions, otherwise it
 * is not possible to replace the given class declaration by a record
 * declaration.
 * 
 * @since 4.5.0
 *
 */
class RecordGettersAnalyzer {

	private List<MethodDeclaration> recordGettersToRemove = new ArrayList<>();

	boolean analyzeRecordGetters(List<MethodDeclaration> methodDeclarations,
			List<SingleVariableDeclaration> canonicalConstructorParameters) {

		Map<SingleVariableDeclaration, MethodDeclaration> parameterToRecordGetterMapEntries = collectParameterToRecordGetterMap(
				methodDeclarations, canonicalConstructorParameters);

		for (Entry<SingleVariableDeclaration, MethodDeclaration> entry : parameterToRecordGetterMapEntries.entrySet()) {
			SingleVariableDeclaration parameter = entry.getKey();
			MethodDeclaration recordGetter = entry.getValue();

			if (hasAnnotationOtherThanOverride(recordGetter)) {
				return false;
			}

			if (Modifier.isStatic(recordGetter.getModifiers())) {
				return false;
			}
			if (isRecordGetterToRemove(recordGetter, parameter)) {
				recordGettersToRemove.add(recordGetter);
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

	private boolean hasAnnotationOtherThanOverride(MethodDeclaration recordGetter) {
		return ASTNodeUtil.convertToTypedList(recordGetter.modifiers(), Annotation.class)
			.stream()
			.map(Annotation::resolveTypeBinding)
			.anyMatch(
					typeBinding -> !ClassRelationUtil.isContentOfType(typeBinding, java.lang.Override.class.getName()));
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

	List<MethodDeclaration> getRecordGettersToRemove() {
		return recordGettersToRemove;
	}
}
