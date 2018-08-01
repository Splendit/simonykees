package eu.jsparrow.rules.common.visitor.helper;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.SimpleName;

public class VariableAssignmentVisitor extends ASTVisitor {
	
	private SimpleName variableName;
	private List<Assignment> variableAssignments = new ArrayList<>();
	
	public VariableAssignmentVisitor(SimpleName variablename) {
		this.variableName = variablename;
	}
	
	@Override
	public boolean visit(Assignment assignment) {

		/*
		 * Check if the left hand-side is our guy
		 */
		Expression leftHandSide = assignment.getLeftHandSide();
		if(ASTNode.SIMPLE_NAME != leftHandSide.getNodeType()) {
			return true;
		}
		SimpleName leftHandSideName = (SimpleName) leftHandSide;
		String leftHandSideIdentifier = leftHandSideName.getIdentifier();
		if(!leftHandSideIdentifier.equals(variableName.getIdentifier())) {
			return true;
		}
		IBinding leftHandSideBinding = leftHandSideName.resolveBinding();
		if(leftHandSideBinding == null || leftHandSideBinding.getKind() != IBinding.VARIABLE) {
			return true;
		}
		
		IVariableBinding leftHandSideVariableBinding = (IVariableBinding) leftHandSideBinding;
		if(leftHandSideVariableBinding.isField() || leftHandSideVariableBinding.isParameter()) {
			return true;
		}
		
		/*
		 * Store the assignment
		 */
		variableAssignments.add(assignment);
		
		return true;
	}
	
	public List<Assignment> getAssignments() {
		return variableAssignments;
	}

}
