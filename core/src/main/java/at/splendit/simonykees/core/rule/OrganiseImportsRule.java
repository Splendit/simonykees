package at.splendit.simonykees.core.rule;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.corext.codemanipulation.OrganizeImportsOperation;
import org.eclipse.jface.text.Document;
import org.eclipse.ltk.core.refactoring.DocumentChange;
import org.eclipse.osgi.util.NLS;
import org.eclipse.text.edits.TextEdit;

import at.splendit.simonykees.core.Activator;
import at.splendit.simonykees.core.i18n.Messages;
import at.splendit.simonykees.core.util.SimonykeesUtil;
import at.splendit.simonykees.core.visitor.AbstractASTRewriteASTVisitor;

@SuppressWarnings("restriction")
public class OrganiseImportsRule extends RefactoringRule<AbstractASTRewriteASTVisitor> {

	private Map<ICompilationUnit, DocumentChange> changes = new HashMap<ICompilationUnit, DocumentChange>();

	public OrganiseImportsRule(Class<AbstractASTRewriteASTVisitor> visitor) {
		super(visitor);
		this.name = "OrganiseImportsRule";
		this.description = "There shall be organising";
	}

	@Override
	public Map<ICompilationUnit, DocumentChange> getDocumentChanges() {
		return Collections.unmodifiableMap(changes);
	}

	@Override
	public void generateDocumentChanges(List<ICompilationUnit> workingCopies)
			throws JavaModelException, ReflectiveOperationException {
		for (ICompilationUnit wc : workingCopies) {
			try {
				applyOrganising(wc);
			} catch (CoreException e) {
				throw new JavaModelException(e);
			}
		}
	}

	private void applyOrganising(ICompilationUnit workingCopy) throws OperationCanceledException, CoreException {
		if (changes.containsKey(workingCopy)) {
			// already have changes
			Activator.log(NLS.bind(Messages.RefactoringRule_warning_workingcopy_already_present, this.name));
		} else {

			final ASTParser astParser = ASTParser.newParser(AST.JLS8);
			SimonykeesUtil.resetParser(workingCopy, astParser, workingCopy.getJavaProject().getOptions(true));
			final CompilationUnit astRoot = (CompilationUnit) astParser.createAST(null);

			// FIXME restricted call
			OrganizeImportsOperation importsOperation = new OrganizeImportsOperation(workingCopy, astRoot,
					true, true, true, null);

			TextEdit edit = importsOperation.createTextEdit(null);

			if (edit.hasChildren()) {
				Document document = new Document(workingCopy.getSource());
				DocumentChange documentChange = SimonykeesUtil
						.generateDocumentChange(OrganiseImportsRule.class.getSimpleName(), document, edit.copy());

				workingCopy.applyTextEdit(edit, null);
				workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);

				if (documentChange != null) {
					changes.put(workingCopy, documentChange);
				} else {
					// no changes
				}
			} else {
				// no changes
			}
		}

	}

}
