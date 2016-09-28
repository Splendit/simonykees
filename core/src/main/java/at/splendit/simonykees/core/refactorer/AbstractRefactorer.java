package at.splendit.simonykees.core.refactorer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.osgi.util.NLS;

import at.splendit.simonykees.core.Activator;
import at.splendit.simonykees.core.exception.MalformedInputException;
import at.splendit.simonykees.core.exception.ReconcileException;
import at.splendit.simonykees.core.exception.RefactoringException;
import at.splendit.simonykees.core.exception.RuleException;
import at.splendit.simonykees.core.i18n.Messages;
import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.util.SimonykeesUtil;

public abstract class AbstractRefactorer {

	protected List<IJavaElement> javaElements;
	protected List<RefactoringRule<? extends ASTVisitor>> rules;
	protected List<ICompilationUnit> workingCopies = new ArrayList<>();

	public AbstractRefactorer(List<IJavaElement> javaElements, List<RefactoringRule<? extends ASTVisitor>> rules) {
		this.javaElements = javaElements;
		this.rules = rules;
	}

	public void prepareRefactoring() throws RefactoringException {
		List<ICompilationUnit> compilationUnits = new ArrayList<>();
		try {
			SimonykeesUtil.collectICompilationUnits(compilationUnits, javaElements);
			if (compilationUnits.isEmpty()) {
				Activator.log(Status.WARNING, Messages.AbstractRefactorer_warn_no_compilation_units_found, null);
				throw new RefactoringException(Messages.AbstractRefactorer_warn_no_compilation_units_found,
						"Selection did not contain any Java files.");
			} else if (!workingCopies.isEmpty()) {
				Activator.log(Status.WARNING, Messages.AbstractRefactorer_warn_working_copies_already_generated, null);
				throw new RefactoringException(Messages.AbstractRefactorer_warn_working_copies_already_generated);
			} else {
				for (ICompilationUnit compilationUnit : compilationUnits) {
					workingCopies.add(compilationUnit.getWorkingCopy(null));
				}
			}
		} catch (JavaModelException e) {
			Activator.log(Status.ERROR, e.getMessage(), e);
			throw new RefactoringException("Could not get compilation unit from java elements",
					"Error parsing java files,\n please check your workspace and try again.", e);
		}
	}

	public void doRefactoring() throws RefactoringException, RuleException {
		if (workingCopies.isEmpty()) {
			Activator.log(Status.WARNING, Messages.AbstractRefactorer_warn_no_working_copies_foung, null);
			throw new RefactoringException(Messages.AbstractRefactorer_warn_no_working_copies_foung);
		}
		List<String> notWorkingRules = new ArrayList<>();
		for (RefactoringRule<? extends ASTVisitor> refactoringRule : rules) {
			try {
				refactoringRule.generateDocumentChanges(workingCopies);
			} catch (JavaModelException | ReflectiveOperationException e) {
				Activator.log(Status.ERROR, e.getMessage(), e);
				notWorkingRules.add(refactoringRule.getName());
			}
		}
		if (!notWorkingRules.isEmpty()) {
			String notWorkingRulesCollected = notWorkingRules.stream().collect(Collectors.joining(", "));
			throw new RuleException(NLS.bind("Could not execute the following rules\n[{0}]", notWorkingRulesCollected),
					NLS.bind("Could not execute the following rules\n[{0}]", notWorkingRulesCollected));
		}
	}

	public void commitRefactoring() throws RefactoringException, ReconcileException {
		if (workingCopies.isEmpty()) {
			Activator.log(Status.WARNING, Messages.AbstractRefactorer_warn_no_working_copies_foung, null);
			throw new RefactoringException(Messages.AbstractRefactorer_warn_no_working_copies_foung);
		}
		List<String> workingCopiesNotCommited = new ArrayList<>();
		for (Iterator<ICompilationUnit> iterator = workingCopies.iterator(); iterator.hasNext();) {
			ICompilationUnit workingCopy = (ICompilationUnit) iterator.next();
			try {
				SimonykeesUtil.commitAndDiscardWorkingCopy(workingCopy);
				iterator.remove();
			} catch (JavaModelException e) {
				Activator.log(Status.ERROR, e.getMessage(), e);
				workingCopiesNotCommited.add(workingCopy.getPath().toString());
			}
		}
		if (!workingCopiesNotCommited.isEmpty()) {
			String notWorkingRulesCollected = workingCopiesNotCommited.stream().collect(Collectors.joining("\n"));
			throw new ReconcileException(NLS.bind("Could not commit the following working copies:\n[{0}]", notWorkingRulesCollected),
					NLS.bind("Could not commit the changes to the following files:\n[{0}]", notWorkingRulesCollected));
		}
	}

	public List<RefactoringRule<? extends ASTVisitor>> getRules() {
		return Collections.unmodifiableList(rules);
	}

	public boolean hasChanges() {
		for (RefactoringRule<? extends ASTVisitor> rule : rules) {
			if (!rule.getDocumentChanges().isEmpty()) {
				return true;
			}
		}
		return false;
	}

	@Deprecated
	public void doOldRefactoring() {
		List<ICompilationUnit> compilationUnits = new ArrayList<>();

		try {
			SimonykeesUtil.collectICompilationUnits(compilationUnits, javaElements);

			if (compilationUnits.isEmpty()) {
				Activator.log(Status.WARNING, Messages.AbstractRefactorer_warn_no_compilation_units_found, null);
				return;
			}

			for (ICompilationUnit compilationUnit : compilationUnits) {
				for (RefactoringRule<? extends ASTVisitor> rule : rules) {
					ICompilationUnit workingCopy = compilationUnit.getWorkingCopy(null);

					try {
						SimonykeesUtil.applyRule(workingCopy, rule.getVisitor());
					} catch (ReflectiveOperationException e) {
						Activator.log(Status.ERROR,
								NLS.bind(Messages.AbstractRefactorer_error_cannot_init_rule, rule.getName()), e);
					}

					SimonykeesUtil.commitAndDiscardWorkingCopy(workingCopy);
				}

			}

		} catch (JavaModelException e) {
			Activator.log(Status.ERROR, e.getMessage(), null);
			// FIXME should also throw an exception
		}
	}

}
