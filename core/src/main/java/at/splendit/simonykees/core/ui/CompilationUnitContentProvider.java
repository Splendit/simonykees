package at.splendit.simonykees.core.ui;

import java.util.Set;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
/**
 * 
 * @author Ludwig Werzowa
 * @since 0.9
 */
public class CompilationUnitContentProvider implements ITreeContentProvider {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		ITreeContentProvider.super.inputChanged(viewer, oldInput, newInput);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object[] getElements(Object inputElement) {
		return CompilationUnitNode.createCompilationUnitNodes((Set<ICompilationUnit>)inputElement);
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		return null;
	}

	@Override
	public Object getParent(Object element) {
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		return false;
	}

}
