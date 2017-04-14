package at.splendit.simonykees.core.rule.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.JavaVersion;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jface.text.Document;
import org.eclipse.ltk.core.refactoring.DocumentChange;
import org.eclipse.osgi.util.NLS;
import org.eclipse.text.edits.TextEdit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.splendit.simonykees.core.Activator;
import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.util.SimonykeesUtil;
import at.splendit.simonykees.core.visitor.AbstractASTRewriteASTVisitor;
import at.splendit.simonykees.i18n.Messages;

/**
 * Format a Java class, the rule does not use an
 * {@link AbstractASTRewriteASTVisitor} so the abstract class itself can be
 * passed to the constructor.
 * 
 * @author Hannes Schweighofer, Ludwig Werzowa
 * @since 0.9.2
 *
 */
public class CodeFormatterRule extends RefactoringRule<AbstractASTRewriteASTVisitor> {

	private static final Logger logger = LoggerFactory.getLogger(CodeFormatterRule.class);
	
	private Map<ICompilationUnit, DocumentChange> changes = new HashMap<ICompilationUnit, DocumentChange>();

	public CodeFormatterRule(Class<AbstractASTRewriteASTVisitor> visitor) {
		super(visitor);
		this.name = Messages.CodeFormatterRule_name;
		this.description = Messages.CodeFormatterRule_description;
		this.requiredJavaVersion = JavaVersion.JAVA_0_9;
	}

	@Override
	public Map<ICompilationUnit, DocumentChange> getDocumentChanges() {
		return Collections.unmodifiableMap(changes);
	}

	@Override
	public void generateDocumentChanges(List<ICompilationUnit> workingCopies, SubMonitor subMonitor)
			throws JavaModelException, ReflectiveOperationException {
		
		subMonitor.setWorkRemaining(workingCopies.size());
		
		for (ICompilationUnit wc : workingCopies) {
			subMonitor.subTask(getName() + ": " + wc.getElementName()); //$NON-NLS-1$
			applyFormating(wc);
			if (subMonitor.isCanceled()) {
				return;
			} else {
				subMonitor.worked(1);
			}
		}
	}

	private void applyFormating(ICompilationUnit workingCopy) throws JavaModelException {
		if (changes.containsKey(workingCopy)) {
			// already have changes
			//Activator.log(NLS.bind(Messages.RefactoringRule_warning_workingcopy_already_present, this.name));
			logger.info(NLS.bind(Messages.RefactoringRule_warning_workingcopy_already_present, this.name));
		} else {
			ISourceRange sourceRange = workingCopy.getSourceRange();
			// TODO check formating style
			CodeFormatter formatter = ToolFactory
					.createCodeFormatter(DefaultCodeFormatterConstants.getEclipseDefaultSettings());
			int formatingKind = CodeFormatter.K_COMPILATION_UNIT | CodeFormatter.F_INCLUDE_COMMENTS
					| CodeFormatter.K_UNKNOWN;
			TextEdit edit = formatter.format(formatingKind, workingCopy.getSource(), sourceRange.getOffset(),
					sourceRange.getLength(), 0, SimonykeesUtil.LINE_SEPARATOR);

			if (edit.hasChildren()) {
				Document document = new Document(workingCopy.getSource());
				DocumentChange documentChange = SimonykeesUtil
						.generateDocumentChange(CodeFormatterRule.class.getSimpleName(), document, edit.copy());

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
