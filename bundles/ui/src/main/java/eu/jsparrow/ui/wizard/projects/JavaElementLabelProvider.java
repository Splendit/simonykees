package eu.jsparrow.ui.wizard.projects;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Font;

import eu.jsparrow.ui.wizard.projects.javaelement.CompilationUnitWrapper;
import eu.jsparrow.ui.wizard.projects.javaelement.JavaProjectWrapper;
import eu.jsparrow.ui.wizard.projects.javaelement.PackageFragmentRootWrapper;
import eu.jsparrow.ui.wizard.projects.javaelement.PackageFragmentWrapper;

/**
 * A label provider for the markers preference page tree.
 * 
 * @since 4.10.0
 *
 */
public class JavaElementLabelProvider extends LabelProvider implements IFontProvider {

	@Override
	public String getText(Object object) {
		if (object instanceof JavaProjectWrapper) {
			return ((JavaProjectWrapper) object).getPathToDisplay();
		}
		if (object instanceof PackageFragmentRootWrapper) {
			return ((PackageFragmentRootWrapper) object).getPathToDisplay();
		}
		if (object instanceof PackageFragmentWrapper) {
			return ((PackageFragmentWrapper) object).getElementName();
		}
		if (object instanceof CompilationUnitWrapper) {
			return ((CompilationUnitWrapper) object).getElementName();
		}
		return object.toString();
	}

	@Override
	public Font getFont(Object element) {
		return JFaceResources.getFontRegistry()
			.getItalic(JFaceResources.DIALOG_FONT);
	}

}