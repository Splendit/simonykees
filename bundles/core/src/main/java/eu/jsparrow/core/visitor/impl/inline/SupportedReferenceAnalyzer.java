package eu.jsparrow.core.visitor.impl.inline;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import eu.jsparrow.rules.common.util.ASTNodeUtil;

/**
 * Helper class to find out whether or not it is possible and reasonable to
 * in-line a reference on a local variable, provided that the local variable is
 * used exactly once.
 * 
 */
public class SupportedReferenceAnalyzer {
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
			.filter(variableDeclarationStatement -> isSingletonList(declarationStatement.fragments()));

	}

	private static boolean isSingletonList(@SuppressWarnings("rawtypes") List list) {
		return list.size() == 1;
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

		if (!isSimpleInitializer(initializerToReplaceReference)) {
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

	private static boolean isSimpleInitializer(Expression initializer) {
		if (isSimpleThisExpression(initializer)) {
			return true;
		}
		if (initializer.getNodeType() == ASTNode.SIMPLE_NAME) {
			return true;
		}
		if (initializer.getNodeType() == ASTNode.QUALIFIED_NAME) {
			QualifiedName qualifiedName = (QualifiedName) initializer;
			return qualifiedName.getQualifier()
				.getNodeType() == ASTNode.SIMPLE_NAME;
		}
		if (initializer.getNodeType() == ASTNode.SUPER_FIELD_ACCESS) {
			return true;
		}
		if (initializer.getNodeType() == ASTNode.FIELD_ACCESS) {
			FieldAccess fieldAccess = (FieldAccess) initializer;
			return isSimpleMemberAccessQualifier(fieldAccess.getExpression());
		}
		if (initializer.getNodeType() == ASTNode.TYPE_LITERAL) {
			TypeLiteral typeLiteral = (TypeLiteral) initializer;
			return isSupportedSimpleType(typeLiteral.getType());

		}
		return ASTNodeUtil.isLiteral(initializer);
	}

	private static Optional<Expression> findSupportedParentExpression(SimpleName uniqueUsage) {

		if (uniqueUsage.getLocationInParent() == MethodInvocation.ARGUMENTS_PROPERTY) {
			MethodInvocation methodInvocation = (MethodInvocation) uniqueUsage.getParent();
			if (isSimpleMethodInvocation(methodInvocation)) {
				return Optional.of(methodInvocation);
			}
			return Optional.empty();
		}

		if (uniqueUsage.getLocationInParent() == SuperMethodInvocation.ARGUMENTS_PROPERTY) {
			SuperMethodInvocation superMethodInvocation = (SuperMethodInvocation) uniqueUsage.getParent();
			if (isSimpleSuperMethodInvocation(superMethodInvocation)) {
				return Optional.of(superMethodInvocation);
			}
			return Optional.empty();
		}

		if (uniqueUsage.getLocationInParent() == ClassInstanceCreation.ARGUMENTS_PROPERTY) {
			ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) uniqueUsage.getParent();
			if (isSimpleClassInstanceCreation(classInstanceCreation)) {
				return Optional.of(classInstanceCreation);
			}
			return Optional.empty();
		}

		if (uniqueUsage.getLocationInParent() == PrefixExpression.OPERAND_PROPERTY) {
			PrefixExpression prefixExpression = (PrefixExpression) uniqueUsage.getParent();
			if (isSupportedPrefixExpression(prefixExpression)) {
				return Optional.of(prefixExpression);
			}
			return Optional.empty();
		}

		return Optional.empty();

	}

	private static boolean isSimpleMethodInvocation(MethodInvocation methodInvocation) {
		if (!isSingletonList(methodInvocation.arguments())) {
			return false;
		}
		Expression expression = methodInvocation.getExpression();
		if (expression == null) {
			return true;
		}
		return isSimpleMemberAccessQualifier(expression);
	}

	private static boolean isSimpleMemberAccessQualifier(Expression memberAccessQualifier) {
		if (memberAccessQualifier.getNodeType() == ASTNode.SIMPLE_NAME) {
			return true;
		}
		return isSimpleThisExpression(memberAccessQualifier);
	}

	private static boolean isSimpleThisExpression(Expression expression) {
		if (expression.getNodeType() != ASTNode.THIS_EXPRESSION) {
			return false;
		}
		ThisExpression thisExpresion = (ThisExpression) expression;
		return thisExpresion.getQualifier() == null;
	}

	private static boolean isSimpleSuperMethodInvocation(SuperMethodInvocation superMethodInvocation) {
		if (!isSingletonList(superMethodInvocation.arguments())) {
			return false;
		}
		return superMethodInvocation.getQualifier() == null;
	}

	private static boolean isSimpleClassInstanceCreation(ClassInstanceCreation classInstanceCreation) {
		if (!isSingletonList(classInstanceCreation.arguments())) {
			return false;
		}

		if (classInstanceCreation.getExpression() != null) {
			return false;
		}

		return isSupportedSimpleType(classInstanceCreation.getType());
	}

	private static boolean isSupportedSimpleType(Type type) {
		if (type.getNodeType() != ASTNode.SIMPLE_TYPE) {
			return false;
		}
		Name typeName = ((SimpleType) type).getName();
		return typeName.getNodeType() == ASTNode.SIMPLE_NAME;
	}

	private static boolean isSupportedPrefixExpression(PrefixExpression prefixExpression) {
		PrefixExpression.Operator operator = prefixExpression.getOperator();
		return operator == PrefixExpression.Operator.PLUS ||
				operator == PrefixExpression.Operator.MINUS ||
				operator == PrefixExpression.Operator.NOT ||
				operator == PrefixExpression.Operator.COMPLEMENT;
	}

}
