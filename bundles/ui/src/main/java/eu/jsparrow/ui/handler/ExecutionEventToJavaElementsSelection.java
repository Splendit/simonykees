package eu.jsparrow.ui.handler;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.exception.RefactoringException;
import eu.jsparrow.ui.util.WizardHandlerUtil;
import eu.jsparrow.ui.wizard.impl.WizardMessageDialog;

public class ExecutionEventToJavaElementsSelection implements IJavaElementsSelectionProvider {

	private static final Logger logger = LoggerFactory.getLogger(ExecutionEventToJavaElementsSelection.class);

	private final ExecutionEvent event;

	public ExecutionEventToJavaElementsSelection(ExecutionEvent event) {
		this.event = event;
	}

	@Override
	public Map<IJavaProject, List<IJavaElement>> getSelectedJavaElements() {

		Map<IJavaProject, List<IJavaElement>> selectedJavaElements;
		try {
			selectedJavaElements = WizardHandlerUtil.getSelectedJavaElements(event);
		} catch (CoreException e) {
			logger.error(e.getMessage(), e);
			WizardMessageDialog.synchronizeWithUIShowError(new RefactoringException(
					Messages.SelectRulesWizardHandler_getting_selected_resources_failed + e.getMessage(),
					Messages.SelectRulesWizardHandler_user_getting_selected_resources_failed, e));
			selectedJavaElements = Collections.emptyMap();
		}

		return selectedJavaElements;
	}

}
