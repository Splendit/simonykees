package eu.jsparrow.jdtunit.util;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;

import eu.jsparrow.jdtunit.JdtUnitException;

/**
 * 
 * @author Hans-Jörg Schnedlitz
 * @since 2.5.0
 */
public class CompilationUnitBuilder {

	private IPackageFragment packageFragment;

	private String name = "DefaultCompilationUnit.java";

	private String content = "";

	public CompilationUnitBuilder(IPackageFragment packageFragment) {
		this.packageFragment = packageFragment;
	}

	public CompilationUnitBuilder setName(String name) {
		this.name = name;
		return this;
	}

	public CompilationUnitBuilder setContent(String content) {
		this.content = content;
		return this;
	}

	public ICompilationUnit build() throws JdtUnitException {
		if (packageFragment == null) {
			throw new JdtUnitException("Package fragment is null");
		}

		ICompilationUnit result;
		try {
			result = packageFragment.createCompilationUnit(name, content, false, null);
		} catch (CoreException e) {
			throw new JdtUnitException("Failed to create compilation unit", e);
		}
		return result;
	}

}
