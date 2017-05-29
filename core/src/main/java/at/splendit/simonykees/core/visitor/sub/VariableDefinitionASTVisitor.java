package at.splendit.simonykees.core.visitor.sub;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import at.splendit.simonykees.core.visitor.AbstractASTRewriteASTVisitor;

/**
 * Finds the definition of the variable thats given at construction. Checks if
 * the variable is only used in the statement of the executed AST
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
public class VariableDefinitionASTVisitor extends AbstractASTRewriteASTVisitor {
	private ASTNode beforeThis;
	private boolean endThis;
	private List<SimpleName> variableNames;
	private List<ASTNode> relevantBlocks;

	public VariableDefinitionASTVisitor() {
		variableNames = new ArrayList<>();
		endThis = true;
	}
	
	public VariableDefinitionASTVisitor(ASTNode beforeThis, List<ASTNode>relevantBlocks) {
		this();
		this.beforeThis = beforeThis;
		this.relevantBlocks = relevantBlocks;
		
	}
	
	public boolean preVisit2(ASTNode node) {
		if(node==beforeThis){
			endThis = false;
		}
		return endThis;
	}
	
	@Override
	public boolean visit(Block node) {
		boolean inScope = false;
		for(ASTNode block : relevantBlocks) {
			if(block == node) {
				inScope = true;
				break;
			}
		}
		
		return inScope;
	}
	
	@Override
	public boolean visit(ForStatement node) {
		boolean inScope = false;
		for(ASTNode block : relevantBlocks) {
			if(block == node) {
				inScope = true;
				break;
			}
		}
		
		return inScope;
	}
	
	@Override
	public boolean visit(MethodDeclaration node) {
		boolean inScope = false;
		for(ASTNode block : relevantBlocks) {
			if(block == node) {
				inScope = true;
				break;
			}
		}
		
		return inScope;
	}

	@Override
	public boolean visit(VariableDeclarationFragment node) {
		if(endThis) {
			variableNames.add(node.getName());
		}
		
		return endThis;
	}
	
	@Override
	public boolean visit(SingleVariableDeclaration node) {
		if(endThis) {
			variableNames.add(node.getName());
		}
		
		return endThis;
	}
	
	public List<SimpleName> getScopeVariableNames() {
		return variableNames;
	}
}