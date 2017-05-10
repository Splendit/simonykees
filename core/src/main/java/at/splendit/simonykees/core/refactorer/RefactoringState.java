/**
 * 
 */
package at.splendit.simonykees.core.refactorer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.ltk.core.refactoring.DocumentChange;
import org.eclipse.osgi.util.NLS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.rule.impl.TryWithResourceRule;
import at.splendit.simonykees.core.util.SimonykeesUtil;
import at.splendit.simonykees.core.visitor.AbstractASTRewriteASTVisitor;
import at.splendit.simonykees.i18n.ExceptionMessages;
import at.splendit.simonykees.i18n.Messages;

/**
 * One {@link RefactoringState} per {@link CompilationUnit}.
 * 
 * @author Ludwig Werzowa
 * @since 1.2
 */
public class RefactoringState {

	private static final Logger logger = LoggerFactory.getLogger(RefactoringState.class);

	private ICompilationUnit workingCopy;

	private Map<RefactoringRule<? extends AbstractASTRewriteASTVisitor>, DocumentChange> changes = new HashMap<RefactoringRule<? extends AbstractASTRewriteASTVisitor>, DocumentChange>();

	private String name;

	public RefactoringState(ICompilationUnit workingCopy) {
		super();
		this.workingCopy = workingCopy;
		this.name = workingCopy.getElementName();
	}

	public String getName() {
		return name;
	}

	public DocumentChange getChangeIfPresent(RefactoringRule<? extends AbstractASTRewriteASTVisitor> rule) {
		// TODO check if the rule has been processed already

		return changes.get(rule);
	}

	public ICompilationUnit getWorkingCopy() {
		return workingCopy;
	}

	public void setWorkingCopy(ICompilationUnit workingCopy) {
		this.workingCopy = workingCopy;
	}

	public void addRules(List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> rules)
			throws JavaModelException, ReflectiveOperationException {
		for (RefactoringRule<? extends AbstractASTRewriteASTVisitor> refactoringRule : rules) {
			addRule(refactoringRule);
		}
	}

	// TODO add monitor
	public void addRule(RefactoringRule<? extends AbstractASTRewriteASTVisitor> rule)
			throws JavaModelException, ReflectiveOperationException {
		generateDocumentChanges(rule);
	}

	/**
	 * Changes are applied to working copy but <b>not</b> committed. 
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
	public void generateDocumentChanges(RefactoringRule<? extends AbstractASTRewriteASTVisitor> rule)
			throws JavaModelException, ReflectiveOperationException {

		applyRule(rule);
	}

	public void clearWorkingCopies() {
		try {
			SimonykeesUtil.discardWorkingCopy(workingCopy);
		} catch (JavaModelException e) {
			logger.error(NLS.bind(ExceptionMessages.AbstractRefactorer_unable_to_discard_working_copy,
					workingCopy.getPath().toString(), e.getMessage()), e);
		}
	}

	private void applyRule(RefactoringRule<? extends AbstractASTRewriteASTVisitor> rule)
			throws JavaModelException, ReflectiveOperationException {

		// FIXME SIM-206: TryWithResource multiple new resource on empty list
		boolean dirtyHack = rule instanceof TryWithResourceRule;

		// TODO test this
		boolean changesAlreadyPresent = changes.containsKey(rule);

		if (changesAlreadyPresent) {
			if (dirtyHack) {
				// we have to collect changes a second time (see SIM-206)
				collectChanges(rule);
			} else {
				// already have changes
				logger.info(NLS.bind(Messages.RefactoringRule_warning_workingcopy_already_present, this.name));
			}
		} else {
			collectChanges(rule);
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
	private void collectChanges(RefactoringRule<? extends AbstractASTRewriteASTVisitor> rule)
			throws JavaModelException, ReflectiveOperationException {
		DocumentChange documentChange = SimonykeesUtil.applyRule(workingCopy, rule.getVisitor());
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

			changes.put(rule, documentChange);
		} else {
			// no changes
		}
	}
}
