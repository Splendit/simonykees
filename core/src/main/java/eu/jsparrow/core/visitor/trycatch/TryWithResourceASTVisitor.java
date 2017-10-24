package eu.jsparrow.core.visitor.trycatch;

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
import org.eclipse.jdt.core.dom.IBinding;
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

import eu.jsparrow.core.builder.NodeBuilder;
import eu.jsparrow.core.util.ClassRelationUtil;
import eu.jsparrow.core.visitor.AbstractASTRewriteASTVisitor;

/**
 * The {@link TryWithResourceASTVisitor} is used to find resources in an
 * Try-Block and moves it to the resource-head of try. A resource is a source
 * that implements {@link Closeable} or {@link AutoCloseable}
 * 
 * @author Martin Huter
 * @since 0.9
 */

public class TryWithResourceASTVisitor extends AbstractASTRewriteASTVisitor {

	private static final String AUTO_CLOSEABLE_FULLY_QUALLIFIED_NAME = java.lang.AutoCloseable.class.getName();
	private static final String CLOSEABLE_FULLY_QUALLIFIED_NAME = java.io.Closeable.class.getName();
	private static final String CLOSE = "close"; //$NON-NLS-1$

	// TODO improvement for suppressed deprecation needed, see SIM-878
	@SuppressWarnings({ "unchecked", "deprecation" })
	@Override
	public boolean visit(TryStatement node) {
		List<VariableDeclarationExpression> resourceList = new ArrayList<>();
		List<SimpleName> resourceNameList = new ArrayList<>();
		boolean exit = false;

		List<VariableDeclarationStatement> varDeclarationStatements = ((List<Object>) node.getBody()
			.statements()).stream()
				.filter(VariableDeclarationStatement.class::isInstance)
				.map(VariableDeclarationStatement.class::cast)
				.collect(Collectors.toList());
		List<VariableDeclarationFragment> toBeMovedToResources = new ArrayList<>();

		for (VariableDeclarationStatement varDeclStatmentNode : varDeclarationStatements) {
			/*
			 * Move all AutoCloseable Object to resource header, stop collection after first
			 * non resource object
			 */
			ITypeBinding typeBind = varDeclStatmentNode.getType().resolveBinding();
			if (ClassRelationUtil.isInheritingContentOfTypes(typeBind, generateFullyQualifiedNameList(
					AUTO_CLOSEABLE_FULLY_QUALLIFIED_NAME, CLOSEABLE_FULLY_QUALLIFIED_NAME))) {

				List<VariableDeclarationFragment> fragments = ((List<Object>) varDeclStatmentNode.fragments()).stream()
						.filter(VariableDeclarationFragment.class::isInstance)
						.map(VariableDeclarationFragment.class::cast).collect(Collectors.toList());
				int numFragments = fragments.size();

				for (VariableDeclarationFragment variableDeclarationFragment : fragments) {

					SimpleName varName = variableDeclarationFragment.getName();

					VerifyRuleConditionVisitor visitor = new VerifyRuleConditionVisitor(varName, toBeMovedToResources);
					node.accept(visitor);

					if (variableDeclarationFragment.getInitializer() != null && visitor.safeToGo()) {

						toBeMovedToResources.add(variableDeclarationFragment);
						VariableDeclarationExpression variableDeclarationExpression = varDeclStatmentNode.getAST()
								.newVariableDeclarationExpression((VariableDeclarationFragment) ASTNode.copySubtree(
										variableDeclarationFragment.getAST(), variableDeclarationFragment));
						variableDeclarationExpression.setType((Type) ASTNode.copySubtree(varDeclStatmentNode.getAST(),
								varDeclStatmentNode.getType()));

						List<Modifier> modifierList = varDeclStatmentNode.modifiers();
						Function<Modifier, Modifier> cloneModifier = modifier -> (Modifier) ASTNode
								.copySubtree(modifier.getAST(), modifier);
						variableDeclarationExpression.modifiers()
								.addAll(modifierList.stream().map(cloneModifier).collect(Collectors.toList()));

						resourceList.add(variableDeclarationExpression);
						resourceNameList.add(variableDeclarationFragment.getName());

						if (numFragments > 1) {
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
			if (exit) {
				break;
			}
		}

		if (!resourceList.isEmpty()) {
			resourceList.forEach(iteratorNode -> astRewrite.getListRewrite(node, TryStatement.RESOURCES_PROPERTY)
					.insertLast(iteratorNode, null));
			onRewrite();

			// remove all close operations on the found resources
			Function<SimpleName, MethodInvocation> mapper = simpleName -> NodeBuilder.newMethodInvocation(node.getAST(),
					(SimpleName) ASTNode.copySubtree(simpleName.getAST(), simpleName),
					NodeBuilder.newSimpleName(node.getAST(), CLOSE));

			node.accept(new RemoveCloseASTVisitor(resourceNameList.stream().map(mapper).collect(Collectors.toList())));

		}
		return true;
	}

	private class ReferencedVariablesASTVisitor extends ASTVisitor {
		private List<SimpleName> referencedVariables;

		public ReferencedVariablesASTVisitor() {
			referencedVariables = new ArrayList<>();
		}

		@Override
		public boolean visit(SimpleName simpleName) {
			if (simpleName.resolveBinding().getKind() == IBinding.VARIABLE) {
				referencedVariables.add(simpleName);
			}
			return true;
		}

		public List<SimpleName> getReferencedVariables() {
			return referencedVariables;
		}
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
				node.resolveMethodBinding().getExceptionTypes();
				getASTRewrite().remove(node.getParent(), null);
			}
			return false;
		}

	}

	/**
	 * Verifies the following condition for the given {@code SimpleName}:
	 * 
	 * - resource is not assigned in the try block. - resource is not closed inside
	 * some other nested try block but directly inside the body. - resource is not
	 * used after it is closed. - resource initializer does not use values that
	 * could be potentially manipulated between the opening of the try block and its
	 * occurrence.
	 * 
	 * @author Ardit Ymeri
	 * @since 1.0
	 */
	private class VerifyRuleConditionVisitor extends ASTVisitor {
		private SimpleName targetName;
		private boolean assigned = false;
		private boolean closeOccurred = false;
		private boolean closeOccuredInNestedTry = false;
		private boolean referencedAfterClose = false;
		private int nestedTryLevel = 0;
		private boolean reachedTargetName = false;
		private List<SimpleName> proceedingSimpleNames;
		private List<SimpleName> referencedByInitializer;
		private List<VariableDeclarationFragment> toBeMovedToResources;

		private MethodInvocation closeStatement = null;

		public VerifyRuleConditionVisitor(SimpleName targetName,
				List<VariableDeclarationFragment> toBeMovedToResources) {
			this.targetName = targetName;
			this.toBeMovedToResources = toBeMovedToResources;
			proceedingSimpleNames = new ArrayList<>();
		}

		@Override
		public boolean visit(Assignment assignment) {
			Expression expression = assignment.getLeftHandSide();
			if (ASTNode.SIMPLE_NAME == expression.getNodeType()) {
				SimpleName simpleName = (SimpleName) expression;
				if (StringUtils.equals(targetName.getIdentifier(), simpleName.getIdentifier())) {
					assigned = true;
				}
			}
			return !assigned;
		}

		/**
		 * Skip the variables declared in the resource part.
		 */
		@Override
		public boolean visit(VariableDeclarationExpression node) {
			return ASTNode.TRY_STATEMENT != node.getParent().getNodeType();
		}

		/**
		 * Skip declarations fragments that are already selected to be moved to
		 * resources.
		 */
		@Override
		public boolean visit(VariableDeclarationFragment node) {
			return toBeMovedToResources.stream().filter(fragment -> node == fragment).findFirst().map(fragment -> false).orElse(true);
		}

		public boolean safeToGo() {
			// check whether the variables used initializer do not show in proceeding simple
			// names

			return !assigned && !initializerIsDirty() && !referencedAfterClose && !closeOccuredInNestedTry
					&& closeOccurred;
		}

		private boolean initializerIsDirty() {
			List<String> proceedingsNames = proceedingSimpleNames.stream().map(SimpleName::getIdentifier)
					.collect(Collectors.toList());

			return referencedByInitializer.stream().map(SimpleName::getIdentifier).anyMatch(proceedingsNames::contains);

		}

		@Override
		public boolean visit(MethodInvocation invocation) {
			// looking for the 'close' method
			if (!closeOccurred && CLOSE.equals(invocation.getName().getIdentifier())
					&& invocation.arguments().isEmpty()) {
				Expression expression = invocation.getExpression();
				if (ASTNode.SIMPLE_NAME == expression.getNodeType()) {
					SimpleName simpleName = (SimpleName) expression;
					if (targetName.getIdentifier().equals(simpleName.getIdentifier())) {

						if (nestedTryLevel == 1) {
							closeOccurred = true;
						} else if (nestedTryLevel > 1) {
							closeOccuredInNestedTry = true;
						}
						closeStatement = invocation;
					}
				}
			}

			return true;
		}

		@Override
		public boolean visit(SimpleName simpleName) {
			IBinding binding = simpleName.resolveBinding();
			if (binding != null && binding.getKind() == IBinding.VARIABLE) {
				if (simpleName == targetName) {
					reachedTargetName = true;
					ASTNode parent = simpleName.getParent();
					if (ASTNode.VARIABLE_DECLARATION_FRAGMENT == parent.getNodeType()) {
						VariableDeclarationFragment fragment = (VariableDeclarationFragment) parent;
						Expression initExpression = fragment.getInitializer();
						if (initExpression != null) {
							referencedByInitializer = findReferencedVariables(initExpression);
						}
					}
				} else if (!reachedTargetName) {
					proceedingSimpleNames.add(simpleName);
				}

				if ((closeOccurred || closeOccuredInNestedTry) && simpleName.getParent() != closeStatement
						&& targetName.getIdentifier().equals(simpleName.getIdentifier())) {
					referencedAfterClose = true;
				}
			}

			return true;
		}

		private List<SimpleName> findReferencedVariables(Expression initExpression) {
			ReferencedVariablesASTVisitor visitor = new ReferencedVariablesASTVisitor();
			initExpression.accept(visitor);
			return visitor.getReferencedVariables();
		}

		@Override
		public boolean visit(TryStatement node) {
			nestedTryLevel += 1;
			return true;
		}

		@Override
		public void endVisit(TryStatement node) {
			nestedTryLevel -= 1;
		}

		@Override
		public boolean preVisit2(ASTNode node) {
			// stop if any of the following...
			return !assigned && !referencedAfterClose && !closeOccuredInNestedTry;
		}
	}
}
