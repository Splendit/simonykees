package eu.jsparrow.core.visitor.security;

import java.util.List;
import java.util.ArrayList;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import eu.jsparrow.rules.common.util.ASTNodeUtil;

public class SqlVariableAnalyzerVisitor extends ASTVisitor {
	
	private CompilationUnit compilationUnit;
	private SimpleName variableName;
	private ASTNode declarationFragment;
	private List<Expression> components = new ArrayList<>();
	private boolean beforeDeclaration = true;
	private boolean beforeUsage = true;
	private boolean unsafe = false;
	
	public SqlVariableAnalyzerVisitor(SimpleName variableName, ASTNode declaration) {
		this.variableName = variableName;
		this.declarationFragment = declaration;
	}
	
	@Override
	public boolean visit(VariableDeclarationFragment fragment) {
		if(this.declarationFragment == fragment) {
			beforeDeclaration = false;
			Expression initializer = fragment.getInitializer();
			if(initializer.getNodeType() == ASTNode.INFIX_EXPRESSION) {
				InfixExpression infixExpression = (InfixExpression)initializer;
				Expression left = infixExpression.getLeftOperand();
				components.add(left);
				Expression right = infixExpression.getRightOperand();
				components.add(right);
				if(infixExpression.hasExtendedOperands()) {
					components.addAll(ASTNodeUtil.convertToTypedList(infixExpression.extendedOperands(), Expression.class));
				}
			}
		}
		return false;
	}
	
	@Override
	public boolean visit(SimpleName simpleName) {
		if(beforeDeclaration) {
			return false;
		}
		
		if(simpleName == variableName) {
			beforeUsage = false;
			return false;
		}
	
		if(isNotReferenceToLocalVariable(simpleName, variableName)) {
			return false;
		}
		
		if(!beforeUsage) {
			unsafe = true;
			return false;
		}
		
		StructuralPropertyDescriptor structuralDescriptor = simpleName.getLocationInParent();
		if(structuralDescriptor == Assignment.LEFT_HAND_SIDE_PROPERTY) {
			Assignment assignment = (Assignment) simpleName.getParent();
			if(assignment.getOperator() == Assignment.Operator.PLUS_ASSIGN) {
				components.add(assignment.getRightHandSide());
			} else {
				unsafe = true;
			}
		} else {
			unsafe = true;
		}
		
		
		
		
		
		return true;
	}

	private boolean isNotReferenceToLocalVariable(SimpleName simpleName, SimpleName variableName2) {
		/* TODO
		 * Check the matching name
		 * Check if it is a variable
		 * Check if it is not part of a qualified name
		 */
		return false;
	}

}
