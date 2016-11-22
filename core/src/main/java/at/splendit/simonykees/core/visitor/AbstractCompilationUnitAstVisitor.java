package at.splendit.simonykees.core.visitor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;

import at.splendit.simonykees.core.exception.runtime.ITypeNotFoundRuntimeException;
import at.splendit.simonykees.core.i18n.ExceptionMessages;

/**
 * Extended {@link AbstractASTRewriteASTVisitor} where a list of java classes
 * can be injected by fully qualified name to enable a comparison.
 * 
 * @author Martin Huter
 *
 */
public abstract class AbstractCompilationUnitAstVisitor extends AbstractASTRewriteASTVisitor {

	protected List<IType> registeredITypes;

	protected AbstractCompilationUnitAstVisitor() {
		super();
		this.registeredITypes = new ArrayList<>();
	}

	protected AbstractCompilationUnitAstVisitor(List<IType> registeredITypes) {
		this();
		this.registeredITypes.addAll(registeredITypes);
	}

	/**
	 * Find the corresponding types of the {@link #relevantClasses()} in the
	 * java project of the {@link CompilationUnit} that accepts the ASTVisitor
	 */
	@Override
	public boolean visit(CompilationUnit node) {
		if (node.getJavaElement() == null && node.getJavaElement().getJavaProject() == null) {
			throw new ITypeNotFoundRuntimeException(
					ExceptionMessages.AbstractCompilationUnitAstVisitor_compilation_unit_no_context);
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

	/**
	 * 
	 * @param iTypeBinding
	 *            Is an {@link ITypeBinding} that is compared to the list of
	 *            injected java-classes if it is related to it by polymorphism
	 * @return if the {@link ITypeBinding} is part of the registered types the return value is true
	 */
	protected boolean isInheritingContentOfRegistertITypes(ITypeBinding iTypeBinding) {
		boolean result = false;
		if (iTypeBinding == null) {
			return false;
		}

		if (registeredITypes.contains(iTypeBinding.getJavaElement())) {
			return true;
		}

		for (ITypeBinding interfaceBind : iTypeBinding.getInterfaces()) {
			if (registeredITypes.contains(interfaceBind.getJavaElement())) {
				return true;
			}
<<<<<<< 3bff72b70a2f321baf34144ed6f817fcca050209
			result = result || isInheritingContentOfRegistertITypes(interfaceBind.getSuperclass());
		}
		return result || isInheritingContentOfRegistertITypes(iTypeBinding.getSuperclass());
	}
	
	protected boolean isContentOfRegistertITypes(ITypeBinding iTypeBinding) {
		if (iTypeBinding == null) {
			return false;
		}

		if (registeredITypes.contains(iTypeBinding.getJavaElement())) {
			return true;
		}
		return false;
	}

	protected String[] relevantClasses() {
		return new String[] {};
	}

}
