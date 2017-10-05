package eu.jsparrow.core.visitor.sub;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;

/**
 * A Visitor that collects all occurrences of a local variable.
 * 
 * @author Ardit Ymeri
 * @since 1.2
 *
 */
public class LocalVariableUsagesASTVisitor extends ASTVisitor {
	private List<SimpleName> usages;
	private SimpleName targetName;

	public LocalVariableUsagesASTVisitor(SimpleName targetName) {
		usages = new ArrayList<>();
		this.targetName = targetName;
	}

	@Override
	public boolean visit(SimpleName node) {
		if (node.resolveBinding().getKind() == IBinding.VARIABLE
				&& StringUtils.equals(node.getIdentifier(), targetName.getIdentifier())
				&& node.getLocationInParent() != FieldAccess.NAME_PROPERTY
				&& node.getLocationInParent() != QualifiedName.NAME_PROPERTY) {
			usages.add(node);
		}
		return false;
	}

	public List<SimpleName> getUsages() {
		return this.usages;
	}
}
