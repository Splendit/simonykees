package eu.jsparrow.core.visitor.unused;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import eu.jsparrow.rules.common.util.ClassRelationUtil;

public class ReferencesVisitor extends ASTVisitor {
	
	private VariableDeclarationFragment  originalFragment;
	private TypeDeclaration originalTypeDeclaration;
	private Map<String, Boolean> options;
	private String originalIdentifier;
	
	private boolean activeReferenceFound = false;
	private List<SimpleName> reassignments = new ArrayList<>();
	private ITypeBinding originalType;

	public ReferencesVisitor(VariableDeclarationFragment  originalFragment, TypeDeclaration originalTypeDeclaration, Map<String, Boolean> options) {
		this.originalFragment = originalFragment;
		this.originalTypeDeclaration = originalTypeDeclaration;
		this.options = options;
		SimpleName name = originalFragment.getName();
		this.originalIdentifier = name.getIdentifier();
		this.originalType = this.originalTypeDeclaration.resolveBinding();
	}
	
	@Override
	public boolean visit(SimpleName simpleName) {
		/*
		 * TODO:check if the RHS of the field assignment is not causing undesirable side effects. 
		 */
		
		boolean isReference = isTargetFieldReference(simpleName);
		if (!isReference) {
			return false;
		}
		
		StructuralPropertyDescriptor locationInParent = simpleName.getLocationInParent();
		if(locationInParent == VariableDeclarationFragment.NAME_PROPERTY) {
			return false;
		}
		if(locationInParent == Assignment.LEFT_HAND_SIDE_PROPERTY) {
			reassignments.add(simpleName);
			return false;
		} else if (locationInParent == FieldAccess.NAME_PROPERTY) {
			FieldAccess fieldAccess = (FieldAccess)simpleName.getParent();
			if(fieldAccess.getLocationInParent() == Assignment.LEFT_HAND_SIDE_PROPERTY) {
				reassignments.add(simpleName);
				return false;
			}
		} else if (locationInParent == QualifiedName.NAME_PROPERTY) {
			QualifiedName qualifiedName = (QualifiedName)simpleName.getParent();
			if(qualifiedName.getLocationInParent() == Assignment.LEFT_HAND_SIDE_PROPERTY) {
				reassignments.add(simpleName);
				return false;
			}
		}
		
		activeReferenceFound = true;
		
		return true;
	}
	
	private boolean isTargetFieldReference(SimpleName simpleName) {
		String identifier = simpleName.getIdentifier();
		if (!identifier.equals(originalIdentifier)) {
			return false;
		}

		IBinding binding = simpleName.resolveBinding();
		int kind = binding.getKind();
		if(kind != IBinding.VARIABLE) {
			return false;
		}
		
		IVariableBinding variableBinding = (IVariableBinding) binding;
		if(!variableBinding.isField()) {
			return false;
		}
		ITypeBinding declaringClass = variableBinding.getDeclaringClass();
		return ClassRelationUtil.compareITypeBinding(declaringClass, originalType);
	}

	public boolean hasActiveReference() {
		return activeReferenceFound;
	}
	
	public List<SimpleName> getReassignments() {
		return this.reassignments;
	}
}
