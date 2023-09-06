package eu.jsparrow.core.visitor.impl.inline;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
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
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import eu.jsparrow.rules.common.util.ASTNodeUtil;

/**
 * Helper class to determine whether or not it is possible and reasonable to
 * in-line a local variable which is used exactly once.
 * 
 */
public class SupportedUniqueUsageAnalyzer {
	private final CompilationUnit compilationUnit;

	protected SupportedUniqueUsageAnalyzer(CompilationUnit compilationUnit) {

		this.compilationUnit = compilationUnit;
	}

	Optional<SimpleName> findUniqueUsageToInline(VariableDeclarationStatement declarationStatement,
			VariableDeclarationFragment declarationFragment, Expression initializer) {
		UniqueLocalVariableReferenceVisitor uniqueReferenceVisitor = new UniqueLocalVariableReferenceVisitor(
				compilationUnit, declarationFragment);

		declarationStatement.getParent()
			.accept(uniqueReferenceVisitor);

		return uniqueReferenceVisitor.getUniqueLocalVariableReference()
			.filter(usage -> isSupportedUsage(usage, initializer, declarationStatement));

	}

	boolean isSupportedUsage(SimpleName usageToReplace, Expression initializer,
			VariableDeclarationStatement declarationStatement) {

		Expression expressionWithUsage;
		if (isSimpleInitializer(initializer)) {
			expressionWithUsage = findSupportedParentExpression(usageToReplace).orElse(usageToReplace);
		} else {
			expressionWithUsage = usageToReplace;
		}

		Statement statementWithSupportedUsage = findStatementWithSupportedUsage(expressionWithUsage)
			.orElse(null);

		if (statementWithSupportedUsage == null) {
			return false;
		}

		VariableDeclarationStatement previuosStatement = ASTNodeUtil
			.findPreviousStatementInBlock(statementWithSupportedUsage, VariableDeclarationStatement.class)
			.orElse(null);

		return previuosStatement == declarationStatement;
	}

	Optional<Statement> findStatementWithSupportedUsage(Expression expressionWithUsage) {

		if (expressionWithUsage.getLocationInParent() == ReturnStatement.EXPRESSION_PROPERTY) {
			return Optional.of((ReturnStatement) expressionWithUsage.getParent());
		}

		if (expressionWithUsage.getLocationInParent() == ThrowStatement.EXPRESSION_PROPERTY) {
			return Optional.of((ThrowStatement) expressionWithUsage.getParent());
		}

		if (expressionWithUsage.getLocationInParent() == Assignment.RIGHT_HAND_SIDE_PROPERTY) {
			Assignment assignment = (Assignment) expressionWithUsage.getParent();
			if (assignment.getLocationInParent() == ExpressionStatement.EXPRESSION_PROPERTY) {
				return Optional.of((ExpressionStatement) assignment.getParent());
			}
			return Optional.empty();
		}

		if (expressionWithUsage.getLocationInParent() == VariableDeclarationFragment.INITIALIZER_PROPERTY) {
			VariableDeclarationFragment parentDeclarationFragment = (VariableDeclarationFragment) expressionWithUsage
				.getParent();
			if (parentDeclarationFragment.getLocationInParent() == VariableDeclarationStatement.FRAGMENTS_PROPERTY) {
				return Optional.of((VariableDeclarationStatement) parentDeclarationFragment.getParent());
			}
			return Optional.empty();
		}

		if (expressionWithUsage.getLocationInParent() == ExpressionStatement.EXPRESSION_PROPERTY) {
			if (expressionWithUsage.getNodeType() == ASTNode.METHOD_INVOCATION
					|| expressionWithUsage.getNodeType() == ASTNode.SUPER_METHOD_INVOCATION
					|| expressionWithUsage.getNodeType() == ASTNode.CLASS_INSTANCE_CREATION) {
				return Optional.of((ExpressionStatement) expressionWithUsage.getParent());
			}
			return Optional.empty();
		}
		return Optional.empty();

	}

	static boolean isSimpleInitializer(Expression initializer) {
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

	private static boolean isSingletonList(@SuppressWarnings("rawtypes") List list) {
		return list.size() == 1;
	}

	static Optional<Expression> findSupportedParentExpression(SimpleName uniqueUsage) {

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
