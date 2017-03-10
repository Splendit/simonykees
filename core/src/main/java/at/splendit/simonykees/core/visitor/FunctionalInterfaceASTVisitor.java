package at.splendit.simonykees.core.visitor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;

import at.splendit.simonykees.core.visitor.loop.VariableDefinitionASTVisitor;

/**
 * Finds anonymous classes an converts it to lambdas, if they are functional
 * interfaces.
 * 
 * @author Martin Huter, Ardit Ymeri
 * @since 0.9
 *
 */
public class FunctionalInterfaceASTVisitor extends AbstractASTRewriteASTVisitor {

	@SuppressWarnings("unchecked")
	@Override
	public boolean visit(AnonymousClassDeclaration node) {
		if (ASTNode.CLASS_INSTANCE_CREATION == node.getParent().getNodeType()) {
			ClassInstanceCreation parentNode = (ClassInstanceCreation) node.getParent();
			Type parentType = parentNode.getType();
			if(ASTNode.PARAMETERIZED_TYPE != parentType.getNodeType()) {
				ITypeBinding parentNodeTypeBinding = parentNode.getType().resolveBinding();
				if (parentNodeTypeBinding != null) {
					if (parentNodeTypeBinding.getFunctionalInterfaceMethod() != null) {
						LambdaExpression newInitializer = node.getAST().newLambdaExpression();
						MethodBlockASTVisitor methodBlockASTVisitor = new MethodBlockASTVisitor();
						node.accept(methodBlockASTVisitor);
						Block moveBlock = methodBlockASTVisitor.getMethodBlock();
						if (moveBlock != null) {
							List<SingleVariableDeclaration> parameteres = methodBlockASTVisitor.getParameters();
							if (parameteres != null) {
								List<ASTNode> relevantBlocks = new ArrayList<>();

								// renaming the clashing variable names
								ASTNode scope = findScope(node, relevantBlocks);								
								if(ASTNode.TYPE_DECLARATION != scope.getNodeType()) {
									// if the scope is the whole class, no need to do any renaming...
									
									VariableDefinitionASTVisitor varVisistor = new VariableDefinitionASTVisitor(node, relevantBlocks);
									scope.accept(varVisistor);
									List<SimpleName> scopeNames = varVisistor.getScopeVariableNames();
									List<SimpleName> conflictingNames = findConflictingNames(parameteres, scopeNames);
									
									if(!conflictingNames.isEmpty()) {
										renameParameters(node, scopeNames, conflictingNames);
									}
								}

								for (SingleVariableDeclaration s : parameteres) {
									newInitializer.parameters().add(astRewrite.createMoveTarget(s));
								}
							}
							newInitializer.setBody(astRewrite.createMoveTarget(moveBlock));
							getAstRewrite().replace(parentNode, newInitializer, null);
						}
					}
				}
			}
		}
		return true;
	}

	/**
	 * Renames all occurrences of variables with conflicting names in the 
	 * given anonymous class node, so that they will not clash with 
	 * the existing local variables within the scope.
	 * 
	 * @param node node representing the anonymous class declaration.
	 * @param scopeNames variables names in the scope of 
	 * the declaration the anonymous class.
	 * @param conflictingNames list of variable names conflicting 
	 * with the existing local variables.
	 */
	private void renameParameters(AnonymousClassDeclaration node, 
			List<SimpleName> scopeNames, List<SimpleName> conflictingNames) {
		LocalVariableUsagesASTVisitor visitor;
		for(SimpleName conflictingName : conflictingNames) {
			visitor = new LocalVariableUsagesASTVisitor(conflictingName);
			node.accept(visitor);
			String newName = calcNewName(scopeNames, conflictingName);
			List<SimpleName> usages = visitor.getUsages();
			for(SimpleName usage : usages) {
				astRewrite.set(usage, SimpleName.IDENTIFIER_PROPERTY, newName, null);
			}
		}
	}
	
