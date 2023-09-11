package eu.jsparrow.core.visitor.impl.inline;

import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

/**
 * Helper class to find out whether or not it is possible and reasonable to
 * in-line a reference on a local variable, provided that the local variable is
 * used exactly once.
 * 
 */
class SupportedReferenceAnalyzer {
	private final Statement statementToInlineReference;
	private final Expression initializerToReplaceReference;

	static Optional<VariableDeclarationStatement> findVariableDeclarationStatementWithSingleFragment(
			VariableDeclarationFragment declarationFragment) {

		if (declarationFragment.getLocationInParent() != VariableDeclarationStatement.FRAGMENTS_PROPERTY) {
			return Optional.empty();
		}

		VariableDeclarationStatement declarationStatement = (VariableDeclarationStatement) declarationFragment
			.getParent();

		return Optional.of(declarationStatement)
			.filter(variableDeclarationStatement -> SimpleStructureHelper
				.isSingletonList(declarationStatement.fragments()));

	}

	SupportedReferenceAnalyzer(Statement statementToInlineReference,
			Expression initializerToReplaceReference) {
		this.statementToInlineReference = statementToInlineReference;
		this.initializerToReplaceReference = initializerToReplaceReference;
	}

	boolean isSupportedReference(SimpleName reference) {

		if (analyzeLocationInParent(reference)) {
			return true;
		}

		if (!SimpleStructureHelper.isSimpleInitializer(initializerToReplaceReference, true)) {
			return false;
		}

		Expression expressionEnclosingReference = findSupportedParentExpression(reference).orElse(null);
		if (expressionEnclosingReference == null) {
			return false;
		}

		return analyzeLocationInParent(expressionEnclosingReference);
	}

	private boolean analyzeLocationInParent(Expression expressionWithUsage) {

		ASTNode parent = expressionWithUsage.getParent();
		if (parent == statementToInlineReference) {
			if (expressionWithUsage.getLocationInParent() == ReturnStatement.EXPRESSION_PROPERTY) {
				return true;
			}

			if (expressionWithUsage.getLocationInParent() == ThrowStatement.EXPRESSION_PROPERTY) {
				return true;
			}

			if (expressionWithUsage.getLocationInParent() == ExpressionStatement.EXPRESSION_PROPERTY) {
				return true;
			}
			return false;
		}

		if (expressionWithUsage.getLocationInParent() == Assignment.RIGHT_HAND_SIDE_PROPERTY) {
			StructuralPropertyDescriptor parentLocationInParent = parent
				.getLocationInParent();
			return parentLocationInParent == ExpressionStatement.EXPRESSION_PROPERTY
					&& parent.getParent() == statementToInlineReference;
		}

		if (expressionWithUsage.getLocationInParent() == VariableDeclarationFragment.INITIALIZER_PROPERTY) {
			VariableDeclarationStatement declarationStatementFound = findVariableDeclarationStatementWithSingleFragment(
					(VariableDeclarationFragment) parent).orElse(null);
			return declarationStatementFound == statementToInlineReference;
		}

		return false;
	}

	private static Optional<Expression> findSupportedParentExpression(SimpleName uniqueUsage) {

		if (uniqueUsage.getLocationInParent() == MethodInvocation.ARGUMENTS_PROPERTY) {
			MethodInvocation methodInvocation = (MethodInvocation) uniqueUsage.getParent();
			if (SimpleStructureHelper.isSimpleMethodInvocation(methodInvocation)) {
				return Optional.of(methodInvocation);
			}
			return Optional.empty();
		}

		if (uniqueUsage.getLocationInParent() == SuperMethodInvocation.ARGUMENTS_PROPERTY) {
			SuperMethodInvocation superMethodInvocation = (SuperMethodInvocation) uniqueUsage.getParent();
			if (SimpleStructureHelper.isSimpleSuperMethodInvocation(superMethodInvocation)) {
				return Optional.of(superMethodInvocation);
			}
			return Optional.empty();
		}

		if (uniqueUsage.getLocationInParent() == ClassInstanceCreation.ARGUMENTS_PROPERTY) {
			ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) uniqueUsage.getParent();
			if (SimpleStructureHelper.isSimpleClassInstanceCreation(classInstanceCreation)) {
				return Optional.of(classInstanceCreation);
			}
			return Optional.empty();
		}

		return Optional.empty();
	}
}
