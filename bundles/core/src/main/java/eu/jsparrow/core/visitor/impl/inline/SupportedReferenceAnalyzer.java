package eu.jsparrow.core.visitor.impl.inline;

import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import eu.jsparrow.rules.common.util.ASTNodeUtil;

/**
 * Helper class to find out whether or not it is possible and reasonable to
 * in-line a reference on a local variable, provided that the local variable is
 * used exactly once.
 * 
 */
class SupportedReferenceAnalyzer {
	private final Statement statementFollowingDeclaration;
	private final Expression initializerOfDeclaration;

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

	SupportedReferenceAnalyzer(Statement statementFollowingDeclaration,
			Expression initializerOfDeclaration) {
		this.statementFollowingDeclaration = statementFollowingDeclaration;
		this.initializerOfDeclaration = initializerOfDeclaration;
	}

	boolean isSupportedReference(SimpleName reference) {

		
		ASTNode parent = reference.getParent();
		if (parent == statementFollowingDeclaration) {
			if (reference.getLocationInParent() == ReturnStatement.EXPRESSION_PROPERTY) {
				return true;
			}

			if (reference.getLocationInParent() == ThrowStatement.EXPRESSION_PROPERTY) {
				return true;
			}
		}
		return false;

		/*
		if (!isConstantInitializer(initializerOfDeclaration)) {
			return false;
		}

		if (reference.getLocationInParent() == MethodInvocation.ARGUMENTS_PROPERTY) {
			MethodInvocation methodInvocation = (MethodInvocation) reference.getParent();
			return SimpleStructureHelper.isSingletonList(methodInvocation.arguments());
		}

		if (reference.getLocationInParent() == SuperMethodInvocation.ARGUMENTS_PROPERTY) {
			SuperMethodInvocation superMethodInvocation = (SuperMethodInvocation) reference.getParent();
			return SimpleStructureHelper.isSingletonList(superMethodInvocation.arguments());
		}

		if (reference.getLocationInParent() == ClassInstanceCreation.ARGUMENTS_PROPERTY) {
			ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) reference.getParent();
			return SimpleStructureHelper.isSingletonList(classInstanceCreation.arguments());
		}

		return reference.getLocationInParent() == Assignment.RIGHT_HAND_SIDE_PROPERTY;
		*/
	}

	static boolean isConstantInitializer(Expression initializer) {

		if (initializer.getNodeType() == ASTNode.PREFIX_EXPRESSION) {
			PrefixExpression prefixExpression = (PrefixExpression) initializer;
			if (SimpleStructureHelper.isSupportedPrefixExpression(prefixExpression)) {
				return isConstantInitializer(prefixExpression.getOperand());
			}
		}
		

		if (ASTNodeUtil.isLiteral(initializer)) {
			return true;
		}
		

		if (initializer.getNodeType() == ASTNode.SIMPLE_NAME) {
			return isStaticFinalField((SimpleName) initializer);
		}

		if (initializer.getNodeType() == ASTNode.QUALIFIED_NAME) {
			return isStaticFinalField((QualifiedName) initializer);
		}

		return false;
	}

	static boolean isStaticFinalField(Name name) {
		IBinding binding = name.resolveBinding();
		if (binding == null) {
			return false;
		}

		if (binding.getKind() != IBinding.VARIABLE) {
			return false;
		}

		IVariableBinding variableBinding = (IVariableBinding) binding;
		int modifiers = variableBinding.getModifiers();
		return Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers);
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
