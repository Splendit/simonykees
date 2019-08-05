package eu.jsparrow.core.visitor.sub;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;

/**
 * Checks if an import statement is used at least once. Supports normal single type import 
 * and normal static method imports. 
 * 
 * @since 3.8.0
 *
 */
public class UnusedImportsVisitor extends ASTVisitor {

	private List<ASTNode> excludes;
	private boolean usageFound = false;
	private String importDeclarationName;
	boolean isStatic;

	public UnusedImportsVisitor(ImportDeclaration importDeclaration, List<ASTNode> excludes) {
		this.excludes = excludes;
		Name name = importDeclaration.getName();
		this.importDeclarationName = name.getFullyQualifiedName();
		this.isStatic = importDeclaration.isStatic();
	}

	@Override
	public boolean preVisit2(ASTNode node) {
		return !usageFound;
	}

	@Override
	public boolean visit(PackageDeclaration pacakgeDeclaration) {
		return false;
	}

	@Override
	public boolean visit(ImportDeclaration importDeclaration) {
		return false;
	}
	
	@Override
	public boolean visit(MethodInvocation methodInvocation) {
		if(!isStatic) {
			return true;
		}
		SimpleName methodName = methodInvocation.getName();
		if(excludes.contains(methodName)) {
			return true;
		}
		
		IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
		if(methodBinding != null) {
			ITypeBinding declaringClass = methodBinding.getDeclaringClass();
			String fullyQualifiedMethodName = declaringClass.getQualifiedName() + "." + methodBinding.getName(); //$NON-NLS-1$
			if(fullyQualifiedMethodName.equals(importDeclarationName)) {
				usageFound = true;
				return false;
			}
		}
		
		return true;
	}
	
	
	@Override
	public boolean visit(SimpleName simpleName) {
		if (!excludes.contains(simpleName)) {
			ITypeBinding type = simpleName.resolveTypeBinding();
			if (type != null && importDeclarationName.equals(type.getQualifiedName())) {
				usageFound = true;
				return false;
			}
		}
		return true;
	}

	/**
	 * 
	 * @return if at least one usage of the given import was found. 
	 */
	public boolean isUsageFound() {
		return usageFound;
	}

}
