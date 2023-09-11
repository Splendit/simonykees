package eu.jsparrow.core.visitor.impl.inline;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeLiteral;

import eu.jsparrow.rules.common.util.ASTNodeUtil;

/**
 * Offers methods which help to decide whether or not an in-lining of a local
 * variable is reasonable or not.
 * 
 * @since 4.19.0
 */
class SimpleStructureHelper {

	static boolean isSingletonList(@SuppressWarnings("rawtypes") List list) {
		return list.size() == 1;
	}

	static boolean isSimpleThisExpression(Expression expression) {
		if (expression.getNodeType() != ASTNode.THIS_EXPRESSION) {
			return false;
		}
		ThisExpression thisExpresion = (ThisExpression) expression;
		return thisExpresion.getQualifier() == null;
	}

	static boolean isSimpleMemberAccessQualifier(Expression memberAccessQualifier) {
		if (memberAccessQualifier.getNodeType() == ASTNode.SIMPLE_NAME) {
			return true;
		}
		return isSimpleThisExpression(memberAccessQualifier);
	}

	static boolean isSupportedSimpleType(Type type) {
		if (type.getNodeType() != ASTNode.SIMPLE_TYPE) {
			return false;
		}
		Name typeName = ((SimpleType) type).getName();
		return typeName.getNodeType() == ASTNode.SIMPLE_NAME;
	}

	static boolean isSimpleInitializer(Expression initializer, boolean includingPrefix) {

		if (includingPrefix && initializer.getNodeType() == ASTNode.PREFIX_EXPRESSION) {
			PrefixExpression prefixExpression = (PrefixExpression) initializer;
			if (isSupportedPrefixExpression(prefixExpression)) {
				return isSimpleInitializer(prefixExpression.getOperand(), false);
			}
		}
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

	static boolean isSimpleMethodInvocation(MethodInvocation methodInvocation) {
		if (!isSingletonList(methodInvocation.arguments())) {
			return false;
		}

		if (!methodInvocation.typeArguments()
			.isEmpty()) {
			return false;
		}
		Expression expression = methodInvocation.getExpression();
		if (expression == null) {
			return true;
		}
		return isSimpleMemberAccessQualifier(expression);
	}

	static boolean isSimpleSuperMethodInvocation(SuperMethodInvocation superMethodInvocation) {
		if (!isSingletonList(superMethodInvocation.arguments())) {
			return false;
		}
		if (!superMethodInvocation.typeArguments()
			.isEmpty()) {
			return false;
		}
		return superMethodInvocation.getQualifier() == null;
	}

	static boolean isSimpleClassInstanceCreation(ClassInstanceCreation classInstanceCreation) {
		if (!isSingletonList(classInstanceCreation.arguments())) {
			return false;
		}

		if (classInstanceCreation.getExpression() != null) {
			return false;
		}

		return isSupportedSimpleType(classInstanceCreation.getType());
	}

	static boolean isSupportedPrefixExpression(PrefixExpression prefixExpression) {
		PrefixExpression.Operator operator = prefixExpression.getOperator();
		return operator == PrefixExpression.Operator.PLUS ||
				operator == PrefixExpression.Operator.MINUS ||
				operator == PrefixExpression.Operator.NOT ||
				operator == PrefixExpression.Operator.COMPLEMENT;
	}

	private SimpleStructureHelper() {
		/*
		 * private default constructor hiding implicit public one
		 */
	}
}