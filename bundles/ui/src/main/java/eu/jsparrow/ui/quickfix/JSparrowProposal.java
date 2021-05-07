package eu.jsparrow.ui.quickfix;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension6;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import eu.jsparrow.core.markers.MarkerManager;
import eu.jsparrow.rules.common.markers.RefactoringEventManager;

public class JSparrowProposal implements IJavaCompletionProposal, ICompletionProposalExtension6 {

	private ICompilationUnit icu;
	private int offset;
	private int length;
	private String resolverName;

	public JSparrowProposal(ICompilationUnit icu, String resolverName, int offset, int length) {
		this.icu = icu;
		this.offset = offset;
		this.length = length;
		this.resolverName = resolverName;
		
	}

	@Override
	public int getRelevance() {
		return 8;
	}

	@Override
	public void apply(IDocument document) {
		RefactoringEventManager eventGenerator = new MarkerManager();
		eventGenerator.resolve(icu, resolverName, offset);

	}

	@Override
	public Point getSelection(IDocument document) {
		return new Point(offset, length);
	}

	@Override
	public String getAdditionalProposalInfo() {
		return "This anonymous class can be converted into a Lambda.";
	}

	@Override
	public String getDisplayString() {
		return "Change to Lambda";
	}

	@Override
	public Image getImage() {
		return null;
	}

	@Override
	public IContextInformation getContextInformation() {
		return null;
	}

	@Override
	public StyledString getStyledDisplayString() {
		return new StyledString("Change to Lambda");
	}

}
