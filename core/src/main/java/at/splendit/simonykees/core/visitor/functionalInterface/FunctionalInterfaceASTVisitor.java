package at.splendit.simonykees.core.visitor.functionalInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.splendit.simonykees.core.util.ASTNodeUtil;
import at.splendit.simonykees.core.util.ClassRelationUtil;
import at.splendit.simonykees.core.visitor.AbstractASTRewriteASTVisitor;
import at.splendit.simonykees.core.visitor.sub.LocalVariableUsagesASTVisitor;
import at.splendit.simonykees.core.visitor.sub.VariableDefinitionASTVisitor;

/**
 * Finds anonymous classes an converts it to lambdas, if they are functional
 * interfaces.
 * 
 * @author Martin Huter, Ardit Ymeri
 * @since 0.9
 *
 */
public class FunctionalInterfaceASTVisitor extends AbstractASTRewriteASTVisitor {

	Logger log = LoggerFactory.getLogger(FunctionalInterfaceASTVisitor.class);

	private Map<String, Integer> renamings = new HashMap<>();
	private CompilationUnit compilationUnit;

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		this.compilationUnit = compilationUnit;
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean visit(AnonymousClassDeclaration node) {
		if (ASTNode.CLASS_INSTANCE_CREATION == node.getParent().getNodeType()) {
			ClassInstanceCreation parentNode = (ClassInstanceCreation) node.getParent();
			Type classType = parentNode.getType();

			/*
			 * Check if the consuming part is the same type (assignment to the
			 * same type, method parameter is the same type)
			 */
			boolean allowedType = ClassRelationUtil
					.compareITypeBinding(ASTNodeUtil.getTypeBindingOfNodeUsage(parentNode), classType.resolveBinding());

			if (allowedType && ASTNode.PARAMETERIZED_TYPE != classType.getNodeType()) {
				ITypeBinding parentNodeTypeBinding = parentNode.getType().resolveBinding();
				if (parentNodeTypeBinding != null) {
					/*
					 * check that only one Method is implemented, which is the
					 * FunctionalInterfaceMethod
					 */
					if (!checkOnlyFunctionalInterfaceMethodIsImplemented(node, parentNodeTypeBinding)) {
						return false;
					}
					LambdaExpression newInitializer = node.getAST().newLambdaExpression();
					MethodBlockASTVisitor methodBlockASTVisitor = new MethodBlockASTVisitor();
					methodBlockASTVisitor.setAstRewrite(astRewrite);
					node.accept(methodBlockASTVisitor);
					Block moveBlock = methodBlockASTVisitor.getMethodBlock();

					if (moveBlock != null && isCommentFree(node, moveBlock)) {
						// find variable declarations inside the method block
						BlockVariableDeclarationsASTVisitor varDeclarationVisitor = new BlockVariableDeclarationsASTVisitor();
						moveBlock.accept(varDeclarationVisitor);
						List<SimpleName> blockLocalVarNames = varDeclarationVisitor.getBlockVariableDelcarations();

						// find parent scope and variable declarations in it
						List<ASTNode> relevantBlocks = new ArrayList<>();
						ASTNode scope = findScope(node, relevantBlocks);

						/*
						 * check if the scope is static for methods and
						 * initializer
						 * 
						 * lambdas are static resolved, anonymous classes not.
						 * this changes the behavior of use-able methods within
						 * static functions
						 */

						@SuppressWarnings("rawtypes")
						List modifiers = null;
						if (ASTNode.INITIALIZER == scope.getNodeType()) {
							modifiers = ((Initializer) scope).modifiers();
						} else if (ASTNode.METHOD_DECLARATION == scope.getNodeType()) {
							modifiers = ((MethodDeclaration) scope).modifiers();
						}
						if (modifiers != null && ASTNodeUtil.hasModifier(modifiers, modifier -> modifier.isStatic())) {
							CheckNativeMethodInvocationASTVisitor visitor = new CheckNativeMethodInvocationASTVisitor();
							node.accept(visitor);
							if (visitor.objectMethodDeclarationInvocated()) {
								return true;
							}
						}

						VariableDefinitionASTVisitor varVisistor = new VariableDefinitionASTVisitor(node,
								relevantBlocks);
						scope.accept(varVisistor);
						List<SimpleName> scopeNames = varVisistor.getScopeVariableNames();

						List<SimpleName> redeclaredNames = checkForClashingLocalVariables(scopeNames,
								blockLocalVarNames);
						renameLocalVariables(node, scopeNames, redeclaredNames);

						List<SingleVariableDeclaration> parameteres = methodBlockASTVisitor.getParameters();

						if (parameteres != null) {
							if (ASTNode.TYPE_DECLARATION != scope.getNodeType()) {
								/*
								 * if the scope is the whole class, no need to
								 * do any renaming...
								 */

								List<SimpleName> conflictingNames = findConflictingNames(parameteres, scopeNames);

								// renaming the clashing variable names
								if (!conflictingNames.isEmpty()) {
									renameLocalVariables(node, scopeNames, conflictingNames);
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
		return true;

	}

	@Override
	public boolean visit(MethodDeclaration node) {
		if (node.getParent() != null && ASTNode.TYPE_DECLARATION == node.getParent().getNodeType()) {
			renamings.clear();
		}
		// FIXME SIM-335: it is better to detected the uninitialized fields that
		// are referenced in the body.
		boolean isConstructor = node.isConstructor();

		return !isConstructor;
	}

	private List<SimpleName> checkForClashingLocalVariables(List<SimpleName> scopeNames,
			List<SimpleName> blockLocalVarNames) {

		List<String> parentScopeNames = scopeNames.stream().map(SimpleName::getIdentifier).collect(Collectors.toList());

		return blockLocalVarNames.stream().filter(simpleName -> parentScopeNames.contains(simpleName.getIdentifier()))
				.collect(Collectors.toList());
	}

	/**
	 * 
	 * @param node
	 * @param parentNodeTypeBinding
	 * @return
	 */
	private boolean checkOnlyFunctionalInterfaceMethodIsImplemented(AnonymousClassDeclaration node,
			ITypeBinding parentNodeTypeBinding) {
		if (node == null) {
			return false;
		}

		if (parentNodeTypeBinding == null || node.bodyDeclarations() == null
				|| parentNodeTypeBinding.getFunctionalInterfaceMethod() == null) {
			return false;
		}

		if (node.bodyDeclarations().size() == 1 && node.bodyDeclarations().get(0) instanceof MethodDeclaration) {
			return StringUtils.equals(parentNodeTypeBinding.getFunctionalInterfaceMethod().getName(),
					((MethodDeclaration) node.bodyDeclarations().get(0)).getName().getIdentifier());
		}

		return false;
	}

	/**
	 * Renames all occurrences of variables with conflicting names in the given
	 * anonymous class node, so that they will not clash with the existing local
	 * variables within the scope.
	 * 
	 * @param node
	 *            node representing the anonymous class declaration.
	 * @param scopeNames
	 *            variables names in the scope of the declaration the anonymous
	 *            class.
	 * @param conflictingNames
	 *            list of variable names conflicting with the existing local
	 *            variables.
	 */
	private void renameLocalVariables(AnonymousClassDeclaration node, List<SimpleName> scopeNames,
			List<SimpleName> conflictingNames) {
		LocalVariableUsagesASTVisitor visitor;
		for (SimpleName conflictingName : conflictingNames) {
			visitor = new LocalVariableUsagesASTVisitor(conflictingName);
			node.accept(visitor);
			String newName = calcNewName(scopeNames, conflictingName);
			List<SimpleName> usages = visitor.getUsages();
			for (SimpleName usage : usages) {
				astRewrite.set(usage, SimpleName.IDENTIFIER_PROPERTY, newName, null);
			}
		}
	}

	/**
	 * Computes a new variable name by appending a suffix to the existing name,
	 * so that it will not clash with any of the variable within the scope.
	 * 
	 * @param scopeVariableNames
	 *            names of the local variables within the scope.
	 * @param simpleName
	 *            current name to be changed.
	 * 
	 * @return new safe name for the variable.
	 */
	private String calcNewName(List<SimpleName> scopeVariableNames, SimpleName simpleName) {
		int suffix = 1;
		List<String> identifiers = scopeVariableNames.stream().map(SimpleName::getIdentifier)
				.collect(Collectors.toList());

		String newName;
		String currentName = simpleName.getIdentifier();
		if (renamings.containsKey(currentName)) {
			suffix = renamings.get(currentName) + 1;
		}

		boolean inalidNewName = true;
		do {
			newName = currentName + Integer.toString(suffix);
			if (!identifiers.contains(newName)) {
				inalidNewName = false;
				// keep track of the introduced new names
				renamings.put(currentName, suffix);
			}
			suffix++;
		} while (inalidNewName);

		return newName;
	}

	/**
	 * Finds the scope where the anonymous class declaration occurs. In addition
	 * it adds all its parent blocks to the {@code relevantBlocks} list.
	 */
	private ASTNode findScope(AnonymousClassDeclaration node, List<ASTNode> relevantBlocks) {
		ASTNode scope = null;
		if (node != null && relevantBlocks != null) {
			relevantBlocks.add(node);
			scope = node;
			boolean stopCondition = true;
			do {
				scope = scope.getParent();
				int scopeNodeType = scope.getNodeType();
				if (ASTNode.BLOCK == scopeNodeType || ASTNode.FOR_STATEMENT == scopeNodeType
						|| ASTNode.METHOD_DECLARATION == scopeNodeType || ASTNode.LAMBDA_EXPRESSION == scopeNodeType) {
					relevantBlocks.add(scope);
				}

				stopCondition = scope == null
						|| (scope.getNodeType() == ASTNode.METHOD_DECLARATION && scope.getParent() != null
								&& ASTNode.ANONYMOUS_CLASS_DECLARATION != scope.getParent().getNodeType())
						|| scope.getNodeType() == ASTNode.TYPE_DECLARATION
						|| scope.getNodeType() == ASTNode.INITIALIZER;
			} while (!stopCondition);
		}
		return scope;
	}

	/**
	 * Filters the names of the given {@code scopeVariableNames} that occur also
	 * in the given {@code parameters}
	 */
	private List<SimpleName> findConflictingNames(List<SingleVariableDeclaration> parameters,
			List<SimpleName> scopeVariableNames) {

		List<String> varNames = scopeVariableNames.stream().map(SimpleName::getIdentifier).distinct()
				.collect(Collectors.toList());

		return parameters.stream().map(parameter -> parameter.getName())
				.filter(parameter -> varNames.contains(parameter.getIdentifier())).collect(Collectors.toList());
	}

	private boolean isCommentFree(AnonymousClassDeclaration node, Block moveBlock) {
		boolean commentFree = false;
		if (compilationUnit != null) {
			int nodeStartPos = node.getStartPosition();
			int nodeLength = node.getLength();
			int nodeLastPos = nodeStartPos + nodeLength;
			int blockStartPos = moveBlock.getStartPosition();
			int blockEndPos = moveBlock.getStartPosition() + moveBlock.getLength();

			List<Comment> allComments = ASTNodeUtil.returnTypedList(compilationUnit.getCommentList(), Comment.class);

			boolean hasComments = allComments.stream().filter(comment -> {
				int commentStartPos = comment.getStartPosition();
				int commentLastPOs = commentStartPos + comment.getLength();
				return (commentStartPos > nodeStartPos && commentLastPOs < blockStartPos)
						|| (commentStartPos > blockEndPos && commentLastPOs < nodeLastPos);
			}).findAny().isPresent();

			commentFree = !hasComments;
		}

		return commentFree;
	}
}
