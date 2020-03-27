package eu.jsparrow.core.visitor.security;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

/**
 * 
 * 
 * @since 3.16.0
 *
 */
public class SqlStatementAnalyzerVisitor extends ASTVisitor {
	
	private ASTNode declaration;
	private SimpleName statementName;
	private Expression initializer;
	private CompilationUnit compilationUnit;
	private MethodInvocation getResultSetInvocation;
	private boolean unsafe = false;
	private boolean beforeDeclaration = true;
	
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
	
	private boolean isStatementReference(Expression expression) {
		if(expression.getNodeType() != ASTNode.SIMPLE_NAME) {
			return false;
		}
		SimpleName simpleName = (SimpleName)expression;
		if(!simpleName.getIdentifier().equals(statementName.getIdentifier())) {
			return false;
		}
		IBinding binding = simpleName.resolveBinding();
		ASTNode declaringNode = compilationUnit.findDeclaringNode(binding);
		return declaringNode == declaration;
	}
	
	@Override
	public boolean visit(Assignment assignment) {
		if(beforeDeclaration) {
			return false;
		}
		if(initializer != null) {
			return true;
		}
			
		Expression left = assignment.getLeftHandSide();
		if(isStatementReference(left)) {
			Expression right = assignment.getRightHandSide();
			if(right.getNodeType() != ASTNode.NULL_LITERAL) {
				this.initializer = right;
				return false;
			}
		}
		
		Expression right = assignment.getRightHandSide();
		if(isStatementReference(right)) {
					unsafe = true;
		}
		
		return true;
	}
	
	@Override
	public boolean visit(SimpleName simpleName) {
		if(beforeDeclaration) {
			return false;
		}
		
		if(simpleName == statementName) {
			return false;
		}
		
		if(isStatementReference(simpleName)) {
			MethodInvocation getResultSet2 = findGetResultSet(simpleName);
			if(getResultSet2 != null) {
				if(this.getResultSetInvocation == null) {
					this.getResultSetInvocation = getResultSet2;
				} else {
					unsafe = true;
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
	
	public MethodInvocation getGetResultSetInvocation() {
		return this.getResultSetInvocation;
	}

}