	/**
	 * Computes a new variable name by appending a suffix to the existing
	 * name, so that it will not clash with any of the variable within
	 * the scope.
	 * 
	 * @param scopeVariableNames names of the local variables within the scope.
	 * @param simpleName current name to be changed.
	 * 
	 * @return new safe name for the variable.
	 */
	private String calcNewName(List<SimpleName> scopeVariableNames, SimpleName simpleName) {
		int suffix = 1;
		List<String> identifiers =
				scopeVariableNames
				.stream()
				.map(SimpleName::getIdentifier)
				.collect(Collectors.toList());
		
		String newName = simpleName.getIdentifier() + Integer.toString(suffix);
		
		while(identifiers.contains(newName)) {
			suffix++;
			newName = simpleName.getIdentifier() + Integer.toString(suffix);
		}
		
		return newName;
	}

	/**
	 * Finds the scope where the anonymous class declaration occurs. 
	 * In addition it adds all its parent blocks to the {@code relevantBlocks} 
	 * list.
	 */
	private ASTNode findScope(AnonymousClassDeclaration node, List<ASTNode> relevantBlocks) {
		ASTNode scope = null;
		if(node != null && relevantBlocks != null) {
			scope = node;
			do {
				scope = scope.getParent();
				int scopeNodeType = scope.getNodeType();
				if(ASTNode.BLOCK == scopeNodeType
						|| ASTNode.FOR_STATEMENT == scopeNodeType
						|| ASTNode.METHOD_DECLARATION == scopeNodeType) {
					relevantBlocks.add(scope);
				}
			}
			while(scope != null 
					&& scope.getNodeType() != ASTNode.METHOD_DECLARATION 
					&& scope.getNodeType() != ASTNode.TYPE_DECLARATION);
		}
		
		return scope;
	}

	/**
	 * Filters the names of the given {@code scopeVariableNames} that occur
	 * also in the given {@code parameters}
	 */
	private List<SimpleName> findConflictingNames(List<SingleVariableDeclaration> parameters, 
			List<SimpleName> scopeVariableNames) {
		
		List<String> varNames = 
				scopeVariableNames
				.stream()
				.map(SimpleName::getIdentifier)
				.distinct()
				.collect(Collectors.toList());
		
		return
			parameters
			.stream()
			.map(parameter -> parameter.getName())
			.filter(parameter -> varNames.contains(parameter.getIdentifier()))
			.collect(Collectors.toList());
	}

	private class MethodBlockASTVisitor extends ASTVisitor {
		private Block methodBlock = null;
		private List<SingleVariableDeclaration> parameters = null;

		@Override
		public boolean visit(Block node) {
			methodBlock = (Block) getAstRewrite().createMoveTarget(node);
			getAstRewrite().remove(node, null);
			return false;
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean visit(MethodDeclaration node) {
			if (!node.parameters().isEmpty()) {
				/**
				 * node.parameters() ensures that the List contains only
				 * SingleVariableDeclaration
				 */
				parameters = node.parameters();
			}
			methodBlock = node.getBody();
			return false;
		}

		public Block getMethodBlock() {
			return methodBlock;
		}

		public List<SingleVariableDeclaration> getParameters() {
			return parameters;
		}
	}
	
	/**
	 * A Visitor that collects all occurrences of a 
	 * local variable. 
	 * 
	 * @author Ardit Ymeri
	 *
	 */
	private class LocalVariableUsagesASTVisitor extends ASTVisitor {
		private List<SimpleName> usages;
		private SimpleName targetName;
		
		public LocalVariableUsagesASTVisitor(SimpleName targetName) {
			usages = new ArrayList<>();
			this.targetName = targetName;
		}
		
		@Override
		public boolean visit(SimpleName node) {
			if(StringUtils.equals(node.getIdentifier(), targetName.getIdentifier())) {
				usages.add(node);
			}
			return false;
		}
		
		public List<SimpleName> getUsages() {
			return this.usages;
		}
	}
}
