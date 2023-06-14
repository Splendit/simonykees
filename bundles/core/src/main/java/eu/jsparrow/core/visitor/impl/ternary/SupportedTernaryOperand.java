package eu.jsparrow.core.visitor.impl.ternary;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.TypeLiteral;

public class SupportedTernaryOperand {

	private static final Set<Class<?>> SUPPORTED_EXPRESSION_TYPES = createSupportedTypesSet();

	private static Set<Class<?>> createSupportedTypesSet() {
		Set<Class<?>> supportedTypes = new HashSet<>();
		supportedTypes.add(ArrayAccess.class);
		supportedTypes.add(ArrayCreation.class);
		supportedTypes.add(BooleanLiteral.class);
		supportedTypes.add(CastExpression.class);
		supportedTypes.add(CharacterLiteral.class);
		supportedTypes.add(ClassInstanceCreation.class);
		supportedTypes.add(FieldAccess.class);
		supportedTypes.add(InfixExpression.class);
		supportedTypes.add(InstanceofExpression.class);
		supportedTypes.add(MethodInvocation.class);
		supportedTypes.add(QualifiedName.class);
		supportedTypes.add(SimpleName.class);
		supportedTypes.add(NullLiteral.class);
		supportedTypes.add(NumberLiteral.class);
		supportedTypes.add(ParenthesizedExpression.class);
		supportedTypes.add(PostfixExpression.class);
		supportedTypes.add(PrefixExpression.class);
		supportedTypes.add(StringLiteral.class);
		supportedTypes.add(SuperFieldAccess.class);
		supportedTypes.add(SuperMethodInvocation.class);
		supportedTypes.add(ThisExpression.class);
		supportedTypes.add(TypeLiteral.class);
		return Collections.unmodifiableSet(supportedTypes);
	}

	static boolean isSupportedTernaryOperand(Expression expression) {
		if (SUPPORTED_EXPRESSION_TYPES.contains(expression.getClass())) {
			SignificantExpressionLengthVisitor lengthVisitor = new SignificantExpressionLengthVisitor();
			expression.accept(lengthVisitor);
			return lengthVisitor.isExpressionSupported();
		}
		return false;
	}

	private SupportedTernaryOperand() {
		// private default constructor hiding implicit public one
	}

	/**
	 * Determines the length of the sum of the string equivalents of simple
	 * names and literals which are contained in the given expression.
	 * 
	 * For example, the two expressions {@code x + y + z}, {@code xyz} and
	 * {@code 123} have the same value for the significant length which is 3 in
	 * this example.
	 * 
	 */
	static class SignificantExpressionLengthVisitor extends ASTVisitor {

		private static final int MAX_LENGTH = 30;

		private boolean unsupportedNode;
		private int totalSignificantLength;

		private static boolean isUnsupportedNode(ASTNode node) {
			int nodeType = node.getNodeType();
			return nodeType == ASTNode.TEXT_BLOCK
					|| nodeType == ASTNode.ASSIGNMENT
					|| nodeType == ASTNode.CONDITIONAL_EXPRESSION
					|| nodeType == ASTNode.LAMBDA_EXPRESSION
					|| nodeType == ASTNode.ANONYMOUS_CLASS_DECLARATION;
		}

		@Override
		public boolean preVisit2(ASTNode node) {
			unsupportedNode = unsupportedNode || isUnsupportedNode(node);
			if (unsupportedNode || totalSignificantLength > MAX_LENGTH) {
				return false;
			}
			return super.preVisit2(node);
		}

		@Override
		public boolean visit(BooleanLiteral node) {
			totalSignificantLength += node.getLength();
			return false;
		}

		@Override
		public boolean visit(CharacterLiteral node) {
			totalSignificantLength += node.getLength();
			return false;
		}

		@Override
		public boolean visit(NullLiteral node) {
			totalSignificantLength += node.getLength();
			return false;
		}

		@Override
		public boolean visit(NumberLiteral node) {
			totalSignificantLength += node.getLength();
			return false;
		}

		@Override
		public boolean visit(StringLiteral node) {
			totalSignificantLength += node.getLength();
			return false;
		}

		@Override
		public boolean visit(SimpleName node) {
			totalSignificantLength += node.getLength();
			return false;
		}

		public boolean isExpressionSupported() {
			if (unsupportedNode) {
				return false;
			}
			return totalSignificantLength <= MAX_LENGTH;
		}
	}
}
