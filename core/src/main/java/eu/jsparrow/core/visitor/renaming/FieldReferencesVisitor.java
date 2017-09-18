package eu.jsparrow.core.visitor.renaming;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;

import eu.jsparrow.core.util.ClassRelationUtil;

/**
 * Finds the references of the field whose name is given as a constructor parameter. 
 * Requires the names of the local variables to be provided. Distinguishes between the 
 * local variables and the fields with the same name.
 * 
 * @author Ardit Ymeri
 * @since 1.2
 *
 */
class FieldReferencesVisitor extends ASTVisitor {
	private String targetNameIdentifier;
	private ITypeBinding targetTypeBinding;
	private List<String> declaredLocalVarName;
	private List<SimpleName> references;
	private ITypeBinding parentTypeBinding;

	public FieldReferencesVisitor(SimpleName targetNode, ITypeBinding parentTypeBinding,
			ITypeBinding targetTypeBinding, List<String> declaredLocalVarNames) {
		
		this.targetNameIdentifier = targetNode.getIdentifier();
		this.declaredLocalVarName = declaredLocalVarNames;
		this.references = new ArrayList<>();
		this.parentTypeBinding = parentTypeBinding;
		this.targetTypeBinding = targetTypeBinding;
	}

	@Override
	public boolean visit(SimpleName simpleName) {
		String identifier = simpleName.getIdentifier();
		IBinding resolvedBinding = simpleName.resolveBinding();
		if (resolvedBinding != null && resolvedBinding.getKind() == IBinding.VARIABLE) {

			if (!simpleName.isDeclaration() && identifier.equals(targetNameIdentifier)
					&& ClassRelationUtil.compareITypeBinding(simpleName.resolveTypeBinding().getErasure(),
							targetTypeBinding.getErasure())) {
				ASTNode parent = simpleName.getParent();
				boolean isReference = false;

				if (parent.getNodeType() == ASTNode.QUALIFIED_NAME) {
					// the simpleName is part of a qualified name
					QualifiedName qualifiedName = ((QualifiedName) parent);
					if (simpleName == qualifiedName.getName()) {
						/*
						 * if the simpleName stands at the tail of the
						 * qualifedName
						 */
						ITypeBinding qualifierTypeBinding = ((QualifiedName) parent).getQualifier()
								.resolveTypeBinding();
						if (qualifierTypeBinding != null &&
								ClassRelationUtil.compareITypeBinding(parentTypeBinding.getErasure(), qualifierTypeBinding.getErasure())) {
							isReference = true;
						}
					} else if (!declaredLocalVarName.contains(identifier)) {
						// the simpleName is the qualifier itself
						isReference = true;
					}

				} else if (parent.getNodeType() == ASTNode.FIELD_ACCESS) {
					/*
					 * a 'field access' is an expression of the form:
					 * this.[field_name]
					 */
					ITypeBinding expressionTypeBinding = ((FieldAccess) parent).getExpression().resolveTypeBinding();
					if (expressionTypeBinding != null &&
							ClassRelationUtil.compareITypeBinding(parentTypeBinding.getErasure(), expressionTypeBinding.getErasure())) {
						isReference = true;
					}
				} else if (!declaredLocalVarName.contains(identifier)) {
					/*
					 * If not a field access and not a qualified name, then the
					 * simpleName which is not a local variable, is a field
					 * access.
					 */
					isReference = true;
				}

				if (isReference) {
					references.add(simpleName);
				}
			}
		}
		return true;
	}

	public List<SimpleName> getReferences() {
		return references;
	}
}
