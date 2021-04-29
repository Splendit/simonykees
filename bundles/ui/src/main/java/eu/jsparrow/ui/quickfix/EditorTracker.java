package eu.jsparrow.ui.quickfix;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

public abstract class EditorTracker implements IWindowListener, IPageListener, IPartListener {

	@Override
	public void partActivated(IWorkbenchPart part) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void partBroughtToTop(IWorkbenchPart part) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void partClosed(IWorkbenchPart part) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void partDeactivated(IWorkbenchPart part) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void partOpened(IWorkbenchPart part) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pageActivated(IWorkbenchPage page) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pageClosed(IWorkbenchPage page) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pageOpened(IWorkbenchPage page) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowActivated(IWorkbenchWindow window) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeactivated(IWorkbenchWindow window) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosed(IWorkbenchWindow window) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowOpened(IWorkbenchWindow window) {
		// TODO Auto-generated method stub
		
	}
	
	public abstract void editorActivated(IEditorPart part);

	public abstract void editorDeactivated(IEditorPart part);

}
