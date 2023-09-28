package eu.jsparrow.ui.treeview;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Font;

/**
 * A label provider for the {@link AbstractCheckBoxTreeViewWrapper} using
 * instances of the type {@link ICheckBoxTreeViewNode} which is sub type of
 * {@link ILabelTextProvider}.
 * 
 * @since 4.17.0
 *
 */
public class CheckBoxTreeLabelProvider extends LabelProvider implements IFontProvider {

	/**
	 * {@inheritDoc}
	 * 
	 * Note that the String returned by this method is also used as sort key
	 * because the ViewerComparator is used by the CheckboxTreeViewer in
	 * {@link AbstractCheckBoxTreeViewWrapper}, and, additionally, this string
	 * is also used as filter criterion.
	 */
	@Override
	public String getText(Object object) {
		return ((ILabelTextProvider) object).getLabelText();
	}

	@Override
	public Font getFont(Object element) {
		return JFaceResources.getFontRegistry()
			.getItalic(JFaceResources.DIALOG_FONT);
	}
}