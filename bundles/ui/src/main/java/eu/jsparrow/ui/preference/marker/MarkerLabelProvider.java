package eu.jsparrow.ui.preference.marker;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Font;

public class MarkerLabelProvider extends LabelProvider implements IFontProvider {

	@Override
	public String getText(Object object) {
		MarkerItemWrapper item = (MarkerItemWrapper) object;
		String name = item.getName();
		return StringUtils.capitalize(name);
	}

	@Override
	public Font getFont(Object element) {
		return JFaceResources.getFontRegistry()
			.getItalic(JFaceResources.DIALOG_FONT);
	}

}