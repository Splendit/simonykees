package at.splendit.simonykees.core.handler;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ltk.core.refactoring.DocumentChange;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import at.splendit.simonykees.core.Activator;
import at.splendit.simonykees.core.dialogs.ChangePreviewWizard;
import at.splendit.simonykees.core.dialogs.DisposableDocumentChange;
import at.splendit.simonykees.core.visitor.arithmetic.ArithmethicAssignmentASTVisitor;

public class DescriptiveRewriteHandler extends AbstractSimonykeesHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final Shell shell = HandlerUtil.getActiveShell(event);
		final String activePartId = HandlerUtil.getActivePartId(event);
		final ASTParser astParser = ASTParser.newParser(AST.JLS8);

		Activator.log("activePartId [" + activePartId + "]");

		switch (activePartId) {
		case "org.eclipse.jdt.ui.CompilationUnitEditor":
			ICompilationUnit originalUnit = getFromEditor(shell, HandlerUtil.getActiveEditor(event));
			ICompilationUnit workingCopy;
			try {
				workingCopy = originalUnit.getWorkingCopy(null);
			} catch (JavaModelException e) {
				// TODO Auto-generated catch block
				throw new ExecutionException("Unable to create workingCopy", e);
			}

			resetParser(workingCopy, astParser);
			CompilationUnit astRoot = (CompilationUnit) astParser.createAST(null);

			/*
			 * 1/2 see
			 * http://help.eclipse.org/mars/index.jsp?topic=%2Forg.eclipse.jdt.
			 * doc.isv%2Fguide%2Fjdt_api_manip.htm
			 * "The modifying API allows to modify directly the AST"
			 */
			// astRoot.recordModifications();

			ASTRewrite astRewrite = ASTRewrite.create(astRoot.getAST());

			// we let the visitor do his job
			astRoot.accept(new ArithmethicAssignmentASTVisitor(astRewrite));

			/*
			 * 2/2
			 */
			try {
				String source = workingCopy.getSource();
				Document document = new Document(source);
				// TextEdit edits = astRoot.rewrite(document,
				// workingCopy.getJavaProject().getOptions(true));
				TextEdit edits = astRewrite.rewriteAST(document, workingCopy.getJavaProject().getOptions(true));

				DisposableDocumentChange documentChange = new DisposableDocumentChange("current", document);
				documentChange.setEdit(edits);

				IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);

				// Create the wizard
				Wizard wizard = new ChangePreviewWizard(documentChange);
				// wizard.init(window.getWorkbench(), null);

				WizardDialog dialog = new WizardDialog(window.getShell(), wizard);

				// Open the wizard dialog
				dialog.open();

				if (!documentChange.isDisposed()) {
					// Modify buffer and reconcile
					workingCopy.applyTextEdit(edits, null);

					/*
					 * Note about bindings:
					 * 
					 * "If requested, a DOM AST representing the compilation unit is returned. 
					 * Its bindings are computed only if the problem requestor is active."
					 * Source: http://help.eclipse.org/mars/index.jsp?topic=%2Forg.eclipse.jdt.doc.isv%2Freference%2Fapi%2Forg%2Feclipse%2Fjdt%2Fcore%2FICompilationUnit.html&anchor=reconcile(int,%20boolean,%20org.eclipse.jdt.core.WorkingCopyOwner,%20org.eclipse.core.runtime.IProgressMonitor)
					 * 
					 * The IProblemRequestor can be passed via the becomeWorkingCopy method. 
					 */
					workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);

					// Commit changes
					workingCopy.commitWorkingCopy(false, null);
				}

				// Destroy working copy
				workingCopy.discardWorkingCopy();

			} catch (JavaModelException | MalformedTreeException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			// Activator.log("new ast\n" + astRoot.toString());

			break;
		case "org.eclipse.jdt.ui.PackageExplorer":
		case "org.eclipse.ui.navigator.ProjectExplorer":
			HandlerUtil.getCurrentStructuredSelection(event);
			Activator.log(Status.ERROR, "activePartId [" + activePartId + "] must be coded next", null);
			break;

		default:
			Activator.log(Status.ERROR, "activePartId [" + activePartId + "] unknown", null);
			break;
		}

		// new RefactoringJob().schedule();

		return null;
	}

}
