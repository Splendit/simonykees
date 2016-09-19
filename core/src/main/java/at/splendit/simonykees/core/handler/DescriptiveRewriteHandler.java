package at.splendit.simonykees.core.handler;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import at.splendit.simonykees.core.Activator;
import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.rule.RulesContainer;
import at.splendit.simonykees.core.ui.ChangePreviewWizard;
import at.splendit.simonykees.core.ui.DisposableDocumentChange;

public class DescriptiveRewriteHandler extends AbstractSimonykeesHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		List<IJavaElement> selectedJavaElements = getSelectedJavaElements(event);
		List<ICompilationUnit> compilationUnits = new ArrayList<>();
		
		try {
			getCompilationUnits(compilationUnits, selectedJavaElements);
			
			if (compilationUnits.isEmpty()) {
				Activator.log(Status.WARNING, "No compilation units found", null); //$NON-NLS-1$
				return null;
			}
			
			for (ICompilationUnit compilationUnit : compilationUnits) {
				ICompilationUnit workingCopy;
				for (RefactoringRule<? extends ASTVisitor> rule : RulesContainer.getAllRules()) {
					try {
						workingCopy = compilationUnit.getWorkingCopy(null);
						final ASTParser astParser = ASTParser.newParser(AST.JLS8);
						resetParser(workingCopy, astParser);
						final CompilationUnit astRoot = (CompilationUnit) astParser.createAST(null);
						final ASTRewrite astRewrite = ASTRewrite.create(astRoot.getAST());
						
						Activator.log("Init rule [" + rule.getName() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
						ASTVisitor visitor = rule.getVisitor().getConstructor(ASTRewrite.class).newInstance(astRewrite);
						astRoot.accept(visitor);
						
						String source = workingCopy.getSource();
						Document document = new Document(source);
						TextEdit edits = astRewrite.rewriteAST(document, workingCopy.getJavaProject().getOptions(true));

						DisposableDocumentChange documentChange = new DisposableDocumentChange("current", document); //$NON-NLS-1$
						documentChange.setEdit(edits);

						IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);

						// Create the wizard
						Wizard wizard = new ChangePreviewWizard(documentChange);
						// wizard.init(window.getWorkbench(), null);

						WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
						Rectangle rectangle = Display.getCurrent().getPrimaryMonitor().getBounds();
						dialog.setPageSize(rectangle.width, rectangle.height);

						// Open the wizard dialog
						dialog.open();

						if (!documentChange.isDisposed()) {
							workingCopy.applyTextEdit(edits, null);
							workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
							workingCopy.commitWorkingCopy(false, null);
						}
						workingCopy.discardWorkingCopy();
						
					} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
							| InvocationTargetException | NoSuchMethodException | SecurityException e) {
						Activator.log(Status.ERROR, "Cannot init rule [" + rule.getName() + "]", e); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
				
			}
			
			
		} catch (JavaModelException e) {
			Activator.log(Status.ERROR, e.getMessage(), null);
			throw new ExecutionException(e.getMessage(), e);
		}
		
		return null;
	}

}
