package eu.jsparrow.core.visitor.impl.ternary;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;

/**
 * Determines the length of the sum of the string equivalents of simple
 * names and literals which are contained in the given expression.
 * 
 * For example, the two expressions {@code x + y + z}, {@code xyz} and
 * {@code 123} have the same value for the significant length which is 3 in
 * this example.
 * 
 */
class SupportedTernaryOperandVisitor extends ASTVisitor {

	private static final int MAX_LENGTH = 20;

	private boolean supportedExpression = true;
	private int totalSignificantLength;
	
	static boolean isSupportedTernaryOperand(Expression expression) {
		SupportedTernaryOperandVisitor lengthVisitor = new SupportedTernaryOperandVisitor();
		expression.accept(lengthVisitor);
		return lengthVisitor.isSupportedExpression();
	}


	private static boolean isSupportedNodeType(int nodeType) {
		return nodeType == ASTNode.ARRAY_ACCESS ||
				nodeType == ASTNode.ARRAY_CREATION ||
				nodeType == ASTNode.ARRAY_INITIALIZER ||
				nodeType == ASTNode.BOOLEAN_LITERAL ||
				nodeType == ASTNode.CAST_EXPRESSION ||
				nodeType == ASTNode.CHARACTER_LITERAL ||
				nodeType == ASTNode.CLASS_INSTANCE_CREATION ||
				nodeType == ASTNode.FIELD_ACCESS ||
				nodeType == ASTNode.INFIX_EXPRESSION ||
				nodeType == ASTNode.INSTANCEOF_EXPRESSION ||
				nodeType == ASTNode.METHOD_INVOCATION ||
				nodeType == ASTNode.QUALIFIED_NAME ||
				nodeType == ASTNode.SIMPLE_NAME ||
				nodeType == ASTNode.NULL_LITERAL ||
				nodeType == ASTNode.NUMBER_LITERAL ||
				nodeType == ASTNode.PARENTHESIZED_EXPRESSION ||
				nodeType == ASTNode.POSTFIX_EXPRESSION ||
				nodeType == ASTNode.PREFIX_EXPRESSION ||
				nodeType == ASTNode.STRING_LITERAL ||
				nodeType == ASTNode.SUPER_FIELD_ACCESS ||
				nodeType == ASTNode.SUPER_METHOD_INVOCATION ||
				nodeType == ASTNode.THIS_EXPRESSION ||
				nodeType == ASTNode.TYPE_LITERAL ||
				nodeType == ASTNode.SIMPLE_TYPE ||
				nodeType == ASTNode.PRIMITIVE_TYPE ||
				nodeType == ASTNode.ARRAY_TYPE ||
				nodeType == ASTNode.PARAMETERIZED_TYPE ||
				nodeType == ASTNode.DIMENSION;
	}

	@Override
	public boolean preVisit2(ASTNode node) {
		supportedExpression = supportedExpression && isSupportedNodeType(node.getNodeType());
		return supportedExpression;
	}

	@Override
	public boolean visit(BooleanLiteral node) {
		addNodeLength(node);
		return false;
	}

	@Override
	public boolean visit(CharacterLiteral node) {
		addNodeLength(node);
		return false;
	}

	@Override
	public boolean visit(NullLiteral node) {
		addNodeLength(node);
		return false;
	}

	@Override
	public boolean visit(NumberLiteral node) {
		addNodeLength(node);
		return false;
	}

	@Override
	public boolean visit(StringLiteral node) {
		addNodeLength(node);
		return false;
	}

	@Override
	public boolean visit(SimpleName node) {
		addNodeLength(node);
		return false;
	}

	private void addNodeLength(ASTNode node) {
		totalSignificantLength += node.getLength();
		supportedExpression = totalSignificantLength <= MAX_LENGTH;
	}

	public boolean isSupportedExpression() {
		return supportedExpression;
	}
}