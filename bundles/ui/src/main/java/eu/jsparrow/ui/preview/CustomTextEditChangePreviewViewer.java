package eu.jsparrow.ui.preview;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareUI;
import org.eclipse.compare.CompareViewerSwitchingPane;
import org.eclipse.compare.IEncodedStreamContentAccessor;
import org.eclipse.compare.IResourceProvider;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.TextEditBasedChange;
import org.eclipse.ltk.core.refactoring.TextEditBasedChangeGroup;
import org.eclipse.ltk.ui.refactoring.ChangePreviewViewerInput;
import org.eclipse.ltk.ui.refactoring.IChangePreviewViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.i18n.Messages;

/**
 * A modified version of
 * {@link org.eclipse.ltk.internal.ui.refactoring.TextEditChangePreviewViewer}.
 * 
 * @implNote This is a hack to disable copying the contents of the preview
 *           wizard. Other than that, this is an exact copy of TextEditChangePreviewViewer.
 * 
 * @see SIM-1735
 * 
 * @version 3.19.0
 */
public class CustomTextEditChangePreviewViewer implements IChangePreviewViewer {

	private static final Logger logger = LoggerFactory.getLogger(CustomTextEditChangePreviewViewer.class);
	private static boolean enabled = true;

	private ComparePreviewer fViewer;

	private static class TextEditBasedChangeInput extends ChangePreviewViewerInput {
		TextEditBasedChangeGroup group;
		int surroundingLines;

		TextEditBasedChangeGroup[] groups;
		IRegion range;

		public TextEditBasedChangeInput(Change change) {
			super(change);
		}
	}

	private static class ComparePreviewer extends CompareViewerSwitchingPane {
		private CompareConfiguration fCompareConfiguration;
		private String fLabel;
		private ImageDescriptor fDescriptor;
		private Image fImage;

		public ComparePreviewer(Composite parent) {
			super(parent, SWT.BORDER | SWT.FLAT, true);
			fCompareConfiguration = new CompareConfiguration();
			fCompareConfiguration.setLeftEditable(false);
			fCompareConfiguration.setLeftLabel(Messages.CustomTextEditChangePreviewViewer_originalCode);
			fCompareConfiguration.setRightEditable(false);
			fCompareConfiguration.setRightLabel(Messages.CustomTextEditChangePreviewViewer_refactoredCode);
			addDisposeListener((DisposeEvent e) -> {
				if (fImage != null && !fImage.isDisposed())
					fImage.dispose();
			});

			Dialog.applyDialogFont(this);
		}

		public void setLabel(String label) {
			fLabel = label;
		}

		public void setImageDescriptor(ImageDescriptor imageDescriptor) {
			fDescriptor = imageDescriptor;
		}

		@Override
		protected Viewer getViewer(Viewer oldViewer, Object input) {
			Viewer viewer = CompareUI.findContentViewer(oldViewer, (ICompareInput) input, this, fCompareConfiguration);
			Control viewerControl = viewer.getControl();
			/*
			 * Here the diff view gets disabled.
			 */
			viewerControl.setEnabled(CustomTextEditChangePreviewViewer.enabled);
			return viewer;
		}

		@Override
		public void setText(String text) {
			if (fLabel != null) {
				// Updating corresponding viewer state to reflect the updated
				// text value.
				getViewer().getControl()
					.setData(CompareUI.COMPARE_VIEWER_TITLE, fLabel);
				super.setText(fLabel);
			} else {
				super.setText(text);
			}
			Image current = null;
			if (fDescriptor != null) {
				current = fImage;
				fImage = fDescriptor.createImage();
			} else {
				current = fImage;
				fImage = null;
			}
			setImage(fImage);
			if (current != null) {
				current.dispose();
			}
		}
	}

	private static class CompareElement implements ITypedElement, IEncodedStreamContentAccessor, IResourceProvider {
		// we use an encoding that preserves Unicode across the stream
		private static final String ENCODING = "UTF-8"; //$NON-NLS-1$
		private String fContent;
		private String fType;
		private IResource fResource;

		public CompareElement(String content, String type, IResource resource) {
			fContent = content;
			fType = type;
			fResource = resource;
		}

		@Override
		public String getName() {
			return "Compare element name"; //$NON-NLS-1$
		}

