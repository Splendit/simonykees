package eu.jsparrow.core.visitor.unused;

import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.CompilationUnit;

import eu.jsparrow.core.visitor.renaming.JavaAccessModifier;

/**
 * 
 * @since 4.9.0
 *
 */
public interface UnusedClassMemberWrapper {

	CompilationUnit getCompilationUnit();
	
	JavaAccessModifier getAccessModifier();
	
	IPath getDeclarationPath();
	
	String getClassMemberIdentifier();
	
	String getClassDeclarationName();
	
	List<ICompilationUnit> getTargetICompilationUnits();
}
