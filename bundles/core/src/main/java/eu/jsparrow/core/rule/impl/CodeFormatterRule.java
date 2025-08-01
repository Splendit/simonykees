package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jface.text.Document;
import org.eclipse.ltk.core.refactoring.DocumentChange;
import org.eclipse.text.edits.TextEdit;

import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;
import eu.jsparrow.rules.common.statistics.FileChangeCount;
import eu.jsparrow.rules.common.statistics.RuleApplicationCount;
import eu.jsparrow.rules.common.util.RefactoringUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * Format a Java class, the rule does not use an
 * {@link AbstractASTRewriteASTVisitor} so the abstract class itself can be
 * passed to the constructor.
 * <p>
 * The formatter selected in the Eclipse settings of the processed project is
 * used.
 * 
 * @author Hannes Schweighofer, Ludwig Werzowa, Matthias Webhofer
 * @since 0.9.2
 *
 */
public class CodeFormatterRule extends RefactoringRuleImpl<AbstractASTRewriteASTVisitor> {

	public CodeFormatterRule() {
		this.visitorClass = AbstractASTRewriteASTVisitor.class;
		this.id = "CodeFormatter"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.CodeFormatterRule_name,
				Messages.CodeFormatterRule_description, Duration.ofMinutes(1),
				Arrays.asList(Tag.JAVA_1_1, Tag.FORMATTING, Tag.READABILITY));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_1;
	}

	@Override
	protected DocumentChange applyRuleImpl(ICompilationUnit workingCopy, CompilationUnit astRoot)
			throws ReflectiveOperationException, JavaModelException {

		try {
			return applyFormating(workingCopy);
		} catch (CoreException e) {
			throw new JavaModelException(e);
		}
	}

	private DocumentChange applyFormating(ICompilationUnit workingCopy) throws JavaModelException {
		ISourceRange sourceRange = workingCopy.getSourceRange();

		/*
		 * Our sample module makes it necessary to use the options of the
		 * currently used IJavaProject instead of JavaCore.getOptions() (used
		 * when passing null), which works in runtime Eclipse etc.
		 */
		CodeFormatter formatter = ToolFactory.createCodeFormatter(workingCopy.getJavaProject()
			.getOptions(true));

		int formatingKind = CodeFormatter.K_COMPILATION_UNIT | CodeFormatter.F_INCLUDE_COMMENTS
				| CodeFormatter.K_UNKNOWN;
		TextEdit edit = formatter.format(formatingKind, workingCopy.getSource(), sourceRange.getOffset(),
				sourceRange.getLength(), 0, RefactoringUtil.LINE_SEPARATOR);

		DocumentChange documentChange = null;

		if (edit.hasChildren()) {

			FileChangeCount count = RuleApplicationCount.getFor(this)
				.getApplicationsForFile(workingCopy.getHandleIdentifier());
			count.clear();
			count.update();
			Document document = new Document(workingCopy.getSource());
			documentChange = RefactoringUtil.generateDocumentChange(CodeFormatterRule.class.getSimpleName(), document,
					edit.copy());

			workingCopy.applyTextEdit(edit, null);

		}

		return documentChange;

	}

}
