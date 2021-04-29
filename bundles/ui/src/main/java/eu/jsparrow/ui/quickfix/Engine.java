package eu.jsparrow.ui.quickfix;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ide.ResourceUtil;

public class Engine extends EditorTracker implements IElementChangedListener, IPreferenceChangeListener {
	
	private IResource currentResource;
	private static final Collection<String> JAVA_EXTENSIONS = new HashSet<>(Arrays.asList(JavaCore
			.getJavaLikeExtensions())); // We only need Java. 

	@Override
	public void preferenceChange(PreferenceChangeEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void elementChanged(ElementChangedEvent event) {
		IJavaElementDelta delta = event.getDelta();
		handleDelta(delta);
	}

	private void handleDelta(IJavaElementDelta delta) {
		if (IJavaElementDelta.REMOVED == delta.getKind()) {
			return;
		}

		IJavaElement element = delta.getElement();

		if (isCurrent(element)) {
			checkElement(element);
		}
		
	}

	private boolean isCurrent(IJavaElement element) {
		IResource resource = element.getResource();
		if (resource == null) {
			return false;
		}
		return resource.equals(currentResource);
	}
	
	@Override
	public void editorActivated(IEditorPart part) {
		currentResource = getResource(part);
		checkResource(currentResource);
		
	}

	@Override
	public void editorDeactivated(IEditorPart part) {
		// TODO Auto-generated method stub
		
	}
	
	private void checkResource(IResource resource) {
		if (shouldProcess(resource)) {
			checkElement(JavaCore.create(resource));
		}
	}
	
	private IResource getResource(IEditorPart editor) {
		if (editor == null) {
			return null;
		}
		IEditorInput editorInput = editor.getEditorInput();
		return ResourceUtil.getResource(editorInput);
	}
	
	private boolean shouldProcess(IResource resource) {
		return resource != null && resource.exists() && IResource.FILE == resource.getType()
				&& isJavaResource(resource);
	}
	
	private boolean isJavaResource(IResource resource) {
		return JAVA_EXTENSIONS.contains(resource.getFileExtension());
	}
	
	public void checkElement(IJavaElement element) {
		if (element == null) {
			return;
		}

		ICompilationUnit cu = (ICompilationUnit) element.getAncestor(IJavaElement.COMPILATION_UNIT);

		if (cu == null) {
			return;
		}

		if (cu.getOwner() != null) {
			cu = cu.getPrimary();
		}

		handleParentSourceReference(cu);
	}

	private void handleParentSourceReference(ICompilationUnit cu) {
		
		
	}

}
