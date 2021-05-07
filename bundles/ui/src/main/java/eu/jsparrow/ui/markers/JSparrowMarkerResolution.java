package eu.jsparrow.ui.markers;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IMarkerResolution2;

import eu.jsparrow.core.markers.CoreRefactoringEventManager;
import eu.jsparrow.rules.common.markers.RefactoringEventManager;

public class JSparrowMarkerResolution implements IMarkerResolution2 {

	private int offset;
	private IResource resource;
	private String description;
	private String name;
	private String resolver;

	public JSparrowMarkerResolution(IMarker marker) {
		this.offset = marker.getAttribute(IMarker.CHAR_START, 0);
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
		RefactoringEventManager eventGenerator = new CoreRefactoringEventManager();
		IJavaElement element = JavaCore.create(resource);
		if (element == null) {
			return;
		}

		ICompilationUnit icu = (ICompilationUnit) element.getAncestor(IJavaElement.COMPILATION_UNIT);
		if (icu == null) {
			return;
		}
		eventGenerator.resolve(icu, this.resolver, this.offset);
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
