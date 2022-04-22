package eu.jsparrow.core.visitor.impl;

import java.util.Collections;
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

import eu.jsparrow.core.markers.common.ReorderModifiersEvent;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * Reorders modifiers of {@link TypeDeclaration}s, {@link FieldDeclaration}s,
 * and {@link MethodDeclaration}s according to Java Coding Conventions.
 * 
 * @since 3.6.0
 *
 */
public class ReorderModifiersASTVisitor extends AbstractASTRewriteASTVisitor implements ReorderModifiersEvent {

	private static final Map<String, Integer> MODIFIER_RANKING;
	static {
		Map<String, Integer> map = new HashMap<>();
		map.put("public", 1); //$NON-NLS-1$
		map.put("protected", 2); //$NON-NLS-1$
		map.put("private", 3); //$NON-NLS-1$
		map.put("abstract", 4); //$NON-NLS-1$
		map.put("default", 5); //$NON-NLS-1$
		map.put("static", 6); //$NON-NLS-1$
		map.put("final", 7); //$NON-NLS-1$
		map.put("transient", 8); //$NON-NLS-1$
		map.put("volatile", 9); //$NON-NLS-1$
		map.put("synchronized", 10); //$NON-NLS-1$
		map.put("native", 11); //$NON-NLS-1$
		map.put("strictfp", 12); //$NON-NLS-1$
		MODIFIER_RANKING = Collections.unmodifiableMap(map);
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
		Comparator<Modifier> comparator = (Modifier first, Modifier second) -> {
			ModifierKeyword firstKeyword = first.getKeyword();
			int rank1 = MODIFIER_RANKING.getOrDefault(firstKeyword.toString(), 0);
			ModifierKeyword secondKeyword = second.getKeyword();
			int rank2 = MODIFIER_RANKING.getOrDefault(secondKeyword.toString(), 0);
			return rank1 - rank2;
		};

		if (isSorted(modifiers, comparator)) {
			return;
		}

		modifiers.sort(comparator);

		for (Modifier modifier : modifiers) {
			listRewrite.insertLast(astRewrite.createMoveTarget(modifier), null);
		}
		onRewrite();
		addMarkerEvent(modifiers);
	}

	private boolean isSorted(List<Modifier> modifiers, Comparator<Modifier> modifierComparator) {
		if (modifiers.size() <= 1) {
			return true;
		}
		for (int i = 0; i < modifiers.size() - 1; i++) {
			for (int j = i + 1; j < modifiers.size(); j++) {
				if (modifierComparator.compare(modifiers.get(i), modifiers.get(j)) > 0) {
					return false;
				}
			}
		}
		return true;
	}

}
