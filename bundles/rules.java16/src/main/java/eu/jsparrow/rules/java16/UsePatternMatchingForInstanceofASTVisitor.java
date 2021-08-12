package eu.jsparrow.rules.java16;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.PatternInstanceofExpression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;

import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

public class UsePatternMatchingForInstanceofASTVisitor extends AbstractASTRewriteASTVisitor {

	@Override
	public boolean visit(InstanceofExpression instanceOf) {
		UsePatternMatchingForInstanceOfAnalyzer analyzer = new UsePatternMatchingForInstanceOfAnalyzer(instanceOf);
		if(analyzer.analyzeInstanceOfExpression()) {
			transform(analyzer);
		}

		return true;
	}

	private void transform(UsePatternMatchingForInstanceOfAnalyzer analyzer) {
		
		InstanceofExpression instanceOf = analyzer.getInstanceOf();
		Expression instanceOfLeftOperand = instanceOf.getLeftOperand();
		Type instanceOfRightOperand = instanceOf.getRightOperand();
		SimpleName patternInstanceOfName = analyzer.getPatternInstanceOfName();
		ASTNode declarationNodeToRemove = analyzer.getDeclarationNodeToRemove();
	
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
