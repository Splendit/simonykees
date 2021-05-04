package eu.jsparrow.ui.quickfix;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.texteditor.IAnnotationImageProvider;

public class JSparrowImageProvider implements IAnnotationImageProvider {

	@Override
	public Image getManagedImage(Annotation annotation) {
		return JSparrowImages.JSPARROW_ACTIVE_16;
	}

	@Override
	public String getImageDescriptorId(Annotation annotation) {
		return null;
	}

	@Override
	public ImageDescriptor getImageDescriptor(String imageDescritporId) {
		return null;
	}

}
