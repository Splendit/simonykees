package eu.jsparrow.core.visitor.sub;

import static eu.jsparrow.rules.common.util.ClassRelationUtil.isContentOfType;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;

import org.eclipse.jdt.core.dom.SimpleType;

/**
 * Looks for the first occurrence of {@link SimpleType} referencing an import of
 * the given type.
 * 
 * @since 3.30.0
 *
 */
public class FirstSimpleTypeOccurrenceVisitor extends ASTVisitor {
	private final String qualifiedTypeName;
	private boolean firstOccurrenceFound;

	public FirstSimpleTypeOccurrenceVisitor(String qualifiedTypeName) {
		this.qualifiedTypeName = qualifiedTypeName;
	}

	@Override
	public boolean preVisit2(ASTNode node) {
		return !firstOccurrenceFound;
	}

	@Override
	public boolean visit(SimpleType node) {
		firstOccurrenceFound = isContentOfType(node.resolveBinding(), qualifiedTypeName);
		return !firstOccurrenceFound;
	}

	public boolean isSimpleTypeReferencingImport() {
		return firstOccurrenceFound;
	}
}