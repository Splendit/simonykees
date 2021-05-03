package eu.jsparrow.ui.quickfix;

import java.net.URL;
import java.util.HashMap;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IMarkerResolution2;
import org.osgi.framework.Bundle;

import eu.jsparrow.core.markers.EventProducer;
import eu.jsparrow.rules.common.RefactoringEventProducer;
import eu.jsparrow.ui.Activator;

public class QuickFix implements IMarkerResolution2 {

	private String label;
	private int offset;
	
	QuickFix(IMarker marker) {
		this.label = marker.getAttribute(IMarker.MESSAGE, "jSparrow QuickFix");
		this.offset = marker.getAttribute(IMarker.CHAR_START, 0);
		
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public void run(IMarker marker) {
		
		RefactoringEventProducer eventGenerator = new EventProducer();
		IResource resource = marker.getResource();
		IJavaElement element = JavaCore.create(resource);
		if (element == null) {
			return;
		}

		ICompilationUnit icu = (ICompilationUnit) element.getAncestor(IJavaElement.COMPILATION_UNIT);
		if (icu == null) {
			return;
		}
		Integer offset = marker.getAttribute(IMarker.CHAR_START, this.offset);
		
		eventGenerator.resolve(icu, offset);
	}

	@Override
	public String getDescription() {
		// TODO: set a new property in the marker for 
		return "Issue description: " + label;
	}

	@Override
	public Image getImage() {
		//TODO: make the image static
		Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);
		IPath iPathActive = new Path("icons/jSparrow_active_icon_16.png");
		URL urlActive = FileLocator.find(bundle, iPathActive, new HashMap<>());
		ImageDescriptor imageDesc = ImageDescriptor.createFromURL(urlActive);
		return imageDesc.createImage();
	}

}
