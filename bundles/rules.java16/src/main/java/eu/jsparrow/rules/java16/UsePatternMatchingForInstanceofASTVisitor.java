package eu.jsparrow.rules.java16;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PatternInstanceofExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * This visitor looks for {@link InstanceofExpression}-nodes which must
 * represent either an {@code if} - condition or a negated {@code if} -
 * condition. If possible, the given {@link InstanceofExpression}-node is
 * replaced by a {@link PatternInstanceofExpression}-node, representing a
 * <b>Pattern Matching for Instanceof expression</b> which is introduced in Java
 * 16.
 * 
 * Example:
 * 
 * <pre>
 * if (o instanceof String) {
 * 	String value = (String) o;
 * 	System.out.println(value);
 * }
 * </pre>
 * 
 * is transformed to
 * 
 * <pre>
 * if(o instanceof String value) {
 * 	System.out.println(value);
 * }
 * </pre>
 * 
 * @since 4.2.0
 * 
 */
public class UsePatternMatchingForInstanceofASTVisitor extends AbstractASTRewriteASTVisitor {

	@Override
	public boolean visit(InstanceofExpression instanceOf) {

		VariableDeclarationStatement variableDeclarationForPatternMatching = findVariableDeclarationForPatternMatching(
				instanceOf).orElse(null);

		if (variableDeclarationForPatternMatching != null) {
			findPatternMatchingData(instanceOf, variableDeclarationForPatternMatching).ifPresent(this::transform);
		}

		return true;
	}

	Optional<VariableDeclarationStatement> findVariableDeclarationForPatternMatching(InstanceofExpression instanceOf) {

		if (instanceOf.getLocationInParent() == IfStatement.EXPRESSION_PROPERTY) {
			Statement thenStatement = ((IfStatement) instanceOf.getParent()).getThenStatement();
			return findVariableStatementInBlock(thenStatement);
		}

		if (instanceOf.getLocationInParent() != ParenthesizedExpression.EXPRESSION_PROPERTY) {
			return Optional.empty();
		}
		ParenthesizedExpression parenthesizedExpression = (ParenthesizedExpression) instanceOf.getParent();

		if (parenthesizedExpression.getLocationInParent() != PrefixExpression.OPERAND_PROPERTY) {
			return Optional.empty();
		}
		PrefixExpression prefixExpression = (PrefixExpression) parenthesizedExpression.getParent();

		if (prefixExpression.getLocationInParent() != IfStatement.EXPRESSION_PROPERTY) {
			return Optional.empty();
		}
		IfStatement ifStatement = (IfStatement) prefixExpression.getParent();
		Statement elseStatement = ifStatement.getElseStatement();

		if (elseStatement != null) {
			return findVariableStatementInBlock(elseStatement);
		}

		if (isThenReturnOrThenBlockEndingWithReturn(ifStatement)) {
			return findVariableDeclarationAfterIfStatement(ifStatement);
		}

		return Optional.empty();
	}

	private Optional<UsePatternMatchingForInstanceofData> findPatternMatchingData(InstanceofExpression instanceOf,
			VariableDeclarationStatement varDecl) {

		Expression instanceOfLeftOperand = instanceOf.getLeftOperand();
		String leftOperandTypeQualifiedName = instanceOfLeftOperand.resolveTypeBinding()
			.getErasure()
			.getQualifiedName();
		ITypeBinding rightOperandTypeBinding = instanceOf.getRightOperand()
			.resolveBinding();

		if (!ClassRelationUtil.isInheritingContentOfTypes(rightOperandTypeBinding,
				Collections.singletonList(leftOperandTypeQualifiedName))) {
			return Optional.empty();
		}

		VariableDeclarationFragment fragment = (VariableDeclarationFragment) varDecl.fragments()
			.get(0);

		if (!isFragmentWithExpectedTypeCastInitializer(fragment, instanceOfLeftOperand, rightOperandTypeBinding)) {
			return Optional.empty();
		}

		SimpleName patternInstanceOfName = fragment.getName();
		if (varDecl.fragments()
			.size() > 1) {
			return Optional.of(new UsePatternMatchingForInstanceofData(instanceOf, patternInstanceOfName, fragment));

		}
		return Optional.of(new UsePatternMatchingForInstanceofData(instanceOf, patternInstanceOfName, varDecl));
	}

