package at.splendit.simonykees.core.visitor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.internal.corext.dom.Bindings;

import at.splendit.simonykees.core.exception.runtime.ITypeNotFoundRuntimeException;

public abstract class AbstractCompilationUnitAstVisitor extends ASTVisitor {

	protected ASTRewrite astRewrite;
	protected List<IType> registeredITypes;

	protected AbstractCompilationUnitAstVisitor(ASTRewrite astRewrite) {
		this.astRewrite = astRewrite;
		this.registeredITypes = new ArrayList<>();
	}
	
	protected AbstractCompilationUnitAstVisitor(ASTRewrite astRewrite, List<IType> registeredITypes) {
		this(astRewrite);
		this.registeredITypes.addAll(registeredITypes);
	}

	@Override
	public boolean visit(CompilationUnit node) {
		if (node.getJavaElement() == null && node.getJavaElement().getJavaProject() == null){
			//FIXME find a better exception for the node without context
			throw new ITypeNotFoundRuntimeException();
		}
		IJavaProject iJavaProject = node.getJavaElement().getJavaProject();
		try {
			for (String javaclass : relevantClasses()) {
				IType classtype = iJavaProject.findType(javaclass);
				if (classtype != null) {
					registeredITypes.add(classtype);
				} else {
					throw new ITypeNotFoundRuntimeException();
				}
			}
		} catch (JavaModelException e) {
			throw new ITypeNotFoundRuntimeException(e);
		}
		return true;
	}
	
	protected boolean isContentofRegistertITypes(ITypeBinding iTypeBinding) {
		boolean result = false;
		if (iTypeBinding == null) {
			return false;
		}
		
		if(registeredITypes.contains(iTypeBinding.getJavaElement())){
			return true;
		}
		
		for (ITypeBinding interfaceBind : iTypeBinding.getInterfaces()) {
			if (registeredITypes.contains(interfaceBind.getJavaElement())) {
				return true;
			}
			result = result || isContentofRegistertITypes(interfaceBind.getSuperclass());
		}
		return result || isContentofRegistertITypes(iTypeBinding.getSuperclass());
	}
	
	protected String[] relevantClasses(){
		return new String[] {};
	}

}