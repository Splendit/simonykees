package eu.jsparrow.rules.common.visitor.helper;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.WildcardType;

public class WildCardTypeVisitor extends ASTVisitor {
	private List<WildcardType> wildCardTypes = new ArrayList<>();

	@Override
	public boolean visit(WildcardType wildCard) {
		wildCardTypes.add(wildCard);
		return true;
	}

	public List<WildcardType> getWildCardTypes() {
		return this.wildCardTypes;
	}
}