package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.visitor.helper.LocalVariableUsagesVisitor;

class ThrowingRunnableArgumentAnalyzer {
	private Type localVariableTypeToReplace;

	boolean analyze(MethodDeclaration surroundingMethodDeclaration,
			List<Expression> arguments) {
		int throwingRunnableArgumentIndex = arguments.size() - 1;
		Expression throwingRunnableArgument = arguments.get(throwingRunnableArgumentIndex);
		if (throwingRunnableArgument.getNodeType() == ASTNode.LAMBDA_EXPRESSION) {
			return true;
		}
		if (throwingRunnableArgument.getNodeType() != ASTNode.SIMPLE_NAME) {
			return false;
		}

		SimpleName throwingRunnableVariableName = (SimpleName) throwingRunnableArgument;
		IBinding binding = throwingRunnableVariableName.resolveBinding();
		CompilationUnit compilationUnit = ASTNodeUtil.getSpecificAncestor(throwingRunnableVariableName,
				CompilationUnit.class);
		ASTNode declaringNode = compilationUnit.findDeclaringNode(binding);
		if (declaringNode == null) {
			return false;
		}
		if (declaringNode.getNodeType() != ASTNode.VARIABLE_DECLARATION_FRAGMENT) {
			return false;
		}
		VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) declaringNode;
		Expression initializer = variableDeclarationFragment.getInitializer();
		if (initializer == null) {
			return false;
		}
		if (initializer.getNodeType() != ASTNode.LAMBDA_EXPRESSION) {
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

		LocalVariableUsagesVisitor localVariableUsagesVisitor = new LocalVariableUsagesVisitor(
				throwingRunnableVariableName);
		surroundingMethodDeclaration.accept(localVariableUsagesVisitor);
		if (localVariableUsagesVisitor.getUsages()
			.size() != 2) {
			return false;
		}
		if (!localVariableUsagesVisitor.getUsages()
			.contains(throwingRunnableVariableName)) {
			return false;
		}

		if (!localVariableUsagesVisitor.getUsages()
			.contains(variableDeclarationFragment.getName())) {
			return false;
		}

		localVariableTypeToReplace = variableDeclarationStatement.getType();
		return true;
	}

	Optional<Type> getLocalVariableTypeToReplace() {
		return Optional.ofNullable(localVariableTypeToReplace);
	}
}