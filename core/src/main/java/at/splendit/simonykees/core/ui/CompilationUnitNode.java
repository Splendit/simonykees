package at.splendit.simonykees.core.ui;

import java.util.Set;

import org.eclipse.jdt.core.ICompilationUnit;

public class CompilationUnitNode {
	
	private ICompilationUnit compilationUnit;
	
	private String displayText;

	public CompilationUnitNode(ICompilationUnit compilationUnit) {
		super();
		this.compilationUnit = compilationUnit;
		this.displayText = compilationUnit.getPath().toOSString();
	}
	
	public static Object[] createCompilationUnitNodes(Set<ICompilationUnit> compilationUnits) {
		return compilationUnits.stream().map(unit -> new CompilationUnitNode(unit)).toArray();
	}

	/**
	 * @return the compilationUnit
	 */
	public ICompilationUnit getCompilationUnit() {
		return compilationUnit;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.displayText;
	}
	
}
