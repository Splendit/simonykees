package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

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
		LocalVariableUsagesVisitor localVariableUsagesVisitor = new LocalVariableUsagesVisitor(
				throwingRunnableVariableName);
		surroundingMethodDeclaration.accept(localVariableUsagesVisitor);
		Predicate<SimpleName> simpleNameEquals = throwingRunnableVariableName::equals;
		List<SimpleName> otherUsages = localVariableUsagesVisitor.getUsages()
			.stream()
			.filter(simpleNameEquals.negate())
			.collect(Collectors.toList());

		if (otherUsages.size() != 1) {
			return false;
		}
		SimpleName otherUsage = otherUsages.get(0);
		ASTNode otherUsageParent = otherUsage.getParent();
		if (otherUsage.getLocationInParent() != VariableDeclarationFragment.NAME_PROPERTY) {
			return false;
		}
		VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) otherUsageParent;
		if (variableDeclarationFragment.getLocationInParent() != VariableDeclarationStatement.FRAGMENTS_PROPERTY) {
			return false;
		}
		VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) variableDeclarationFragment
			.getParent();
		if (variableDeclarationStatement.fragments()
			.size() != 1) {
			return false;
		}
		localVariableTypeToReplace = variableDeclarationStatement.getType();
		return true;
	}

	Optional<Type> getLocalVariableTypeToReplace() {
		return Optional.ofNullable(localVariableTypeToReplace);
	}
}