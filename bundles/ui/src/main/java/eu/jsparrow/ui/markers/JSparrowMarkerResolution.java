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
import eu.jsparrow.ui.util.LicenseUtil;

import static eu.jsparrow.ui.markers.JSparrowMarkerPropertyKeys.*;

/**
 * Provides resolution for a jSparrow marker. 
 * 
 * @since 4.0.0
 *
 */
public class JSparrowMarkerResolution implements IMarkerResolution2 {

	private int offset;
	private int newLength;
	private IResource resource;
	private String codePreview;
	private String name;
	private String resolver;
	private RefactoringEventManager refactoringEventManager;
	private int weightValue;

	public JSparrowMarkerResolution(IMarker marker, RefactoringEventManager eventManager) {
		this.refactoringEventManager = eventManager;
		this.offset = marker.getAttribute(IMarker.CHAR_START, 0);
		this.newLength = marker.getAttribute(HIGHLIGHT_LENGTH_KEY, offset);
		this.name = marker.getAttribute(NAME_KEY, "jSparrow Quickfix"); //$NON-NLS-1$
		this.resource = marker.getResource();
		this.codePreview = marker.getAttribute(CODE_PREVIEW_KEY, ""); //$NON-NLS-1$
		this.resolver = marker.getAttribute(RESOLVER_KEY, ""); //$NON-NLS-1$
		this.weightValue = marker.getAttribute(WEIGHT_VALUE_KEY, 1);
		
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
		refactoringEventManager.resolve(icu, this.resolver, this.offset);
		IWorkbenchPage page = PlatformUI.getWorkbench()
			.getActiveWorkbenchWindow()
			.getActivePage();
		IEditorPart editor = page.getActiveEditor();
		ITextEditor textEditor = editor.getAdapter(ITextEditor.class);
		textEditor.selectAndReveal(this.offset, this.newLength);
		// TODO: update the pay per use license model. Get the cost number from the marker properties?
		LicenseUtil licenseUtil = LicenseUtil.get();
		// TODO: run async
		licenseUtil.reserveQuantity(weightValue);
		
	}

	@Override
	public String getDescription() {
		return codePreview;
	}

	@Override
	public Image getImage() {
		return JSparrowImages.JSPARROW_ACTIVE_16;
	}

}
