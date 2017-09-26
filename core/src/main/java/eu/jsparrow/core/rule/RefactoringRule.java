package eu.jsparrow.core.rule;

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
import org.eclipse.osgi.util.NLS;
import org.eclipse.text.edits.TextEdit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.exception.RefactoringException;
import eu.jsparrow.core.util.RefactoringUtil;
import eu.jsparrow.core.util.TagUtil;
import eu.jsparrow.core.visitor.AbstractASTRewriteASTVisitor;
import eu.jsparrow.i18n.Messages;

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

	// default is true because of preferences page
	protected boolean enabled = true;
	protected boolean satisfiedJavaVersion = true;
	protected boolean satisfiedLibraries = true;

	private Class<T> visitor;

	protected RefactoringRule(Class<T> visitor) {
		this.visitor = visitor;
		this.id = this.getClass().getSimpleName();
		this.tags = TagUtil.getTagsForRule(this.getClass());
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
	 * Responsible to calculate if the rule is executable in the current
	 * project.
	 * 
	 * @param project
	 */
	public void calculateEnabledForProject(IJavaProject project) {
		String compilerCompliance = project.getOption(JavaCore.COMPILER_COMPLIANCE, true);
		if (null == compilerCompliance) {
			/*
			 * if we cannot get the compiler compliance, we are unable to know
			 * whether or not the Java version is satisfied
			 */
			satisfiedJavaVersion = false;
		} else {
			String enumRepresentation = convertCompilerComplianceToEnumRepresentation(compilerCompliance);
			satisfiedJavaVersion = JavaVersion.valueOf(enumRepresentation).atLeast(requiredJavaVersion);
		}
		satisfiedLibraries = ruleSpecificImplementation(project);
		enabled = satisfiedJavaVersion && satisfiedLibraries;
	}

	protected String convertCompilerComplianceToEnumRepresentation(String compilerCompliance) {
		return "JAVA_" + compilerCompliance.replace(".", "_"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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

	protected T visitorFactory() throws InstantiationException, IllegalAccessException {
		return visitor.newInstance();
	}

	/**
	 * Responsible to calculate of the rule is executable in the current
	 * project.
	 * 
	 */
	public final DocumentChange applyRule(ICompilationUnit workingCopy)
			throws ReflectiveOperationException, JavaModelException, RefactoringException {

		logger.trace(NLS.bind(Messages.RefactoringRule_applying_rule_to_workingcopy, this.name,
				workingCopy.getElementName()));

		return applyRuleImpl(workingCopy);
	}

	/**
	 * This method may be overridden.
	 * 
	 * @param workingCopy
	 * @return
	 * @throws ReflectiveOperationException
	 * @throws JavaModelException
	 * @throws RefactoringException
	 */
	protected DocumentChange applyRuleImpl(ICompilationUnit workingCopy)
			throws ReflectiveOperationException, JavaModelException, RefactoringException {
		
		final CompilationUnit astRoot = RefactoringUtil.parse(workingCopy);

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

		AbstractASTRewriteASTVisitor rule = visitorFactory();
		rule.setAstRewrite(astRewrite);
		try {
			astRoot.accept(rule);
		} catch (RuntimeException e) {
			throw new RefactoringException(e);
		}

		Document document = new Document(workingCopy.getSource());
		TextEdit edits = astRewrite.rewriteAST(document, workingCopy.getJavaProject().getOptions(true));

		if (edits.hasChildren()) {

			/*
			 * The TextEdit instance changes as soon as it is applied to the
			 * working copy. This results in an incorrect preview of the
			 * DocumentChange. To fix this issue, a copy of the TextEdit is used
			 * for the DocumentChange.
			 */
			DocumentChange documentChange = RefactoringUtil.generateDocumentChange(visitor.getSimpleName(), document,
					edits.copy());

			workingCopy.applyTextEdit(edits, null);

			// TODO think about using IProblemRequestor
			// TODO think about returning the new AST
			workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);

			return documentChange;
		} else {
			return null;
		}
	}

	/**
	 * Independent library requirements for rules that need to be defined for
	 * each rule. Returns null as default implementation
	 * 
	 * @return String value of required library fully qualified class name
	 */
	public String requiredLibraries() {
		return null;
	}

	/**
	 * Helper method for description building. Saves information if java version
	 * is satisfied for rule on selected project.
	 * 
	 * @return true if rule can be applied according to java version, false
	 *         otherwise
	 */
	public boolean isSatisfiedJavaVersion() {
		return satisfiedJavaVersion;
	}

	/**
	 * Helper method for description building. Saves information if required
	 * libraries are satisfied for rule on selected project.
	 * 
	 * @return true if rule can be applied according to required libraries,
	 *         false otherwise
	 */
	public boolean isSatisfiedLibraries() {
		return satisfiedLibraries;
	}
}
