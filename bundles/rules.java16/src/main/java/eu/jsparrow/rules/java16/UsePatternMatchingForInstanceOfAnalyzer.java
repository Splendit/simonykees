package eu.jsparrow.rules.java16;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;

class UsePatternMatchingForInstanceOfAnalyzer {

	private final InstanceofExpression instanceOf;
	private SimpleName patternInstanceOfName;
	private ASTNode declarationNodeToRemove;

	UsePatternMatchingForInstanceOfAnalyzer(InstanceofExpression instanceOf) {
		this.instanceOf = instanceOf;
	}

	boolean analyzeInstanceOfExpression() {

		if (instanceOf.getLocationInParent() != IfStatement.EXPRESSION_PROPERTY) {
			return false;
		}

		VariableDeclarationStatement varDecl = findVariableDeclarationStatementInThenBlock(
				(IfStatement) instanceOf.getParent()).orElse(null);
		if (varDecl == null) {
			return false;
		}

		Expression instanceOfLeftOperand = instanceOf.getLeftOperand();
		String leftOperandTypeQualifiedName = instanceOfLeftOperand.resolveTypeBinding()
			.getErasure()
			.getQualifiedName();
		ITypeBinding rightOperandTypeBinding = instanceOf.getRightOperand().resolveBinding();

		if (!ClassRelationUtil.isInheritingContentOfTypes(rightOperandTypeBinding,
				Collections.singletonList(leftOperandTypeQualifiedName))) {
			return false;
		}

		VariableDeclarationFragment fragment = (VariableDeclarationFragment) varDecl.fragments()
			.get(0);

		if (!isFragmentWithExpectedTypeCastInitializer(fragment, instanceOfLeftOperand, rightOperandTypeBinding)) {
			return false;
		}

		if (varDecl.fragments()
			.size() > 1) {
			declarationNodeToRemove = fragment;
		} else {
			declarationNodeToRemove = varDecl;
		}
		patternInstanceOfName = fragment.getName();

		return true;
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

	private boolean isFragmentWithExpectedTypeCastInitializer(VariableDeclarationFragment fragment, Expression instanceOfLeftOperand,
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

	public InstanceofExpression getInstanceOf() {
		return instanceOf;
	}

	public SimpleName getPatternInstanceOfName() {
		return patternInstanceOfName;
	}

	public ASTNode getDeclarationNodeToRemove() {
		return declarationNodeToRemove;
	}
}
