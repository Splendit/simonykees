package at.splendit.simonykees.core.rule;

import java.util.List;

import org.apache.commons.lang3.JavaVersion;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.Document;
import org.eclipse.ltk.core.refactoring.DocumentChange;
import org.eclipse.text.edits.TextEdit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.splendit.simonykees.core.util.SimonykeesUtil;
import at.splendit.simonykees.core.visitor.AbstractASTRewriteASTVisitor;
import at.splendit.simonykees.i18n.Messages;

/**
 * Wrapper Class for {@link AbstractASTRewriteASTVisitor} that holds UI name,
 * description, if its enabled and the document changes for
 * {@link ICompilationUnit} that are processed
 * 
 * @author Martin Huter, Hannes Schweighofer, Ludwig Werzowa
 * @since 0.9
 *
 * @param <T>
 *            is the {@link AbstractASTRewriteASTVisitor} implementation that is
 *            applied by this rule
 */
public abstract class RefactoringRule<T extends AbstractASTRewriteASTVisitor> {

	private static final Logger logger = LoggerFactory.getLogger(RefactoringRule.class);

	protected String id;

	protected String name = Messages.RefactoringRule_default_name;

	protected String description = Messages.RefactoringRule_default_description;

	protected final JavaVersion requiredJavaVersion;

	protected final List<Tag> tags;

	protected boolean enabled = true;

	private Class<T> visitor;

	public RefactoringRule(Class<T> visitor) {
		this.visitor = visitor;
		// TODO maybe add a better id
		this.id = this.getClass().getSimpleName();
		this.tags = Tag.getTagsForRule(this.getClass());
		this.requiredJavaVersion = provideRequiredJavaVersion();
	}

	/**
	 * the required java version of the implemented rule
	 * 
	 * @return
	 */
	protected abstract JavaVersion provideRequiredJavaVersion();

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public JavaVersion getRequiredJavaVersion() {
		return requiredJavaVersion;
	}

	public List<Tag> getTags() {
		return tags;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public Class<T> getVisitor() {
		return visitor;
	}

	public String getId() {
		return id;
	}

	/**
	 * Responsible to calculate of the rule is executable in the current
	 * project.
	 * 
	 * @param project
	 */
	public void calculateEnabledForProject(IJavaProject project) {
		String compilerCompliance = project.getOption(JavaCore.COMPILER_COMPLIANCE, true);
		if (null != compilerCompliance) {
			String enumRepresentation = "JAVA_" + compilerCompliance.replace(".", "_"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			enabled = JavaVersion.valueOf(enumRepresentation).atLeast(requiredJavaVersion);
			if (enabled) {
				enabled = ruleSpecificImplementation(project);
			}
		}
	}

	/**
	 * JavaVersion independent requirements for rules that need to be defined
	 * for each rule. Returns true as default implementation
	 * 
	 * @param project
	 * @return
	 */
	public boolean ruleSpecificImplementation(IJavaProject project) {
		return true;
	}

	/**
	 * TODO description
	 * 
	 * Apply a single rule to a {@code ICompilationUnit}, changes are not
	 * committed to {@code workingCopy}
	 * 
	 * @param workingCopy
	 *            working copy of the java document that was selected for a rule
	 *            application
	 * @param ruleClazz
	 *            class object of the Rule.class that is applied to the
	 *            {@link ICompilationUnit} workingCopy
	 * @return a {@code DocumentChange} containing the old and new source or
	 *         null if no changes were detected
	 * @throws ReflectiveOperationException
	 *             is thrown if the {@code ruleClazz} has no default constructor
	 *             that could be invoked with newInstance
	 * @throws JavaModelException
	 *             if an exception occurs while accessing its corresponding
	 *             resource
	 * 
	 *             if this edit can not be applied to the compilation unit's
	 *             buffer. Reasons include: This compilation unit does not exist
	 *             (IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST). The
	 *             provided edit can not be applied as there is a problem with
	 *             the text edit locations
	 *             (IJavaModelStatusConstants.BAD_TEXT_EDIT_LOCATION).
	 * 
	 *             if the contents of the original element cannot be accessed.
	 *             Reasons include: The original Java element does not exist
	 *             (ELEMENT_DOES_NOT_EXIST)
	 * @since 0.9
	 * 
	 */
	public DocumentChange applyRule(ICompilationUnit workingCopy)
			throws ReflectiveOperationException, JavaModelException {
		final CompilationUnit astRoot = SimonykeesUtil.parse(workingCopy);
		final ASTRewrite astRewrite = ASTRewrite.create(astRoot.getAST());
		// FIXME resolves that comments are manipulated during astrewrite
		//
		// Solution from https://bugs.eclipse.org/bugs/show_bug.cgi?id=250142
		// The best solution for such problems is usually to call
		// ASTRewrite#setTargetSourceRangeComputer(TargetSourceRangeComputer)
		// and set a NoCommentSourceRangeComputer or a properly configured
		// TightSourceRangeComputer.

		// astRewrite.setTargetSourceRangeComputer(new
		// NoCommentSourceRangeComputer());

		AbstractASTRewriteASTVisitor rule = visitor.newInstance();
		rule.setAstRewrite(astRewrite);
		astRoot.accept(rule);

		Document document = new Document(workingCopy.getSource());
		TextEdit edits = astRewrite.rewriteAST(document, workingCopy.getJavaProject().getOptions(true));

		if (edits.hasChildren()) {

			/*
			 * The TextEdit instance changes as soon as it is applied to the
			 * working copy. This results in an incorrect preview of the
			 * DocumentChange. To fix this issue, a copy of the TextEdit is used
			 * for the DocumentChange.
			 */
			DocumentChange documentChange = SimonykeesUtil.generateDocumentChange(visitor.getSimpleName(), document,
					edits.copy());

			workingCopy.applyTextEdit(edits, null);
			workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);

			return documentChange;
		} else {
			return null;
		}

	}
}
