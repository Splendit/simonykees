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
 * @since 2.3
 *
 */
public class ChangeElementLabelProvider extends LabelProvider implements IFontProvider {

	@Override
	public Image getImage(Object object) {
		return null;
	}

	@Override
	public String getText(Object object) {
		DocumentChangeWrapper documentChangeWrapper = (DocumentChangeWrapper) object;
		if (documentChangeWrapper.isParent()) {
			return documentChangeWrapper.getOldIdentifier() + " -> " + documentChangeWrapper.getNewIdentifier() + "    " //$NON-NLS-1$ //$NON-NLS-2$
					+ documentChangeWrapper.getDocumentChange().getName();
		} else {
			return documentChangeWrapper.getDocumentChange().getName();
		}
	}

	public Font getFont(Object element) {
		return JFaceResources.getFontRegistry().getItalic(JFaceResources.DIALOG_FONT);
	}
}