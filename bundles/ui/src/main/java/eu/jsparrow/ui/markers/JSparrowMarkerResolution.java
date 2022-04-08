package eu.jsparrow.ui.markers;

import static eu.jsparrow.ui.markers.JSparrowMarkerPropertyKeys.CODE_PREVIEW_KEY;
import static eu.jsparrow.ui.markers.JSparrowMarkerPropertyKeys.HIGHLIGHT_LENGTH_KEY;
import static eu.jsparrow.ui.markers.JSparrowMarkerPropertyKeys.NAME_KEY;
import static eu.jsparrow.ui.markers.JSparrowMarkerPropertyKeys.RESOLVER_KEY;
import static eu.jsparrow.ui.markers.JSparrowMarkerPropertyKeys.WEIGHT_VALUE_KEY;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMarkerResolution2;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;

import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.markers.RefactoringEventManager;
import eu.jsparrow.ui.util.LicenseUtil;

/**
 * Provides resolution for a jSparrow marker.
 * 
 * @since 4.0.0
 *
 */
public class JSparrowMarkerResolution implements IMarkerResolution2, IJavaCompletionProposal {

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
		chargeCredit();

	}

	@Override
	public String getDescription() {
		return codePreview;
	}

	@Override
	public Image getImage() {
		return JSparrowImages.JSPARROW_ACTIVE_16;
	}

	private void chargeCredit() {
		Job job = Job.create(Messages.JSparrowMarkerResolution_reserving_payPerUseCredit, monitor -> {
			LicenseUtil licenseUtil = LicenseUtil.get();
			licenseUtil.reserveQuantity(weightValue);
			return Status.OK_STATUS;
		});
		job.setUser(false);
		job.setPriority(Job.SHORT);
		job.schedule();
	}

	@Override
	public void apply(IDocument document) {
		/*
		 * Auto generated method
		 */
	}

	@Override
	public Point getSelection(IDocument document) {
		/*
		 * Auto generated method
		 */
		return null;
	}

	@Override
	public String getAdditionalProposalInfo() {
		/*
		 * Auto generated method
		 */
		return null;
	}

	@Override
	public String getDisplayString() {
		return name;
	}

	@Override
	public IContextInformation getContextInformation() {
		/*
		 * Auto generated method
		 */
		return null;
	}

	@Override
	public int getRelevance() {
		return 10;
	}
}
