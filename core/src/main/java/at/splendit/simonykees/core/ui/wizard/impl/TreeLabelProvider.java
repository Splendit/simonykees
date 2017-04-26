package at.splendit.simonykees.core.ui.wizard.impl;

import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.osgi.framework.Bundle;

import at.splendit.simonykees.core.Activator;
import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.visitor.AbstractASTRewriteASTVisitor;

public class TreeLabelProvider extends LabelProvider implements IColorProvider {

	private ResourceManager resourceManager;
	
	private static final String ICON_ENABLED_RULE = "icons/check.png"; //$NON-NLS-1$
	private static final String ICON_DISABLED_RULE = "icons/error.png"; //$NON-NLS-1$ 

	@SuppressWarnings("unchecked")
	@Override
	public String getText(Object element) {
		String s;
		if (element instanceof RefactoringRule<?>) {
			s = ((RefactoringRule<? extends AbstractASTRewriteASTVisitor>) element).getName();
		} else {
			s = (String) element;
		}
		return s;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Image getImage(Object element) {
		if (element instanceof RefactoringRule<?>) {
			Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);
            URL url;
			if (((RefactoringRule<? extends AbstractASTRewriteASTVisitor>) element).isEnabled()) {
                url = FileLocator.find(bundle, new Path(ICON_ENABLED_RULE), null);
			} else {
                url = FileLocator.find(bundle, new Path(ICON_DISABLED_RULE), null);
			}
			return getResourceManager().createImage(ImageDescriptor.createFromURL(url));
		}
		return super.getImage(element);
	}

	@Override
	public Color getForeground(Object element) {
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Color getBackground(Object element) {
		if (element instanceof RefactoringRule<?>) {
			if (!((RefactoringRule<? extends AbstractASTRewriteASTVisitor>) element).isEnabled()) {
				return Display.getDefault().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND);
			}
		}
		return null;
	}

	@Override
	public void dispose() {
		// garbage collect system resources, images have to be manually disposed
		if (resourceManager != null) {
			resourceManager.dispose();
			resourceManager = null;
		}
	}

	protected ResourceManager getResourceManager() {
		if (resourceManager == null) {
			resourceManager = new LocalResourceManager(JFaceResources.getResources());
		}
		return resourceManager;
	}

}
