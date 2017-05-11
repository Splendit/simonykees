package at.splendit.simonykees.core.ui.preview;

import java.util.Set;

import org.eclipse.jdt.core.ICompilationUnit;

/**
 * Representation of an {@link ICompilationUnit} in the file view of a
 * {@link RefactoringPreviewWizardPage}.
 * <p>
 * Displays an {@link ICompilationUnit} as "[class name] - [package name]"
 * 
 * @author Ludwig Werzowa
 * @since 0.9
 */
public class CompilationUnitNode {

	private ICompilationUnit compilationUnit;

	private String displayText;

	public CompilationUnitNode(ICompilationUnit compilationUnit) {
		super();
		this.compilationUnit = compilationUnit;
		this.displayText = String.format("%s - %s", getClassName(), getPackage()); //$NON-NLS-1$
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.displayText;
	}

	public String getClassName() {
		return this.compilationUnit.getElementName();
	}

	public String getPackage() {
		return this.compilationUnit.getParent().getElementName();
	}

}
