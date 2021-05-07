package eu.jsparrow.ui.quickfix;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jdt.ui.text.java.IQuickFixProcessor;

public class JSparrowQuickFix implements IQuickFixProcessor {

	@Override
	public boolean hasCorrections(ICompilationUnit unit, int problemId) {
		return true;
	}

	@Override
	public IJavaCompletionProposal[] getCorrections(IInvocationContext context, IProblemLocation[] locations)
			throws CoreException {

		ICompilationUnit compilationUnit = context.getCompilationUnit();
		int selectionOffset = context.getSelectionOffset();
		int selectionLength = context.getSelectionLength();
		List<IJavaElement> elements = new ArrayList<>();
		elements.add(compilationUnit.getElementAt(selectionOffset));
		List<IJavaCompletionProposal> proposals = new ArrayList<>();
		IJavaCompletionProposal proposal = new JSparrowProposal(compilationUnit, "", selectionOffset, selectionLength);
		proposals.add(proposal);
		return proposals.toArray(new IJavaCompletionProposal[proposals.size()]);
	}

}
