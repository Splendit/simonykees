package eu.jsparrow.core.visitor.impl;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

public class ReorderModifiersASTVisitor extends AbstractASTRewriteASTVisitor {

	private static final Map<String, Integer> MODIFIER_RANKING = new HashMap<>();
	{
		MODIFIER_RANKING.put("public", 1); //$NON-NLS-1$
		MODIFIER_RANKING.put("protected", 2); //$NON-NLS-1$
		MODIFIER_RANKING.put("private", 3); //$NON-NLS-1$
		MODIFIER_RANKING.put("abstract", 4); //$NON-NLS-1$
		MODIFIER_RANKING.put("default", 5); //$NON-NLS-1$
		MODIFIER_RANKING.put("static", 6); //$NON-NLS-1$
		MODIFIER_RANKING.put("final", 7); //$NON-NLS-1$
		MODIFIER_RANKING.put("transient", 8); //$NON-NLS-1$
		MODIFIER_RANKING.put("volatile", 9); //$NON-NLS-1$
		MODIFIER_RANKING.put("synchronized", 10); //$NON-NLS-1$
		MODIFIER_RANKING.put("native", 11); //$NON-NLS-1$
		MODIFIER_RANKING.put("strictfp", 12); //$NON-NLS-1$
	}

	@Override
	public boolean visit(FieldDeclaration fieldDeclaration) {
		ListRewrite listRewrite = astRewrite.getListRewrite(fieldDeclaration, FieldDeclaration.MODIFIERS2_PROPERTY);
		List<Modifier> modifiers = ASTNodeUtil.convertToTypedList(fieldDeclaration.modifiers(), Modifier.class);
		sort(modifiers, listRewrite);
		return true;
	}

	@Override
	public boolean visit(MethodDeclaration methodDeclaration) {
		ListRewrite listRewrite = astRewrite.getListRewrite(methodDeclaration, MethodDeclaration.MODIFIERS2_PROPERTY);
		List<Modifier> modifiers = ASTNodeUtil.convertToTypedList(methodDeclaration.modifiers(), Modifier.class);
		sort(modifiers, listRewrite);
		return true;
	}

	@Override
	public boolean visit(TypeDeclaration typeDeclaration) {
		ListRewrite listRewrite = astRewrite.getListRewrite(typeDeclaration, TypeDeclaration.MODIFIERS2_PROPERTY);
		List<Modifier> modifiers = ASTNodeUtil.convertToTypedList(typeDeclaration.modifiers(), Modifier.class);
		sort(modifiers, listRewrite);
		return true;
	}

	private void sort(List<Modifier> modifiers, ListRewrite listRewrite) {
		Comparator<Modifier> comparator = (Modifier modifier1, Modifier modifier2) -> {
			ModifierKeyword key1 = modifier1.getKeyword();
			String modifierKey1 = key1.toString();
			int rank1 = MODIFIER_RANKING.getOrDefault(modifierKey1, 0);
			ModifierKeyword key2 = modifier2.getKeyword();
			String modifierKey2 = key2.toString();
			int rank2 = MODIFIER_RANKING.getOrDefault(modifierKey2, 0);
			return rank1 - rank2;
		};
		
		if(isSorted(modifiers, comparator)) {
			return;
		}
		
		modifiers.sort(comparator);
		
		for(Modifier modifier : modifiers) {
			listRewrite.insertLast((Modifier)astRewrite.createMoveTarget(modifier), null);
			onRewrite();
		}
		
		
		
	}

	private boolean isSorted(List<Modifier> modifiers, Comparator<Modifier> comparator) {
		if(modifiers.size() <= 1) {
			return true;
		}
		for(int i = 0; i<modifiers.size() - 1; i++) {
			for(int j = i+1; j < modifiers.size(); j++) {
				if(comparator.compare(modifiers.get(i), modifiers.get(j)) > 0) {
					return false;
				}
			}
		}
		return true;
	}

}
