package eu.jsparrow.rules.common;

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

import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.exception.RefactoringException;
import eu.jsparrow.rules.common.statistics.RuleApplicationCount;
import eu.jsparrow.rules.common.util.PropertyUtil;
import eu.jsparrow.rules.common.util.RefactoringUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * Wrapper Class for {@link AbstractASTRewriteASTVisitor} that holds UI name,
 * description, if its enabled and the document changes for
 * {@link ICompilationUnit} that are processed
 * 
 * @author Martin Huter, Hannes Schweighofer, Ludwig Werzowa, Hans-Jörg Schrödl,
 *         Matthias Webhofer
 * @since 0.9
 *
 * @param <T>
 *            is the {@link AbstractASTRewriteASTVisitor} implementation that is
 *            applied by this rule
 */
public abstract class RefactoringRule<T extends AbstractASTRewriteASTVisitor> implements RefactoringRuleInterface {

	private static final Logger logger = LoggerFactory.getLogger(RefactoringRule.class);

	protected String id;

	protected RuleDescription ruleDescription;

	protected final JavaVersion requiredJavaVersion;

	// default is true because of preferences page
	protected boolean enabled = true;
	protected boolean satisfiedJavaVersion = true;
	protected boolean satisfiedLibraries = true;

	protected Class<T> visitorClass;

	protected RefactoringRule() {
		this.requiredJavaVersion = provideRequiredJavaVersion();
	}

	/**
	 * the required java version of the implemented rule
	 * 
	 * @return
	 */
	protected abstract JavaVersion provideRequiredJavaVersion();

	public JavaVersion getRequiredJavaVersion() {
		return requiredJavaVersion;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public Class<T> getVisitor() {
		return visitorClass;
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
			// SIM-844 HOTFIX to accept java 9
			JavaVersion usedJavaVersion = PropertyUtil.stringToJavaVersion(compilerCompliance);
			satisfiedJavaVersion = usedJavaVersion.atLeast(requiredJavaVersion);
		}
		satisfiedLibraries = ruleSpecificImplementation(project);
		enabled = satisfiedJavaVersion && satisfiedLibraries;
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

	protected AbstractASTRewriteASTVisitor visitorFactory() throws InstantiationException, IllegalAccessException {
		AbstractASTRewriteASTVisitor visitor = visitorClass.newInstance();
		visitor.addRewriteListener(RuleApplicationCount.getFor(this));
		return visitor;
	}

	/**
	 * Responsible to calculate of the rule is executable in the current
	 * project.
	 * 
	 */
	public final DocumentChange applyRule(ICompilationUnit workingCopy, CompilationUnit astRoot)
			throws ReflectiveOperationException, JavaModelException, RefactoringException {

		String bind = NLS.bind(Messages.RefactoringRule_applying_rule_to_workingcopy, this.getRuleDescription()
			.getName(), workingCopy.getElementName());
		logger.trace(bind);

		return applyRuleImpl(workingCopy,astRoot);
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
	protected DocumentChange applyRuleImpl(ICompilationUnit workingCopy, CompilationUnit astRoot)
			throws ReflectiveOperationException, JavaModelException, RefactoringException {

		final ASTRewrite astRewrite = ASTRewrite.create(astRoot.getAST());

		AbstractASTRewriteASTVisitor rule = visitorFactory();
		rule.setASTRewrite(astRewrite);
		rule.setCompilationUnit(workingCopy.getHandleIdentifier());
		try {
			astRoot.accept(rule);
		} catch (RuntimeException e) {
			throw new RefactoringException(e);
		}

		Document document = new Document(workingCopy.getSource());
		TextEdit edits = astRewrite.rewriteAST(document, workingCopy.getJavaProject()
			.getOptions(true));

		if (edits.hasChildren()) {

			/*
			 * The TextEdit instance changes as soon as it is applied to the
			 * working copy. This results in an incorrect preview of the
			 * DocumentChange. To fix this issue, a copy of the TextEdit is used
			 * for the DocumentChange.
			 */
			DocumentChange documentChange = RefactoringUtil.generateDocumentChange(visitorClass.getSimpleName(),
					document, edits.copy());

			workingCopy.applyTextEdit(edits, null);

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

	@Override
	public RuleDescription getRuleDescription() {
		return this.ruleDescription;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RefactoringRule<?> other = (RefactoringRule<?>) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Rule [id=" + id + ", name=" + this.getRuleDescription() //$NON-NLS-1$ //$NON-NLS-2$
			.getName() + "]"; //$NON-NLS-1$
	}

}
