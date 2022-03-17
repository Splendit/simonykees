package eu.jsparrow.core.visitor.impl;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import eu.jsparrow.core.markers.common.MultiVariableDeclarationLineEvent;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;
import eu.jsparrow.rules.common.visitor.helper.CommentRewriter;

/**
 * Declaring multiple variables on one line is difficult to read. This Visitor
 * extracts these declarations and puts them in a single line for each variable.
 * 
 * @author Matthias Webhofer
 * @since 1.2
 *
 */
public class MultiVariableDeclarationLineASTVisitor extends AbstractASTRewriteASTVisitor
		implements MultiVariableDeclarationLineEvent {

	@Override
	public boolean visit(FieldDeclaration fieldDeclaration) {

		List<VariableDeclarationFragment> fragments = ASTNodeUtil.convertToTypedList(fieldDeclaration.fragments(),
				VariableDeclarationFragment.class);

		if (fragments.size() > 1) {

			List<FieldDeclaration> newFieldDeclarations = fragments.stream()
				.skip(1)
				.map(fragment -> {

					FieldDeclaration newFieldDeclaration = (FieldDeclaration) ASTNode.copySubtree(astRewrite.getAST(),
							fieldDeclaration);
					VariableDeclarationFragment newFragment = (VariableDeclarationFragment) astRewrite
						.createMoveTarget(fragment);

					newFieldDeclaration.fragments()
						.clear();

					ListRewrite fieldRewrite = astRewrite.getListRewrite(newFieldDeclaration,
							FieldDeclaration.FRAGMENTS_PROPERTY);
					fieldRewrite.insertLast(newFragment, null);

					return newFieldDeclaration;
				})
				.collect(Collectors.toList());

			writeNewDeclaration(fieldDeclaration, newFieldDeclarations);
		}

		return true;
	}

	@Override
	public boolean visit(VariableDeclarationStatement variableDeclarationStatement) {
		List<VariableDeclarationFragment> fragments = ASTNodeUtil
			.convertToTypedList(variableDeclarationStatement.fragments(), VariableDeclarationFragment.class);

		if (isGeneratedNode(variableDeclarationStatement.getType())) {
			return true;
		}

		if (fragments.size() > 1) {

			List<VariableDeclarationStatement> newVariableDeclarationStatements = fragments.stream()
				.skip(1)
				.map(fragment -> {

					VariableDeclarationStatement newVariableDeclarationStatement = (VariableDeclarationStatement) ASTNode
						.copySubtree(astRewrite.getAST(), variableDeclarationStatement);
					VariableDeclarationFragment newFragment = (VariableDeclarationFragment) astRewrite
						.createMoveTarget(fragment);

					newVariableDeclarationStatement.fragments()
						.clear();
					ListRewrite variableRewrite = astRewrite.getListRewrite(newVariableDeclarationStatement,
							VariableDeclarationStatement.FRAGMENTS_PROPERTY);
					variableRewrite.insertLast(newFragment, null);

					return newVariableDeclarationStatement;
				})
				.collect(Collectors.toList());

			writeNewDeclaration(variableDeclarationStatement, newVariableDeclarationStatements);
			List<Comment> nonRelatedComments = findInternalUnlinkedComments(variableDeclarationStatement, fragments);
			getCommentRewriter().saveBeforeStatement(variableDeclarationStatement, nonRelatedComments);
		}

		return true;
	}

	private List<Comment> findInternalUnlinkedComments(VariableDeclarationStatement variableDeclarationStatement,
			List<VariableDeclarationFragment> fragments) {
		CommentRewriter helper = getCommentRewriter();
		List<Comment> linkedComments = fragments.stream()
			.flatMap(fragment -> helper.findRelatedComments(fragment)
				.stream())
			.collect(Collectors.toList());

		return helper.findInternalComments(variableDeclarationStatement)
			.stream()
			.filter(comment -> !linkedComments.contains(comment))
			.collect(Collectors.toList());
	}

	/**
	 * Execution of the Refactoring of the rule
	 * 
	 * @param declaration
	 *            starting {@link ASTNode} from which the addition fragment
	 *            nodes are appended
	 * @param declarations
	 *            fragments that are added to the listRewrite List
	 */
	private void writeNewDeclaration(ASTNode declaration, List<? extends ASTNode> declarations) {
		StructuralPropertyDescriptor locationInParent = declaration.getLocationInParent();
		if (locationInParent instanceof ChildListPropertyDescriptor) {
			ListRewrite listRewrite = astRewrite.getListRewrite(declaration.getParent(),
					(ChildListPropertyDescriptor) locationInParent);
			Collections.reverse(declarations);
			declarations.forEach(field -> listRewrite.insertAfter(field, declaration, null));
			onRewrite();
			addMarkerEvent(declaration);
		}
	}
}
