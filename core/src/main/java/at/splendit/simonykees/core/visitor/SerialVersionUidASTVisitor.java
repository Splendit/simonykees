package at.splendit.simonykees.core.visitor;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

/**
 * Checks if the serialversionUID is static and final and adds the modifier if
 * absent
 * 
 * @author Martin Huter
 * @since 0.9.2
 *
 */
public class SerialVersionUidASTVisitor extends AbstractCompilationUnitASTVisitor {

	private static Integer COLLECTION_KEY = 1;
	private static String COLLECTION_FULLY_QUALLIFIED_NAME = "java.util.Collection"; //$NON-NLS-1$

	public SerialVersionUidASTVisitor() {
		super();
		this.fullyQuallifiedNameMap.put(COLLECTION_KEY,
				generateFullyQuallifiedNameList(COLLECTION_FULLY_QUALLIFIED_NAME));
	}

	@Override
	public boolean visit(FieldDeclaration node) {
		CheckSerialUidASTVisitor checkSerialUidASTVisitor = new CheckSerialUidASTVisitor();
		node.accept(checkSerialUidASTVisitor);
		if (checkSerialUidASTVisitor.getSerialUidNode() != null && !checkSerialUidASTVisitor.getWantedKeyWords().isEmpty()) {
			if (1 == node.fragments().size()) {
				ListRewrite modifieresRewrite = astRewrite.getListRewrite(node, FieldDeclaration.MODIFIERS2_PROPERTY);
				for(ModifierKeyword modifierKeyword : checkSerialUidASTVisitor.getWantedKeyWords()){
					modifieresRewrite.insertLast(node.getAST().newModifier(modifierKeyword), null);
				}
			}
		}
		return true;
	}

	private class CheckSerialUidASTVisitor extends ASTVisitor {

		private Set<ModifierKeyword> wantedKeyWords;
		private VariableDeclarationFragment serialUidNode = null;

		public CheckSerialUidASTVisitor() {
			wantedKeyWords = new HashSet<>();
			wantedKeyWords.add(ModifierKeyword.FINAL_KEYWORD);
			wantedKeyWords.add(ModifierKeyword.STATIC_KEYWORD);
			wantedKeyWords.add(ModifierKeyword.PRIVATE_KEYWORD);
		}
		
		@Override
		public boolean visit(VariableDeclarationFragment node) {
			if (StringUtils.equals(node.getName().getIdentifier(), "serialVersionUID")) {
				serialUidNode = node;
			}
			return true;
		}

		@Override
		public boolean visit(Modifier node) {
			wantedKeyWords.remove(node.getKeyword());
			return true;
		}
		
		public Set<ModifierKeyword> getWantedKeyWords() {
			return wantedKeyWords;
		}

		public VariableDeclarationFragment getSerialUidNode() {
			return serialUidNode;
		}
	}
}
