package eu.jsparrow.core.visitor.sub;

import static eu.jsparrow.rules.common.util.ClassRelationUtil.isContentOfType;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;

import org.eclipse.jdt.core.dom.SimpleType;

/**
 * Looks for the first occurrence of {@link SimpleType} referencing an import of
 * the given type.
 *
 */
public class SimpleTypeReferencingImportVisitor extends ASTVisitor {
	private final String qualifiedTypeName;
	private boolean simpleTypeReferencingImport;

	public SimpleTypeReferencingImportVisitor(String qualifiedTypeName) {
		this.qualifiedTypeName = qualifiedTypeName;
	}

	@Override
	public boolean preVisit2(ASTNode node) {
		return !simpleTypeReferencingImport;
	}

	@Override
	public boolean visit(SimpleType node) {
		simpleTypeReferencingImport = isContentOfType(node.resolveBinding(), qualifiedTypeName);
		return !simpleTypeReferencingImport;
	}

	public boolean isSimpleTypeReferencingImport() {
		return simpleTypeReferencingImport;
	}
}