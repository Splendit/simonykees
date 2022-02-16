package eu.jsparrow.ui.preview;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

/**
 * This class provides the label for the tree in FileTree
 * 
 * @author Andreja Sambolec
 * @since 2.3.0
 *
 */
public class ChangeElementLabelProvider extends LabelProvider implements IFontProvider {

	@Override
	public Image getImage(Object object) {
		return null;
	}

	@Override
	public String getText(Object object) {
		if(object instanceof DocumentChangeWrapper) {
			DocumentChangeWrapper documentChangeWrapper = (DocumentChangeWrapper) object;
			if (documentChangeWrapper.isParent()) {
				return documentChangeWrapper.getOldIdentifier() + " -> " //$NON-NLS-1$
						+ documentChangeWrapper.getNewIdentifier() + "    " //$NON-NLS-1$
						+ documentChangeWrapper.getDocumentChange()
							.getName();
			} else {
				return documentChangeWrapper.getDocumentChange()
					.getName();
			}
		} else {
			RemoveUnusedCodeDocumentChangeWrapper documentChangeWrapper = (RemoveUnusedCodeDocumentChangeWrapper) object;
			if (documentChangeWrapper.isParent()) {
				return documentChangeWrapper.getIdentifier() + " - " //$NON-NLS-1$
						+ "    " //$NON-NLS-1$
						+ documentChangeWrapper.getDocumentChange()
							.getName();
			} else {
				return documentChangeWrapper.getDocumentChange()
					.getName();
			}
		}

	}

	@Override
	public Font getFont(Object element) {
		return JFaceResources.getFontRegistry()
			.getItalic(JFaceResources.DIALOG_FONT);
	}
}