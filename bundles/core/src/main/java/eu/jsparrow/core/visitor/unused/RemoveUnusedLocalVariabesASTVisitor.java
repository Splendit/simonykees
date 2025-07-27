package eu.jsparrow.core.visitor.unused;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * Removes all unused local variables which can be found in a given compilation
 * unit.
 * 
 * @since 4.9.0
 *
 */
public class RemoveUnusedLocalVariabesASTVisitor extends AbstractASTRewriteASTVisitor {

	private static final List<String> SUPPORTED_ANNOTATIONS = Collections.unmodifiableList(
			Arrays.asList(Deprecated.class.getName(), SuppressWarnings.class.getName()));
	private final Map<String, Boolean> options;

	public RemoveUnusedLocalVariabesASTVisitor(Map<String, Boolean> options) {
		this.options = options;
	}

	@Override
	public boolean visit(VariableDeclarationStatement node) {

		if (hasUnsupportedAnnotations(node)) {
			return true;
		}

		List<VariableDeclarationFragment> fragments = ASTNodeUtil.convertToTypedList(node.fragments(),
				VariableDeclarationFragment.class);

		List<ExpressionStatement> reassignmentsToRemove = new ArrayList<>();
		List<VariableDeclarationFragment> fragmentsToRemove = new ArrayList<>();
		Block scope = ASTNodeUtil.findParentBlock(node)
			.orElse(null);
		if (scope != null) {
			for (VariableDeclarationFragment fragment : fragments) {
				if (SafelyRemoveable.isSafelyRemovable(fragment, options)) {
					LocalVariablesReferencesVisitor referencesVisitor = new LocalVariablesReferencesVisitor(
							getCompilationUnit(), fragment, options);
					scope.accept(referencesVisitor);
					if (!referencesVisitor.hasActiveReference() && !referencesVisitor.hasUnresolvedReference()) {
						reassignmentsToRemove.addAll(referencesVisitor.getReassignments());
						fragmentsToRemove.add(fragment);
					}
				}
			}
		}

		if (!fragmentsToRemove.isEmpty()) {
			if (fragmentsToRemove.size() == fragments.size()) {
				astRewrite.remove(node, null);
			} else {
				fragmentsToRemove.forEach(fragment -> astRewrite.remove(fragment, null));
			}
			reassignmentsToRemove.forEach(assignment -> astRewrite.remove(assignment, null));
			onRewrite();
		}

		return true;
	}

	boolean hasUnsupportedAnnotations(VariableDeclarationStatement node) {
		return ASTNodeUtil.convertToTypedList(node.modifiers(), Annotation.class)
			.stream()
			.map(Annotation::resolveTypeBinding)
			.anyMatch(typeBinding -> !ClassRelationUtil.isContentOfTypes(typeBinding, SUPPORTED_ANNOTATIONS));
	}

}
