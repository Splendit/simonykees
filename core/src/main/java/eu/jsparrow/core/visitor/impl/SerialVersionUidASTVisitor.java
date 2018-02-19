package eu.jsparrow.core.visitor.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import eu.jsparrow.core.builder.NodeBuilder;
import eu.jsparrow.core.visitor.AbstractASTRewriteASTVisitor;
import eu.jsparrow.rules.common.util.ASTNodeUtil;

/**
 * Checks if the serialversionUID is static and final and adds the modifier if
 * absent
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
public class SerialVersionUidASTVisitor extends AbstractASTRewriteASTVisitor {

	@Override
	public boolean visit(FieldDeclaration node) {

		// test if it a primitive long, otherwise ignore this node
		if (!(node.getType()
			.isPrimitiveType() && PrimitiveType.LONG.equals(((PrimitiveType) node.getType()).getPrimitiveTypeCode()))) {
			return true;
		}

		// check if improvements can be done
		CheckSerialUidASTVisitor checkSerialUidASTVisitor = new CheckSerialUidASTVisitor();
		node.accept(checkSerialUidASTVisitor);
		if (checkSerialUidASTVisitor.getSerialUidNode() != null && !checkSerialUidASTVisitor.getWantedKeyWords()
			.isEmpty()) {

			/*
			 * only one variable is defined in this FieldDeclaration.
			 * FieldDeclaration -> (Modifiers) (Type) (Fragments)
			 */
			if (1 == node.fragments()
				.size()) {
				ListRewrite modifieresRewrite = astRewrite.getListRewrite(node, FieldDeclaration.MODIFIERS2_PROPERTY);
				checkSerialUidASTVisitor.getWantedKeyWords()
					.forEach(modifierKeyword -> modifieresRewrite.insertLast(node.getAST()
						.newModifier(modifierKeyword), null));
				onRewrite();
			}
			/*
			 * if two or more variables are defined in one statement, we split
			 * the declaration.
			 */
			else if (1 < node.fragments()
				.size()) {
				ListRewrite fragmentsRewrite = astRewrite.getListRewrite(node, FieldDeclaration.FRAGMENTS_PROPERTY);
				fragmentsRewrite.remove(checkSerialUidASTVisitor.getSerialUidNode(), null);
				VariableDeclarationFragment serialUidNode = (VariableDeclarationFragment) astRewrite
					.createMoveTarget(checkSerialUidASTVisitor.getSerialUidNode());
				List<ASTNode> newModifier = new ArrayList<>();
				ASTNodeUtil.convertToTypedList(node.modifiers(), Modifier.class)
					.stream()
					.filter(m -> m instanceof ASTNode)
					.forEach(m -> newModifier.add(astRewrite.createCopyTarget((ASTNode) m)));
				checkSerialUidASTVisitor.getWantedKeyWords()
					.stream()
					.forEach(mk -> newModifier.add(node.getAST()
						.newModifier(mk)));
				Type newType = (Type) astRewrite.createCopyTarget(node.getType());
				FieldDeclaration newField = NodeBuilder.newFieldDeclaration(node.getAST(), newType, serialUidNode,
						newModifier);
				/*
				 * a declarationfield must always be in a list of statements of
				 * the surrounding class block
				 */
				if (node.getLocationInParent() instanceof ChildListPropertyDescriptor) {
					astRewrite
						.getListRewrite(node.getParent(), (ChildListPropertyDescriptor) node.getLocationInParent())
						.insertFirst(newField, null);
					onRewrite();
				}
			}
		}
		return true;
	}

	private class CheckSerialUidASTVisitor extends ASTVisitor {

		/**
		 * A list is used to assure the order of the keywords
		 */
		private List<ModifierKeyword> wantedKeyWords;
		private VariableDeclarationFragment serialUidNode = null;

		public CheckSerialUidASTVisitor() {
			wantedKeyWords = new ArrayList<>();
			wantedKeyWords.add(ModifierKeyword.STATIC_KEYWORD);
			wantedKeyWords.add(ModifierKeyword.FINAL_KEYWORD);
		}

		@Override
		public boolean visit(VariableDeclarationFragment node) {
			if (StringUtils.equals(node.getName()
				.getIdentifier(), "serialVersionUID")) { //$NON-NLS-1$
				serialUidNode = node;
			}
			return true;
		}

		@Override
		public boolean visit(Modifier node) {
			wantedKeyWords.remove(node.getKeyword());
			return true;
		}

		public List<ModifierKeyword> getWantedKeyWords() {
			return wantedKeyWords;
		}

		public VariableDeclarationFragment getSerialUidNode() {
			return serialUidNode;
		}
	}
}
