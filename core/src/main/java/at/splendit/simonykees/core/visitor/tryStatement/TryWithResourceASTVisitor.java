package at.splendit.simonykees.core.visitor.tryStatement;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import at.splendit.simonykees.core.builder.NodeBuilder;
import at.splendit.simonykees.core.util.ClassRelationUtil;
import at.splendit.simonykees.core.visitor.AbstractCompilationUnitASTVisitor;

/**
 * The {@link TryWithResourceASTVisitor} is used to find resources in an
 * Try-Block and moves it to the resource-head of try. A resource is a source
 * that implements {@link Closeable} or {@link AutoCloseable}
 * 
 * @author Martin Huter
 * @since 0.9
 */

public class TryWithResourceASTVisitor extends AbstractCompilationUnitASTVisitor {

	private static final Integer AUTO_CLOSEABLE_KEY = 1;
	private static final String AUTO_CLOSEABLE_FULLY_QUALLIFIED_NAME = "java.lang.AutoCloseable"; //$NON-NLS-1$
	private static final String CLOSEABLE_FULLY_QUALLIFIED_NAME = "java.io.Closeable"; //$NON-NLS-1$

	public TryWithResourceASTVisitor() {
		super();
		this.fullyQuallifiedNameMap.put(AUTO_CLOSEABLE_KEY,
				generateFullyQuallifiedNameList(AUTO_CLOSEABLE_FULLY_QUALLIFIED_NAME, CLOSEABLE_FULLY_QUALLIFIED_NAME));
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean visit(TryStatement node) {
		List<VariableDeclarationExpression> resourceList = new ArrayList<>();
		List<SimpleName> resourceNameList = new ArrayList<>();
		boolean exit = false;

		List<VariableDeclarationStatement> varDeclarationStatements = 
				((List<Object>)node.getBody().statements())
				.stream()
				.filter(VariableDeclarationStatement.class::isInstance)
				.map(VariableDeclarationStatement.class::cast)
				.collect(Collectors.toList());
		
		for (VariableDeclarationStatement varDeclStatmentNode : varDeclarationStatements) {
			/*
			 * Move all AutoCloseable Object to resource header, stop collection
			 * after first non resource object
			 */
			ITypeBinding typeBind = varDeclStatmentNode.getType().resolveBinding();
			if (ClassRelationUtil.isInheritingContentOfRegistertITypes(typeBind,
					iTypeMap.get(AUTO_CLOSEABLE_KEY))) {
	
				List<VariableDeclarationFragment>fragments = 
						((List<Object>)varDeclStatmentNode.fragments())
						.stream()
						.filter(VariableDeclarationFragment.class::isInstance)
						.map(VariableDeclarationFragment.class::cast)
						.collect(Collectors.toList());
				int numFragments = fragments.size();
				
				for (VariableDeclarationFragment variableDeclarationFragment : fragments) {

					SimpleName varName = variableDeclarationFragment.getName();
					CheckAssignmentsVisitor visitor = new CheckAssignmentsVisitor(varName);
					node.accept(visitor);
					if(!visitor.foundAssignmetns()) {
						VariableDeclarationExpression variableDeclarationExpression = varDeclStatmentNode.getAST()
								.newVariableDeclarationExpression((VariableDeclarationFragment) ASTNode.copySubtree(
										variableDeclarationFragment.getAST(), variableDeclarationFragment));
						variableDeclarationExpression.setType((Type) ASTNode
								.copySubtree(varDeclStatmentNode.getAST(), varDeclStatmentNode.getType()));

						List<Modifier> modifierList = varDeclStatmentNode.modifiers();
						Function<Modifier, Modifier> cloneModifier = modifier -> (Modifier) ASTNode
								.copySubtree(modifier.getAST(), modifier);
						variableDeclarationExpression.modifiers()
								.addAll(modifierList.stream().map(cloneModifier).collect(Collectors.toList()));

						resourceList.add(variableDeclarationExpression);
						resourceNameList.add(variableDeclarationFragment.getName());
						if(numFragments > 1) {
							astRewrite.remove(variableDeclarationFragment, null);
							numFragments--;
						} else {
							astRewrite.remove(varDeclStatmentNode, null);
						}
					}
					
					// FIXME dirty hack!
					if (!resourceList.isEmpty() && node.resources().isEmpty()) {
						exit = true;
						break;
					}
				}
			} else {
				break;
			}

			// FIXME dirty hack!
			if (node.resources().isEmpty() || exit) {
				break;
			}
		}

		if (!resourceList.isEmpty()) {
			resourceList.forEach(iteratorNode -> astRewrite.getListRewrite(node, TryStatement.RESOURCES_PROPERTY)
					.insertLast(iteratorNode, null));

			// remove all close operations on the found resources
			Function<SimpleName, MethodInvocation> mapper = simpleName -> NodeBuilder.newMethodInvocation(node.getAST(),
					(SimpleName) ASTNode.copySubtree(simpleName.getAST(), simpleName),
					NodeBuilder.newSimpleName(node.getAST(), "close")); //$NON-NLS-1$

			node.accept(new RemoveCloseASTVisitor(resourceNameList.stream().map(mapper).collect(Collectors.toList())));

		}
		return true;
	}

	private class RemoveCloseASTVisitor extends ASTVisitor {

		List<MethodInvocation> methodInvocationList;
		ASTMatcher astMatcher;

		public RemoveCloseASTVisitor(List<MethodInvocation> methodInvocationList) {
			this.methodInvocationList = methodInvocationList;
			this.astMatcher = new ASTMatcher();
		}

		@Override
		public boolean visit(MethodInvocation node) {
			if (methodInvocationList.stream().anyMatch(methodInvocation -> astMatcher.match(node, methodInvocation)
					&& node.getParent() instanceof Statement)) {
				getAstRewrite().remove(node.getParent(), null);
			}
			return false;
		}

	}

	/**
	 * Looks for assignments of given {@code SimpleName}.
	 * 
	 * @author Ardit Ymeri
	 * @since 1.0
	 */
	private class CheckAssignmentsVisitor extends ASTVisitor {
		private SimpleName targetName;
		private boolean assigned = false;
		
		public CheckAssignmentsVisitor(SimpleName targetName) {
			this.targetName = targetName;
		}
		
		@Override
		public boolean visit(Assignment assignment) {
			Expression expression = assignment.getLeftHandSide();
			if(ASTNode.SIMPLE_NAME == expression.getNodeType()) {
				 SimpleName simpleName = (SimpleName)expression;
				 if(StringUtils.equals(targetName.getIdentifier(), simpleName.getIdentifier())) {
					 assigned = true;
				 }
			}
			return !assigned;
		}
		
		@Override
		public boolean preVisit2(ASTNode node) {
			return !assigned;
		}
		
		public boolean foundAssignmetns() {
			return assigned;
		}
	}
}
