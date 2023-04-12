package eu.jsparrow.core.refactorer;

import org.eclipse.jdt.core.ICompilationUnit;

/**
 * @since 4.17.0
 */
public class JavaFileNode extends JavaPackageChildNode {

	private final ICompilationUnit compilationUnit;
	private final String javaFileName;

	JavaFileNode(ICompilationUnit compilationUnit) {
		this.compilationUnit = compilationUnit;
		this.javaFileName = compilationUnit.getElementName();
	}

	public ICompilationUnit getCompilationUnit() {
		return compilationUnit;
	}

	public String getJavaFileName() {
		return javaFileName;
	}
}
