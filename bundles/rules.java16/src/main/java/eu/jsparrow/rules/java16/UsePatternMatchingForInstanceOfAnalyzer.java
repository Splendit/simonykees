package eu.jsparrow.rules.java16;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
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
		if (instanceOf.getLocationInParent() == IfStatement.EXPRESSION_PROPERTY) {
			Expression instanceOfLeftOperand = instanceOf.getLeftOperand();
			Type instanceOfRightOperand = instanceOf.getRightOperand();
			String leftOperandTypeQualifiedName = instanceOfLeftOperand.resolveTypeBinding()
				.getErasure()
				.getQualifiedName();
			ITypeBinding rightOperandTypeBinding = instanceOfRightOperand.resolveBinding();

			if (!ClassRelationUtil.isInheritingContentOfTypes(rightOperandTypeBinding,
					Collections.singletonList(leftOperandTypeQualifiedName))) {
				return false;
			}

			IfStatement ifStatement = (IfStatement) instanceOf.getParent();
			Statement thenStatement = ifStatement.getThenStatement();
			if (thenStatement.getNodeType() != ASTNode.BLOCK) {
				return false;
			}
			Block block = (Block) thenStatement;
			List<Statement> statements = ASTNodeUtil.convertToTypedList(block.statements(), Statement.class);
			if (statements.isEmpty()) {
				return false;
			}
			Statement first = statements.get(0);
			if (first.getNodeType() != ASTNode.VARIABLE_DECLARATION_STATEMENT) {
				return false;
			}
			VariableDeclarationStatement varDecl = (VariableDeclarationStatement) first;
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) varDecl.fragments()
				.get(0);

			Expression initializer = fragment.getInitializer();
			if (initializer == null || initializer.getNodeType() != ASTNode.CAST_EXPRESSION) {
				return false;
			}

			if (!ClassRelationUtil.compareITypeBinding(rightOperandTypeBinding, initializer.resolveTypeBinding())) {
				return false;
			}

			CastExpression castExpression = (CastExpression) initializer;
			Expression castOperand = castExpression.getExpression();

			ExpressionMatcher expressionMatcher = new ExpressionMatcher();
			if (!expressionMatcher.match(castOperand, instanceOfLeftOperand)) {
				return false;
			}

			if (varDecl.fragments()
				.size() > 1) {
				declarationNodeToRemove = fragment;
			} else {
				declarationNodeToRemove = varDecl;
			}			
			patternInstanceOfName = fragment.getName();
		}
		return true;
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
