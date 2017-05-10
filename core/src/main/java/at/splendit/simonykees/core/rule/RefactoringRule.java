package at.splendit.simonykees.core.rule;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.JavaVersion;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.ltk.core.refactoring.DocumentChange;
import org.eclipse.osgi.util.NLS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.splendit.simonykees.core.rule.impl.TryWithResourceRule;
import at.splendit.simonykees.core.util.SimonykeesUtil;
import at.splendit.simonykees.core.util.TagUtil;
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
	protected boolean satisfiedJavaVersion = true;
	protected boolean satisfiedLibraries = true;

	private Class<T> visitor;

	private Map<ICompilationUnit, DocumentChange> changes = new HashMap<ICompilationUnit, DocumentChange>();

	public RefactoringRule(Class<T> visitor) {
		this.visitor = visitor;
		// TODO maybe add a better id
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
	 * Changes should be generated with {@code generateDocumentChanges} first
	 * 
	 * @return Map containing {@code ICompilationUnit}s as key and corresponding
	 *         {@code DocumentChange}s as value
	 */
	public Map<ICompilationUnit, DocumentChange> getDocumentChanges() {
		return Collections.unmodifiableMap(changes);
	}

	/**
	 * Changes are applied to working copy but <b>not</b> committed
	 * 
	 * @param workingCopies
	 *            List of {@link ICompilationUnit} for which a
	 *            {@link DocumentChange} for each selected rule is created
	 * @throws JavaModelException
	 *             if this element does not exist or if an exception occurs
	 *             while accessing its corresponding resource.
	 * @throws ReflectiveOperationException
	 *             is thrown if the default constructor of {@link #visitor} is
	 *             not present and the reflective construction fails.
	 */
	public void generateDocumentChanges(List<ICompilationUnit> workingCopies, SubMonitor subMonitor)
			throws JavaModelException, ReflectiveOperationException {

		subMonitor.setWorkRemaining(workingCopies.size());

		for (ICompilationUnit wc : workingCopies) {
			subMonitor.subTask(getName() + ": " + wc.getElementName()); //$NON-NLS-1$
			applyRule(wc);
			if (subMonitor.isCanceled()) {
				return;
			} else {
				subMonitor.worked(1);
			}
		}
	}

	private void applyRule(ICompilationUnit workingCopy) throws JavaModelException, ReflectiveOperationException {

		// FIXME SIM-206: TryWithResource multiple new resource on empty list
		boolean dirtyHack = this instanceof TryWithResourceRule;

		boolean changesAlreadyPresent = changes.containsKey(workingCopy);

		if (changesAlreadyPresent) {
			if (dirtyHack) {
				// we have to collect changes a second time (see SIM-206)
				collectChanges(workingCopy);
			} else {
				// already have changes
				logger.info(NLS.bind(Messages.RefactoringRule_warning_workingcopy_already_present, this.name));
			}
		} else {
			collectChanges(workingCopy);
		}

	}

	/**
	 * Apply the current rule and collect all resulting changes.
	 * 
	 * @param workingCopies
	 *            List of {@link ICompilationUnit} for which a
	 *            {@link DocumentChange} for each selected rule is created
	 * @throws JavaModelException
	 *             if this element does not exist or if an exception occurs
	 *             while accessing its corresponding resource.
	 * @throws ReflectiveOperationException
	 *             is thrown if the default constructor of {@link #visitor} is
	 *             not present and the reflective construction fails.
	 */
	private void collectChanges(ICompilationUnit workingCopy) throws JavaModelException, ReflectiveOperationException {
		DocumentChange documentChange = SimonykeesUtil.applyRule(workingCopy, visitor);
		if (documentChange != null) {

			/*
			 * FIXME SIM-206: TryWithResource multiple new resource on empty
			 * list
			 */
			/*
			 * FIXME SIM-206: this particular part of the fix does not work.
			 * This will create the correct results. However, the
			 * RefactoringPreviewWizard will show the diff between the first and
			 * the second run, rather than the diff between the original source
			 * and the second run. See comment in SIM-206.
			 */
			// if (dirtyHack) {
			// DocumentChange temp = changes.get(workingCopy);
			// if (temp != null) {
			// documentChange.addEdit(temp.getEdit());
			// }
			// }

			changes.put(workingCopy, documentChange);
		} else {
			// no changes
		}
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
			satisfiedJavaVersion = JavaVersion.valueOf(enumRepresentation).atLeast(requiredJavaVersion);
			
			satisfiedLibraries = ruleSpecificImplementation(project);
			
			enabled = satisfiedJavaVersion && satisfiedLibraries;
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
