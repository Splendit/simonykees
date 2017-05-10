package at.splendit.simonykees.core.visitor;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import at.splendit.simonykees.core.util.ASTNodeUtil;

/**
 * Declaring multiple variables on one line is difficult to read. This Visitor
 * extracts these declarations and puts them in a single line for each variable.
 * 
 * @author Matthias Webhofer
 * @since 1.2
 *
 */
public class MultiVariableDeclarationLineASTVisitor extends AbstractASTRewriteASTVisitor {

	@Override
	public boolean visit(FieldDeclaration fieldDeclaration) {

		List<VariableDeclarationFragment> fragments = ASTNodeUtil.convertToTypedList(fieldDeclaration.fragments(),
				VariableDeclarationFragment.class);

		ASTRewrite astRewrite = getAstRewrite();
		AST ast = astRewrite.getAST();

		if (fragments.size() > 1) {

			List<FieldDeclaration> newFieldDeclarations = fragments.stream().skip(1).map(fragment -> {

				FieldDeclaration newFieldDeclaration = (FieldDeclaration) ASTNode.copySubtree(ast, fieldDeclaration);
				VariableDeclarationFragment newFragment = (VariableDeclarationFragment) astRewrite
						.createMoveTarget(fragment);

				newFieldDeclaration.fragments().clear();

				ListRewrite fieldRewrite = astRewrite.getListRewrite(newFieldDeclaration,
						FieldDeclaration.FRAGMENTS_PROPERTY);
				fieldRewrite.insertLast(newFragment, null);

				return newFieldDeclaration;
			}).collect(Collectors.toList());

			ChildListPropertyDescriptor propertyDescriptor;

			ASTNode parent = fieldDeclaration.getParent();

			if (parent instanceof EnumDeclaration) {
				propertyDescriptor = EnumDeclaration.BODY_DECLARATIONS_PROPERTY;
			} else if (parent instanceof AnnotationTypeDeclaration) {
				propertyDescriptor = AnnotationTypeDeclaration.BODY_DECLARATIONS_PROPERTY;
			} else {
				propertyDescriptor = TypeDeclaration.BODY_DECLARATIONS_PROPERTY;
			}

			ListRewrite listRewrite = astRewrite.getListRewrite(parent, propertyDescriptor);
			Collections.reverse(newFieldDeclarations);
			newFieldDeclarations.forEach(field -> listRewrite.insertAfter(field, fieldDeclaration, null));
		}

		return true;
	}

	@Override
	public boolean visit(VariableDeclarationStatement variableDeclarationStatement) {
		List<VariableDeclarationFragment> fragments = ASTNodeUtil
				.convertToTypedList(variableDeclarationStatement.fragments(), VariableDeclarationFragment.class);

		ASTRewrite astRewrite = getAstRewrite();
		AST ast = astRewrite.getAST();

		if (fragments.size() > 1) {

			List<VariableDeclarationStatement> newVariableDeclarationStatements = fragments.stream().skip(1)
					.map(fragment -> {

						VariableDeclarationStatement newVariableDeclarationStatement = (VariableDeclarationStatement) ASTNode
								.copySubtree(ast, variableDeclarationStatement);
						VariableDeclarationFragment newFragment = (VariableDeclarationFragment) astRewrite
								.createMoveTarget(fragment);

						newVariableDeclarationStatement.fragments().clear();
						ListRewrite variableRewrite = astRewrite.getListRewrite(newVariableDeclarationStatement,
								VariableDeclarationStatement.FRAGMENTS_PROPERTY);
						variableRewrite.insertLast(newFragment, null);

						return newVariableDeclarationStatement;
					}).collect(Collectors.toList());

			ListRewrite listRewrite = astRewrite.getListRewrite(variableDeclarationStatement.getParent(),
					Block.STATEMENTS_PROPERTY);
			Collections.reverse(newVariableDeclarationStatements);
			newVariableDeclarationStatements
					.forEach(var -> listRewrite.insertAfter(var, variableDeclarationStatement, null));
		}

		return true;
	}
}
