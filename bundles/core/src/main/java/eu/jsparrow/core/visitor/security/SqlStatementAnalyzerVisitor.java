package eu.jsparrow.core.visitor.security;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

public class SqlStatementAnalyzerVisitor extends ASTVisitor {
	
	private ASTNode declaration;
	private SimpleName statementName;
	private Expression initializer;
	private CompilationUnit compilationUnit;
	private MethodInvocation getResultSet;
	private boolean unsafe = false;
	private boolean beforeDeclaration = true;
	private boolean beforeUsage = true;
	
	public SqlStatementAnalyzerVisitor(ASTNode declaration, SimpleName sqlStatement, CompilationUnit compilationUnit) {
		this.declaration = declaration;
		this.statementName = sqlStatement;
		this.compilationUnit = compilationUnit;
	}
	
	@Override 
	public boolean visit(VariableDeclarationFragment declarationFragment) {
		if(this.declaration == declarationFragment) {
			beforeDeclaration = false;
			Expression statementInitializer = declarationFragment.getInitializer();
			if(initializer != null && initializer.getNodeType() != ASTNode.NULL_LITERAL) {
				this.initializer = statementInitializer;
			}
			return false;
		}
		return true;
	}
	
	@Override
	public boolean visit(Assignment assignment) {
		if(beforeDeclaration) {
			return false;
		}
		if(initializer == null) {
			Expression left = assignment.getLeftHandSide();
			if(left.getNodeType() == ASTNode.SIMPLE_NAME) {
				SimpleName leftName = (SimpleName)left;
				if(leftName.getIdentifier().equals(this.statementName.getIdentifier())) {
					ASTNode leftDeclaration = compilationUnit.findDeclaringNode(leftName.resolveBinding());
					if(leftDeclaration == this.declaration) {
						Expression right = assignment.getRightHandSide();
						if(right.getNodeType() != ASTNode.NULL_LITERAL) {
							this.initializer = right;
							return false;
						}
					}
				}
			}
		}
		
		return true;
	}
	
	@Override
	public boolean visit(SimpleName simpleName) {
		if(beforeDeclaration) {
			return false;
		}
		
		if(simpleName == statementName) {
			beforeUsage = false;
			return false;
		}
		
		if(simpleName.getIdentifier().equals(this.statementName.getIdentifier())) {
			ASTNode declaringNode = compilationUnit.findDeclaringNode(simpleName.resolveBinding());
			if(declaringNode == this.declaration) {
				if(beforeUsage) {
					unsafe = true;
				} else {
					if(this.getResultSet != null) {
						unsafe = true;
					} else {
						MethodInvocation getResultSet = findGetResultSet(simpleName);
						if(getResultSet != null) {
							this.getResultSet = getResultSet;
						} else {
							unsafe = true;
						}
					}
				}
			}
		}
		
		return true;
	}
	
	
	private MethodInvocation findGetResultSet(SimpleName simpleName) {
		StructuralPropertyDescriptor structuralDescriptor = simpleName.getLocationInParent();
		if(structuralDescriptor == MethodInvocation.EXPRESSION_PROPERTY) {
			MethodInvocation methodInvocation = (MethodInvocation)simpleName.getParent();
			SimpleName methodName = methodInvocation.getName();
			if("getResultSet".equals(methodName.getIdentifier())) { //$NON-NLS-1$
				return methodInvocation;
			}
		}
		return null;
	}

	public boolean isUnsafe() {
		return unsafe;
	}
	
	public Expression getInitializer() {
		return initializer;
	}

}