		@Override
		public Image getImage() {
			return null;
		}

		@Override
		public String getType() {
			return fType;
		}

		@Override
		public InputStream getContents() throws CoreException {
			try {
				return new ByteArrayInputStream(fContent.getBytes(ENCODING));
			} catch (UnsupportedEncodingException e) {
				return new ByteArrayInputStream(fContent.getBytes());
			}
		}

		@Override
		public String getCharset() {
			return ENCODING;
		}

		@Override
		public IResource getResource() {
			return fResource;
		}
	}

	public static ChangePreviewViewerInput createInput(TextEditBasedChange change) {
		return new ChangePreviewViewerInput(change);
	}

	public static ChangePreviewViewerInput createInput(Change change, TextEditBasedChangeGroup group,
			int surroundingLines) {
		TextEditBasedChangeInput input = new TextEditBasedChangeInput(change);
		input.group = group;
		input.surroundingLines = surroundingLines;
		return input;
	}

	public static ChangePreviewViewerInput createInput(Change change, TextEditBasedChangeGroup[] groups,
			IRegion range) {
		TextEditBasedChangeInput input = new TextEditBasedChangeInput(change);
		input.groups = groups;
		input.range = range;
		return input;
	}

	@Override
	public void createControl(Composite parent) {
		fViewer = new ComparePreviewer(parent);

	}

	@Override
	public Control getControl() {
		return fViewer;
	}

	@Override
	public void setInput(ChangePreviewViewerInput input) {
		try {
			Change change = input.getChange();
			if (input instanceof TextEditBasedChangeInput) {
				TextEditBasedChangeInput extended = (TextEditBasedChangeInput) input;
				if (extended.group != null && extended.surroundingLines >= 0) {
					TextEditBasedChangeGroup group = extended.group;
					TextEditBasedChange editChange = group.getTextEditChange();
					setInput(editChange,
							editChange.getCurrentContent(group.getRegion(), true, 2, new NullProgressMonitor()),
							editChange.getPreviewContent(new TextEditBasedChangeGroup[] { group }, group.getRegion(),
									true, 2, new NullProgressMonitor()),
							editChange.getTextType());
					return;
				} else if (extended.groups != null && extended.groups.length > 0 && extended.range != null) {
					TextEditBasedChange editChange = extended.groups[0].getTextEditChange();
					TextEditBasedChangeGroup[] groups = extended.groups;
					setInput(editChange,
							editChange.getCurrentContent(extended.range, true, 0, new NullProgressMonitor()),
							editChange.getPreviewContent(groups, extended.range, true, 0, new NullProgressMonitor()),
							editChange.getTextType());
					return;
				}
			} else if (change instanceof TextEditBasedChange) {
				TextEditBasedChange editChange = (TextEditBasedChange) change;
				setInput(editChange, editChange.getCurrentContent(new NullProgressMonitor()),
						editChange.getPreviewContent(new NullProgressMonitor()), editChange.getTextType());
				return;
			} else {
				fViewer.setInput(null);
			}
		} catch (CoreException | AssertionFailedException e) {
			logger.error(e.getMessage(), e);
			fViewer.setInput(null);
		}
	}

	public void refresh() {
		fViewer.getViewer()
			.refresh();
	}

	private void setInput(TextEditBasedChange change, String left, String right, String type) {
		Object element = change.getModifiedElement();
		IResource resource = null;
		if (element instanceof IAdaptable) {
			IAdaptable adaptable = (IAdaptable) element;
			IWorkbenchAdapter workbenchAdapter = adaptable.getAdapter(IWorkbenchAdapter.class);
			if (workbenchAdapter != null) {
				fViewer.setLabel(workbenchAdapter.getLabel(element));
				fViewer.setImageDescriptor(workbenchAdapter.getImageDescriptor(element));
			} else {
				fViewer.setLabel(null);
				fViewer.setImageDescriptor(null);
			}
			resource = adaptable.getAdapter(IResource.class);
		} else {
			fViewer.setLabel(null);
			fViewer.setImageDescriptor(null);
		}

		fViewer.setInput(
				new DiffNode(new CompareElement(left, type, resource), new CompareElement(right, type, resource)));
	}
	
	public static void setEnableDiffView(boolean enabledDiffView) {
		enabled = enabledDiffView;
	}
}
