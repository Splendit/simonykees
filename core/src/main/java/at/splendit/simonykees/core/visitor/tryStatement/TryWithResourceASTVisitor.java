package at.splendit.simonykees.core.visitor.tryStatement;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
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
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

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
		for (Object statementIterator : node.getBody().statements()) {
			// Move all AutoCloseable Object to resource header, stop collection
			// after first non resource object
			if (statementIterator instanceof VariableDeclarationStatement) {
				VariableDeclarationStatement varDeclStatmentNode = (VariableDeclarationStatement) statementIterator;
				ITypeBinding typeBind = varDeclStatmentNode.getType().resolveBinding();
				if (ClassRelationUtil.isInheritingContentOfRegistertITypes(typeBind,
						iTypeMap.get(AUTO_CLOSEABLE_KEY))) {
					for (Object iterator : varDeclStatmentNode.fragments()) {
						if (iterator instanceof VariableDeclarationFragment) {
							VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) iterator;
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
						}
					}
					astRewrite.remove(varDeclStatmentNode, null);
				} else {
					break;
				}
			} else {
				break;
			}

			// FIXME dirty hack!
			if (node.resources().isEmpty()) {
				break;
			}
		}

		if (!resourceList.isEmpty()) {
			// Add Resources to try head
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

}
