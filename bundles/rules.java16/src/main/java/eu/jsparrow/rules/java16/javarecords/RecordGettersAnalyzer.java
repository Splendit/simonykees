package eu.jsparrow.rules.java16.javarecords;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;

class RecordGettersAnalyzer {

	private List<MethodDeclaration> recordGetterstoRemove = new ArrayList<>();

	boolean analyzeRecordGetters(List<MethodDeclaration> methodDeclarations,
			List<SingleVariableDeclaration> canonicalConstructorParameters) {

		for (SingleVariableDeclaration parameter : canonicalConstructorParameters) {
			String parameterIdentifier = parameter.getName()
				.getIdentifier();
			MethodDeclaration recordGetter = methodDeclarations.stream()
				.filter(method -> method.getName()
					.getIdentifier()
					.equals(parameterIdentifier)
						&& method.parameters()
							.isEmpty())
				.findFirst()
				.orElse(null);

			if (recordGetter != null) {
				if (Modifier.isStatic(recordGetter.getModifiers())) {
					return false;
				}
				if (isRecordGetterToRemove(recordGetter, parameterIdentifier)) {
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
		}
		return true;
	}

	private boolean isRecordGetterToRemove(MethodDeclaration methodDeclaration,
			String componentIdentifier) {

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

	public List<MethodDeclaration> getRecordGetterstoRemove() {
		return recordGetterstoRemove;
	}
}
