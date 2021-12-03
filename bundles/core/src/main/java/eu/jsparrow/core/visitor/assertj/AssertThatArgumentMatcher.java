package eu.jsparrow.core.visitor.assertj;

import java.util.Optional;
import java.util.function.Predicate;

import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.TypeLiteral;

/**
 * Wrapper class storing a predicate on an Expression representing the argument
 * of an {@code assertThat} invocation. For example, if a {@link SimpleName} is
 * required as argument, then the predicate will determine whether a given
 * expression is equivalent to the specified {@link SimpleName}.
 * 
 * @since 4.6.0
 */
class AssertThatArgumentMatcher {

	private static final ASTMatcher astMatcher = new ASTMatcher();
	private final Predicate<Expression> assertThatArgumentPredicate;

	private AssertThatArgumentMatcher(Predicate<Expression> assertThatArgumentPredicate) {
		this.assertThatArgumentPredicate = assertThatArgumentPredicate;
	}

	boolean isMatchingAssertThatArgument(Expression assertThatArgument) {
		return assertThatArgumentPredicate.test(assertThatArgument);
	}

	static Optional<AssertThatArgumentMatcher> findAssertThatArgumentMatcher(Expression assertThatArgument) {

		if (assertThatArgument.getNodeType() == ASTNode.SIMPLE_NAME) {
			SimpleName simpleName = (SimpleName) assertThatArgument;
			return createOptionalOf(expression -> astMatcher.match(simpleName, expression));
		}

		if (assertThatArgument.getNodeType() == ASTNode.QUALIFIED_NAME) {
			QualifiedName qualifiedName = (QualifiedName) assertThatArgument;
			return createOptionalOf(expression -> astMatcher.match(qualifiedName, expression));
		}

		if (assertThatArgument.getNodeType() == ASTNode.FIELD_ACCESS) {
			FieldAccess fieldAccess = (FieldAccess) assertThatArgument;
			if (isSupportedFieldAccess(fieldAccess)) {
				return createOptionalOf(expression -> astMatcher.match(fieldAccess, expression));
			}
			return Optional.empty();
		}

		if (assertThatArgument.getNodeType() == ASTNode.THIS_EXPRESSION) {
			ThisExpression thisExpression = (ThisExpression) assertThatArgument;
			return createOptionalOf(expression -> astMatcher.match(thisExpression, expression));
		}

		if (assertThatArgument.getNodeType() == ASTNode.SUPER_FIELD_ACCESS) {
			SuperFieldAccess superFieldAccess = (SuperFieldAccess) assertThatArgument;
			return createOptionalOf(expression -> astMatcher.match(superFieldAccess, expression));
		}

		if (assertThatArgument.getNodeType() == ASTNode.NUMBER_LITERAL) {
			NumberLiteral numberLiteral = (NumberLiteral) assertThatArgument;
			return createOptionalOf(expression -> astMatcher.match(numberLiteral, expression));
		}

		if (assertThatArgument.getNodeType() == ASTNode.CHARACTER_LITERAL) {
			CharacterLiteral characterLiteral = (CharacterLiteral) assertThatArgument;
			return createOptionalOf(expression -> astMatcher.match(characterLiteral, expression));
		}

		if (assertThatArgument.getNodeType() == ASTNode.STRING_LITERAL) {
			StringLiteral stringLiteral = (StringLiteral) assertThatArgument;
			return createOptionalOf(expression -> astMatcher.match(stringLiteral, expression));
		}

		if (assertThatArgument.getNodeType() == ASTNode.TYPE_LITERAL) {
			TypeLiteral typeLiteral = (TypeLiteral) assertThatArgument;
			return createOptionalOf(expression -> astMatcher.match(typeLiteral, expression));
		}

		return Optional.empty();
	}

	private static boolean isSupportedFieldAccess(FieldAccess fieldAccess) {
		Expression fieldAccessExpression = fieldAccess.getExpression();

		if (fieldAccessExpression != null) {
			if (fieldAccessExpression.getNodeType() == ASTNode.FIELD_ACCESS) {
				return isSupportedFieldAccess((FieldAccess) fieldAccessExpression);
			}
			return fieldAccessExpression.getNodeType() == ASTNode.THIS_EXPRESSION ||
					fieldAccessExpression.getNodeType() == ASTNode.SUPER_FIELD_ACCESS;
		}
		return false;
	}

	private static Optional<AssertThatArgumentMatcher> createOptionalOf(
			Predicate<Expression> assertThatArgumentPredicate) {
		return Optional.of(new AssertThatArgumentMatcher(assertThatArgumentPredicate));
	}
}
