package eu.jsparrow.ui.quickfix;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

public abstract class EditorTracker implements IWindowListener, IPageListener, IPartListener {
	/**
	 * <p>
	 * <strong>Must be called on the UI thread</strong>
	 * <p>
	 * Track will add listeners to workbench to track Editors.
	 */
	public void track(IWorkbench workbench) {
		for (IWorkbenchWindow workbenchWindow : workbench.getWorkbenchWindows()) {
			track(workbenchWindow);
		}
		workbench.addWindowListener(this);
	}

	private void track(IWorkbenchWindow workbenchWindow) {
		IWorkbenchPage[] pages = workbenchWindow.getPages();
		for (IWorkbenchPage page : pages) {
			page.addPartListener(this);
		}
		workbenchWindow.addPageListener(this);

		activate(workbenchWindow);
	}

	private void activate(IWorkbenchWindow workbenchWindow) {
		IWorkbenchPage activePage = workbenchWindow.getActivePage();
		if (activePage != null) {
			pageActivated(activePage);
			IEditorPart activeEditor = activePage.getActiveEditor();
			if (activeEditor != null) {
				editorActivated(activeEditor);
			}
		}
	}

	private void deactivate(IWorkbenchWindow workbenchWindow) {
		for (IWorkbenchPage page : workbenchWindow.getPages()) {
			for (IEditorReference editorReference : page.getEditorReferences()) {
				IEditorPart editor = editorReference.getEditor(false);
				if (editor != null) {
					editorDeactivated(editor);
				}
			}
		}
	}

	/**
	 * <p>
	 * <strong>Must be called on the UI thread</strong>
	 * <p>
	 * Track will add listeners to workbench to track Editors.
	 */
	public void untrack(IWorkbench workbench) {
		for (IWorkbenchWindow workbenchWindow : workbench.getWorkbenchWindows()) {
			untrack(workbenchWindow);
		}
		workbench.removeWindowListener(this);

	}

	private void untrack(IWorkbenchWindow workbenchWindow) {
		IWorkbenchPage[] pages = workbenchWindow.getPages();
		for (IWorkbenchPage page : pages) {
			page.removePartListener(this);
		}
		workbenchWindow.removePageListener(this);
		deactivate(workbenchWindow);
	}

	// --- Window listener

	@Override
	public void windowActivated(IWorkbenchWindow window) {
	}

	@Override
	public void windowDeactivated(IWorkbenchWindow window) {
	}

	@Override
	public void windowClosed(IWorkbenchWindow window) {
		untrack(window);
	}

	@Override
	public void windowOpened(IWorkbenchWindow window) {
		track(window);
	}

	// ---- IPageListener

	@Override
	public void pageActivated(IWorkbenchPage page) {
	}

	@Override
	public void pageClosed(IWorkbenchPage page) {
		page.removePartListener(this);
	}

	@Override
	public void pageOpened(IWorkbenchPage page) {
		page.addPartListener(this);
	}

	// ---- Part Listener

	@Override
	public void partActivated(IWorkbenchPart part) {
		if (part instanceof IEditorPart) {
			editorActivated((IEditorPart) part);
		}
	}

	@Override
	public void partBroughtToTop(IWorkbenchPart part) {
		if (part instanceof IEditorPart) {
			editorActivated((IEditorPart) part);
		}
	}

	@Override
	public void partClosed(IWorkbenchPart part) {
		if (part instanceof IEditorPart) {
			editorDeactivated((IEditorPart) part);
		}
	}

	@Override
	public void partDeactivated(IWorkbenchPart part) {
	}

	@Override
	public void partOpened(IWorkbenchPart part) {
	}
	
	public abstract void editorActivated(IEditorPart part);

	public abstract void editorDeactivated(IEditorPart part);

}
