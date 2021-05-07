package eu.jsparrow.ui.quickfix;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IMarkerResolution2;

import eu.jsparrow.core.markers.MarkerManager;
import eu.jsparrow.rules.common.markers.RefactoringEventManager;

public class JSparrowMarkerResolution implements IMarkerResolution2 {

	private String label;
	private int offset;
	private IResource resource;
	private String description;
	private String name;
	
	public JSparrowMarkerResolution(IMarker marker) {
		this.label = marker.getAttribute(IMarker.MESSAGE, "jSparrow QuickFix");
		this.offset = marker.getAttribute(IMarker.CHAR_START, 0);
		this.name = marker.getAttribute("name", "jSparrow Quickfix");
		this.resource = marker.getResource();
		this.description = marker.getAttribute("description", "");
		
	}

	@Override
	public String getLabel() {
		return name;
	}

	@Override
	public void run(IMarker marker) {
		
		RefactoringEventManager eventGenerator = new MarkerManager();
		IJavaElement element = JavaCore.create(resource);
		if (element == null) {
			return;
		}

		ICompilationUnit icu = (ICompilationUnit) element.getAncestor(IJavaElement.COMPILATION_UNIT);
		if (icu == null) {
			return;
		}
		eventGenerator.resolve(icu, offset);
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
