package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.IVariableBinding;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.visitor.helper.LocalVariableUsagesVisitor;

class ThrowingRunnableArgumentAnalyzer {
	private Type localVariableTypeToReplace;

	boolean analyze(MethodDeclaration surroundingMethodDeclaration,
			List<Expression> arguments) {
		int throwingRunnableArgumentIndex = arguments.size() - 1;
		Expression throwingRunnableArgument = arguments.get(throwingRunnableArgumentIndex);
		if (isSupportedThrowingRunnableExpression(throwingRunnableArgument)) {
			return true;
		}

		if (throwingRunnableArgument.getNodeType() != ASTNode.SIMPLE_NAME) {
			return false;
		}
		SimpleName throwingRunnableVariableName = (SimpleName) throwingRunnableArgument;
		IBinding binding = throwingRunnableVariableName.resolveBinding();
		if (binding.getKind() != IBinding.VARIABLE) {
			return false;
		}
		IVariableBinding variableBinding = (IVariableBinding) binding;
		if (variableBinding.isField()) {
			return false;
		}
		if (variableBinding.isParameter()) {
			return false;
		}
		CompilationUnit compilationUnit = ASTNodeUtil.getSpecificAncestor(throwingRunnableVariableName,
				CompilationUnit.class);
		ASTNode declaringNode = compilationUnit.findDeclaringNode(variableBinding);
		if (declaringNode == null) {
			return false;
		}
		if (declaringNode.getNodeType() != ASTNode.VARIABLE_DECLARATION_FRAGMENT) {
			return false;
		}
		VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) declaringNode;
		Expression initializer = variableDeclarationFragment.getInitializer();

		if (initializer != null && !isSupportedThrowingRunnableExpression(initializer)) {
			return false;
		}
		if (variableDeclarationFragment.getLocationInParent() != VariableDeclarationStatement.FRAGMENTS_PROPERTY) {
			return false;
		}
		VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) variableDeclarationFragment
			.getParent();
		if (variableDeclarationStatement.fragments()
			.size() != 1) {
			return false;
		}
		if (!analyzeThrowingRunnableUsages(variableDeclarationFragment, throwingRunnableVariableName,
				surroundingMethodDeclaration)) {
			return false;
		}
		localVariableTypeToReplace = variableDeclarationStatement.getType();
		return true;
	}

	private boolean isSupportedThrowingRunnableExpression(Expression initializer) {
		return initializer.getNodeType() == ASTNode.NULL_LITERAL
				|| initializer.getNodeType() == ASTNode.LAMBDA_EXPRESSION;
	}

	private boolean analyzeThrowingRunnableUsages(VariableDeclarationFragment variableDeclarationFragment,
			SimpleName nameAsAssertionArgument,
			MethodDeclaration surroundingMethodDeclaration) {

		LocalVariableUsagesVisitor localVariableUsagesVisitor = new LocalVariableUsagesVisitor(
				nameAsAssertionArgument);
		surroundingMethodDeclaration.accept(localVariableUsagesVisitor);

		List<SimpleName> usages = localVariableUsagesVisitor.getUsages();
		SimpleName nameAtDeclaration = variableDeclarationFragment.getName();
		return usages.contains(nameAtDeclaration) && usages.contains(nameAsAssertionArgument)
				&& usages.stream()
					.filter(usage -> usage != nameAtDeclaration)
					.filter(usage -> usage != nameAsAssertionArgument)
					.allMatch(this::isLeftHandSideOfSupportedAssignment);
	}

	private boolean isLeftHandSideOfSupportedAssignment(SimpleName usage) {
		if (usage.getLocationInParent() != Assignment.LEFT_HAND_SIDE_PROPERTY) {
			return false;
		}
		Assignment assignment = (Assignment) usage.getParent();
		return isSupportedThrowingRunnableExpression(assignment.getRightHandSide());
	}

	Optional<Type> getLocalVariableTypeToReplace() {
		return Optional.ofNullable(localVariableTypeToReplace);
	}
}