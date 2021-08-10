package eu.jsparrow.rules.java16;

import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.Expression;
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
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

public class UsePatternMatchingForInstanceofASTVisitor extends AbstractASTRewriteASTVisitor {

	private final ExpressionMatcher expressionMatcher = new ExpressionMatcher();

	@Override
	public boolean visit(InstanceofExpression instanceOf) {
		/*
		 * FIXME: this is only a prototype implementation. Please either drop
		 * it, or double-check every single step of this code.
		 */
		if (instanceOf.getLocationInParent() == IfStatement.EXPRESSION_PROPERTY) {
			IfStatement ifStatement = (IfStatement) instanceOf.getParent();
			Statement thenStatement = ifStatement.getThenStatement();
			if (thenStatement.getNodeType() != ASTNode.BLOCK) {
				return true;
			}
			Block block = (Block) thenStatement;
			List<Statement> statements = ASTNodeUtil.convertToTypedList(block.statements(), Statement.class);
			if (statements.isEmpty()) {
				return true;
			}
			Statement first = statements.get(0);
			if (first.getNodeType() != ASTNode.VARIABLE_DECLARATION_STATEMENT) {
				return true;
			}
			VariableDeclarationStatement varDecl = (VariableDeclarationStatement) first;
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) varDecl.fragments()
				.get(0);

			Expression initializer = fragment.getInitializer();
			if (initializer == null || initializer.getNodeType() != ASTNode.CAST_EXPRESSION) {
				return true;
			}
			CastExpression castExpression = (CastExpression) initializer;
			Expression castOperand = castExpression.getExpression();
			Expression instanceOfLeftOperand = instanceOf.getLeftOperand();
			if (!expressionMatcher.match(castOperand, instanceOfLeftOperand)) {
				return true;
			}

			SimpleName name = fragment.getName();
			AST ast = instanceOf.getAST();
			PatternInstanceofExpression patternInstanceOf = ast.newPatternInstanceofExpression();
			patternInstanceOf.setLeftOperand((Expression) astRewrite.createCopyTarget(instanceOfLeftOperand));
			SingleVariableDeclaration singleVarDecl = ast.newSingleVariableDeclaration();
			singleVarDecl.setType((Type) astRewrite.createCopyTarget(instanceOf.getRightOperand()));
			singleVarDecl.setName((SimpleName) astRewrite.createCopyTarget(name));
			patternInstanceOf.setRightOperand(singleVarDecl);
			astRewrite.replace(instanceOf, patternInstanceOf, null);
			if(varDecl.fragments().size() > 1) {
				astRewrite.remove(fragment, null);
			}
			else {
				astRewrite.remove(varDecl, null);				
			}
			
			onRewrite();
		}
		return true;
	}
}