	private boolean isThenReturnOrThenBlockEndingWithReturn(IfStatement ifStatement) {
		Statement thenStatement = ifStatement.getThenStatement();
		if (thenStatement.getNodeType() == ASTNode.RETURN_STATEMENT) {
			return true;
		}
		if (thenStatement.getNodeType() != ASTNode.BLOCK) {
			return false;
		}
		Block block = (Block) thenStatement;
		List<Statement> statements = ASTNodeUtil.convertToTypedList(block.statements(), Statement.class);
		if (statements.isEmpty()) {
			return false;
		}
		int lastStatementIndex = statements.size() - 1;
		Statement lastStatement = statements.get(lastStatementIndex);
		return lastStatement.getNodeType() == ASTNode.RETURN_STATEMENT;
	}

	private Optional<VariableDeclarationStatement> findVariableStatementInBlock(
			Statement statementExpectedAsBlock) {
		if (statementExpectedAsBlock.getNodeType() != ASTNode.BLOCK) {
			return Optional.empty();
		}
		Block block = (Block) statementExpectedAsBlock;
		List<Statement> statements = ASTNodeUtil.convertToTypedList(block.statements(), Statement.class);
		if (statements.isEmpty()) {
			return Optional.empty();
		}
		Statement first = statements.get(0);
		if (first.getNodeType() != ASTNode.VARIABLE_DECLARATION_STATEMENT) {
			return Optional.empty();
		}
		return Optional.of((VariableDeclarationStatement) first);
	}

	private Optional<VariableDeclarationStatement> findVariableDeclarationAfterIfStatement(
			IfStatement ifStatement) {
		if (ifStatement.getLocationInParent() != Block.STATEMENTS_PROPERTY) {
			return Optional.empty();
		}
		Block block = (Block) ifStatement.getParent();
		List<Statement> statements = ASTNodeUtil.convertToTypedList(block.statements(), Statement.class);
		int indexOfStatementAfterIf = statements.indexOf(ifStatement) + 1;

		if (indexOfStatementAfterIf >= statements.size()) {
			return Optional.empty();
		}

		Statement statementAfterIf = statements.get(indexOfStatementAfterIf);
		if (statementAfterIf.getNodeType() != ASTNode.VARIABLE_DECLARATION_STATEMENT) {
			return Optional.empty();
		}
		return Optional.of((VariableDeclarationStatement) statementAfterIf);
	}

	private boolean isFragmentWithExpectedTypeCastInitializer(VariableDeclarationFragment fragment,
			Expression instanceOfLeftOperand,
			ITypeBinding expectedTypeBinding) {
		Expression initializer = fragment.getInitializer();
		if (initializer == null || initializer.getNodeType() != ASTNode.CAST_EXPRESSION) {
			return false;
		}

		CastExpression castExpression = (CastExpression) initializer;
		Expression castOperand = castExpression.getExpression();

		ExpressionMatcher expressionMatcher = new ExpressionMatcher();
		if (!expressionMatcher.match(castOperand, instanceOfLeftOperand)) {
			return false;
		}

		return ClassRelationUtil.compareITypeBinding(expectedTypeBinding, initializer.resolveTypeBinding());
	}

	private void transform(UsePatternMatchingForInstanceofData transformationData) {

		InstanceofExpression instanceOf = transformationData.getInstanceOf();
		Expression instanceOfLeftOperand = instanceOf.getLeftOperand();
		Type instanceOfRightOperand = instanceOf.getRightOperand();
		SimpleName patternInstanceOfName = transformationData.getPatternInstanceOfName();
		ASTNode declarationNodeToRemove = transformationData.getDeclarationNodeToRemove();

		AST ast = instanceOf.getAST();
		PatternInstanceofExpression patternInstanceOf = ast.newPatternInstanceofExpression();
		patternInstanceOf.setLeftOperand((Expression) astRewrite.createCopyTarget(instanceOfLeftOperand));
		SingleVariableDeclaration singleVarDecl = ast.newSingleVariableDeclaration();
		singleVarDecl.setType((Type) astRewrite.createCopyTarget(instanceOfRightOperand));
		singleVarDecl.setName((SimpleName) astRewrite.createCopyTarget(patternInstanceOfName));
		patternInstanceOf.setRightOperand(singleVarDecl);
		astRewrite.replace(instanceOf, patternInstanceOf, null);
		astRewrite.remove(declarationNodeToRemove, null);
		onRewrite();
	}
}
