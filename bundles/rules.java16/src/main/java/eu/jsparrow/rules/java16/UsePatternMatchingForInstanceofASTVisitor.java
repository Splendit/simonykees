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
import org.eclipse.jdt.core.dom.PatternInstanceofExpression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

public class UsePatternMatchingForInstanceofASTVisitor extends AbstractASTRewriteASTVisitor {

	@Override
	public boolean visit(InstanceofExpression instanceOf) {
		analyzeInstanceOfExpression(instanceOf).ifPresent(this::transform);
		return true;
	}

	private Optional<UsePatternMatchingForInstanceofData> analyzeInstanceOfExpression(InstanceofExpression instanceOf) {

		if (instanceOf.getLocationInParent() != IfStatement.EXPRESSION_PROPERTY) {
			return Optional.empty();
		}

		VariableDeclarationStatement varDecl = findVariableDeclarationStatementInThenBlock(
				(IfStatement) instanceOf.getParent()).orElse(null);
		if (varDecl == null) {
			return Optional.empty();
		}

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

	private Optional<VariableDeclarationStatement> findVariableDeclarationStatementInThenBlock(
			IfStatement ifStatement) {

		Statement thenStatement = ifStatement.getThenStatement();
		if (thenStatement.getNodeType() != ASTNode.BLOCK) {
			return Optional.empty();
		}
		Block block = (Block) thenStatement;
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
