package eu.jsparrow.ui.markers;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMarkerResolution2;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;

import eu.jsparrow.rules.common.markers.RefactoringEventManager;

public class JSparrowMarkerResolution implements IMarkerResolution2 {

	private int offset;
	private int newLength;
	private IResource resource;
	private String description;
	private String name;
	private String resolver;
	private RefactoringEventManager eventGenerator;

	public JSparrowMarkerResolution(IMarker marker, RefactoringEventManager eventGenerator) {
		this.eventGenerator = eventGenerator;
		this.offset = marker.getAttribute(IMarker.CHAR_START, 0);
		this.newLength = marker.getAttribute("newLength", offset); //$NON-NLS-1$
		this.name = marker.getAttribute("name", "jSparrow Quickfix"); //$NON-NLS-1$ //$NON-NLS-2$
		this.resource = marker.getResource();
		this.description = marker.getAttribute("description", ""); //$NON-NLS-1$ //$NON-NLS-2$
		this.resolver = marker.getAttribute("resolver", ""); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public String getLabel() {
		return name;
	}

	@Override
	public void run(IMarker marker) {

		IJavaElement element = JavaCore.create(resource);
		if (element == null) {
			return;
		}

		ICompilationUnit icu = (ICompilationUnit) element.getAncestor(IJavaElement.COMPILATION_UNIT);
		if (icu == null) {
			return;
		}
		eventGenerator.resolve(icu, this.resolver, this.offset);
		IWorkbenchPage page = PlatformUI.getWorkbench()
			.getActiveWorkbenchWindow()
			.getActivePage();
		IEditorPart editor = page.getActiveEditor();
		ITextEditor textEditor = editor.getAdapter(ITextEditor.class);
		textEditor.selectAndReveal(this.offset, this.newLength);
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public Image getImage() {
		return JSparrowImages.JSPARROW_ACTIVE_16;
	}

}
