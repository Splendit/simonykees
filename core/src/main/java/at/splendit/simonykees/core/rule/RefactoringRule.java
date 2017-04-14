package at.splendit.simonykees.core.rule;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.JavaVersion;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.ltk.core.refactoring.DocumentChange;
import org.eclipse.osgi.util.NLS;

import at.splendit.simonykees.core.Activator;
import at.splendit.simonykees.i18n.Messages;
import at.splendit.simonykees.core.rule.impl.TryWithResourceRule;
import at.splendit.simonykees.core.util.SimonykeesUtil;
import at.splendit.simonykees.core.visitor.AbstractASTRewriteASTVisitor;

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

	protected String id;

	protected String name = Messages.RefactoringRule_default_name;

	protected String description = Messages.RefactoringRule_default_description;

	protected JavaVersion requiredJavaVersion = JavaVersion.JAVA_1_4;

	protected boolean enabled = true;

	private Class<T> visitor;

	private Map<ICompilationUnit, DocumentChange> changes = new HashMap<ICompilationUnit, DocumentChange>();

	public RefactoringRule(Class<T> visitor) {
		this.visitor = visitor;
		// TODO maybe add a better id
		this.id = this.getClass().getSimpleName();
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
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

	public JavaVersion getRequiredJavaVersion() {
		return requiredJavaVersion;
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
				Activator.log(NLS.bind(Messages.RefactoringRule_warning_workingcopy_already_present, this.name));
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
}
