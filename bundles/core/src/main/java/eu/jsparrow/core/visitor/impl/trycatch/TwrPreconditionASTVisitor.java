package eu.jsparrow.core.visitor.impl.trycatch;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import eu.jsparrow.core.visitor.sub.ReferencedVariablesASTVisitor;

/**
 * Verifies the following condition for the given {@code SimpleName}:
 * 
 * <ul>
 * <li>resource is not assigned in the try block.</li>
 * <li>resource is not closed inside some other nested try block but directly
 * inside the body.</li>
 * <li>resource is not used after it is closed.</li>
 * <li>resource initializer does not use values that could be potentially
 * manipulated between the opening of the try block and its occurrence.</li>
 * </ul>
 * 
 * @author Ardit Ymeri
 * @since 1.0
 */
class TwrPreconditionASTVisitor extends ASTVisitor {
	private SimpleName targetName;
	private boolean assigned = false;
	private boolean closeOccurred = false;
	private boolean closeOccurredInNestedTry = false;
	private boolean referencedAfterClose = false;
	private int nestedTryLevel = 0;
	private boolean reachedTargetName = false;
	private List<SimpleName> precedingSimpleNames;
	private List<SimpleName> referencedByInitializer;
	private List<VariableDeclarationFragment> toBeMovedToResources;

	private MethodInvocation closeStatement = null;

	public TwrPreconditionASTVisitor(SimpleName targetName, List<VariableDeclarationFragment> toBeMovedToResources) {
		this.targetName = targetName;
		this.toBeMovedToResources = toBeMovedToResources;
		precedingSimpleNames = new ArrayList<>();
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
		return ASTNode.TRY_STATEMENT != node.getParent()
			.getNodeType();
	}

	/**
	 * Skip declarations fragments that are already selected to be moved to
	 * resources.
	 */
	@Override
	public boolean visit(VariableDeclarationFragment node) {
		return toBeMovedToResources.stream()
			.filter(fragment -> node == fragment)
			.findFirst()
			.map(fragment -> false)
			.orElse(true);
	}

	public boolean safeToGo() {
		// check whether the variables used initializer do not show in
		// preceding simple
		// names

		return !assigned && !initializerIsDirty() && !referencedAfterClose && !closeOccurredInNestedTry
				&& closeOccurred;
	}

	private boolean initializerIsDirty() {
		List<String> precedingsNames = precedingSimpleNames.stream()
			.map(SimpleName::getIdentifier)
			.collect(Collectors.toList());

		return referencedByInitializer.stream()
			.map(SimpleName::getIdentifier)
			.anyMatch(precedingsNames::contains);

	}

	@Override
	public boolean visit(MethodInvocation invocation) {
		// looking for the 'close' method
		if (!closeOccurred && TryWithResourceASTVisitor.CLOSE.equals(invocation.getName()
			.getIdentifier()) && invocation.arguments()
				.isEmpty()) {
			Expression expression = invocation.getExpression();
			if (expression != null && ASTNode.SIMPLE_NAME == expression.getNodeType()) {
				SimpleName simpleName = (SimpleName) expression;
				if (targetName.getIdentifier()
					.equals(simpleName.getIdentifier())) {

					if (nestedTryLevel == 1) {
						closeOccurred = true;
					} else if (nestedTryLevel > 1) {
						closeOccurredInNestedTry = true;
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
				referencedByInitializer = findReferencedVariables(simpleName);
			} else if (!reachedTargetName) {
				precedingSimpleNames.add(simpleName);
			}

			if ((closeOccurred || closeOccurredInNestedTry) && simpleName.getParent() != closeStatement
					&& targetName.getIdentifier()
						.equals(simpleName.getIdentifier())) {
				referencedAfterClose = true;
			}
		}

		return true;
	}

	private static List<SimpleName> findReferencedVariables(SimpleName simpleName) {
		List<SimpleName> result = new ArrayList<>();
		ASTNode parent = simpleName.getParent();
		if (ASTNode.VARIABLE_DECLARATION_FRAGMENT == parent.getNodeType()) {
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) parent;
			Expression initExpression = fragment.getInitializer();
			if (initExpression != null) {
				ReferencedVariablesASTVisitor visitor = new ReferencedVariablesASTVisitor();
				initExpression.accept(visitor);
				result.addAll(visitor.getReferencedVariables());
			}
		}
		return result;
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
		return !assigned && !referencedAfterClose && !closeOccurredInNestedTry;
	}
}