package at.splendit.simonykees.core.ui.preview;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.ltk.core.refactoring.DocumentChange;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

public class ChangeElementLabelProvider extends LabelProvider implements IFontProvider {

	@Override
	public Image getImage(Object object) {
		return null;
	}

	@Override
	public String getText(Object object) {
		if (object instanceof DocumentChange) {
			return ((DocumentChange) object).getName();
		}
		return ((DocumentChangeWrapper) object).getDocumentChange().getName();
	}

	public Font getFont(Object element) {
		return JFaceResources.getFontRegistry().getItalic(JFaceResources.DIALOG_FONT);
	}
}