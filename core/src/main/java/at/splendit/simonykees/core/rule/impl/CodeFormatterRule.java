package at.splendit.simonykees.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jface.text.Document;
import org.eclipse.ltk.core.refactoring.DocumentChange;
import org.eclipse.text.edits.TextEdit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	
	public CodeFormatterRule(Class<AbstractASTRewriteASTVisitor> visitor) {
		super(visitor);
		this.name = Messages.CodeFormatterRule_name;
		this.description = Messages.CodeFormatterRule_description;
	}
	
	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_1;
	}

	@Override
	protected DocumentChange applyRuleImpl(ICompilationUnit workingCopy)
			throws ReflectiveOperationException, JavaModelException {
		
		// TODO monitor?
//		subMonitor.setWorkRemaining(workingCopies.size());
		
//		for (ICompilationUnit wc : workingCopies) {
//			subMonitor.subTask(getName() + ": " + wc.getElementName()); //$NON-NLS-1$
//			applyFormating(wc);
//			if (subMonitor.isCanceled()) {
//				return;
//			} else {
//				subMonitor.worked(1);
//			}
//		}
		
		try {
			return applyFormating(workingCopy);
		} catch (CoreException e) {
			throw new JavaModelException(e);
		}
	}

	private DocumentChange applyFormating(ICompilationUnit workingCopy) throws JavaModelException {
//		if (changes.containsKey(workingCopy)) {
//			// already have changes
//			logger.info(NLS.bind(Messages.RefactoringRule_warning_workingcopy_already_present, this.name));
//		} else {
			ISourceRange sourceRange = workingCopy.getSourceRange();
			// TODO check formating style
			CodeFormatter formatter = ToolFactory
					.createCodeFormatter(DefaultCodeFormatterConstants.getEclipseDefaultSettings());
			int formatingKind = CodeFormatter.K_COMPILATION_UNIT | CodeFormatter.F_INCLUDE_COMMENTS
					| CodeFormatter.K_UNKNOWN;
			TextEdit edit = formatter.format(formatingKind, workingCopy.getSource(), sourceRange.getOffset(),
					sourceRange.getLength(), 0, SimonykeesUtil.LINE_SEPARATOR);

			DocumentChange documentChange = null;
			
			if (edit.hasChildren()) {
				Document document = new Document(workingCopy.getSource());
				documentChange = SimonykeesUtil
						.generateDocumentChange(CodeFormatterRule.class.getSimpleName(), document, edit.copy());

				workingCopy.applyTextEdit(edit, null);
				workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);

//				if (documentChange != null) {
//					changes.put(workingCopy, documentChange);
//				} else {
//					// no changes
//				}
//			} else {
//				// no changes
				
			}

			return documentChange;
//		}

	}

}
