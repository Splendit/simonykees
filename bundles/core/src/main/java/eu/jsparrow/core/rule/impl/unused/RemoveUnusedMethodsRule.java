package eu.jsparrow.core.rule.impl.unused;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.ltk.core.refactoring.DocumentChange;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.TextEditGroup;

import eu.jsparrow.core.visitor.unused.UnusedClassMemberWrapper;
import eu.jsparrow.core.visitor.unused.method.RemoveUnusedMethodsASTVisitor;
import eu.jsparrow.core.visitor.unused.method.TestSourceReference;
import eu.jsparrow.core.visitor.unused.method.UnusedMethodWrapper;
import eu.jsparrow.core.visitor.unused.method.UnusedMethodsEngine;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;
import eu.jsparrow.rules.common.statistics.RuleApplicationCount;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * Finds and removes unused methods.
 * 
 * @see RemoveUnusedMethodsASTVisitor
 * @see UnusedMethodsEngine
 * 
 * @since 4.9.0
 *
 */
public class RemoveUnusedMethodsRule extends RefactoringRuleImpl<RemoveUnusedMethodsASTVisitor> {

	private List<UnusedMethodWrapper> unusedMethods;

	public RemoveUnusedMethodsRule(List<UnusedMethodWrapper> unusedMethods) {
		this.visitorClass = RemoveUnusedMethodsASTVisitor.class;
		this.id = "RemoveUnusedMethods"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription("Remove Unused Methods", //$NON-NLS-1$
				"Finds and removes methods that are never used actively.", Duration.ofMinutes(2), //$NON-NLS-1$
				Arrays.asList(Tag.JAVA_1_1, Tag.READABILITY, Tag.CODING_CONVENTIONS));
		this.unusedMethods = unusedMethods;
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_1;
	}

	@Override
	protected AbstractASTRewriteASTVisitor visitorFactory() {
		RemoveUnusedMethodsASTVisitor visitor = new RemoveUnusedMethodsASTVisitor(unusedMethods);
		visitor.addRewriteListener(RuleApplicationCount.getFor(this));
		return visitor;
	}

	public Map<UnusedClassMemberWrapper, Map<ICompilationUnit, DocumentChange>> computeDocumentChangesPerMethod()
			throws JavaModelException {

		Map<UnusedClassMemberWrapper, Map<ICompilationUnit, DocumentChange>> map = new HashMap<>();
		for (UnusedMethodWrapper unusedMethod : unusedMethods) {
			Map<ICompilationUnit, DocumentChange> documentChanges = computeDocumentChangesForUnusedMethod(
					unusedMethod);
			map.put(unusedMethod, documentChanges);
		}
		return map;
	}

	private Map<ICompilationUnit, DocumentChange> computeDocumentChangesForUnusedMethod(
			UnusedMethodWrapper unusedMethod) throws JavaModelException {

		List<ICompilationUnit> targetCompilationUnits = unusedMethod.getTargetICompilationUnits();
		Map<ICompilationUnit, DocumentChange> documentChanges = new HashMap<>();
		for (ICompilationUnit targetICU : targetCompilationUnits) {
			TextEditGroup editGroup = unusedMethod.getTextEditGroup(targetICU);
			if (!editGroup.isEmpty()) {
				MethodDeclaration declaration = unusedMethod.getMethodDeclaration();
				DocumentChange documentChange = DocumentChangeUtil.createDocumentChange(targetICU);
				TextEdit rootEdit = new MultiTextEdit();
				documentChange.setEdit(rootEdit);
				addDeclarationTextEdits(unusedMethod, targetICU, declaration, documentChange);
				IPath targetICUPath = targetICU.getPath();
				unusedMethod.getTestReferences()
					.stream()
					.filter(externalReference -> comparePaths(targetICUPath, externalReference))
					.forEach(externalReference -> addTestReferencesTextEdits(documentChange, externalReference));
				documentChange.setTextType("java"); //$NON-NLS-1$
				documentChanges.put(targetICU, documentChange);
			}
		}

		return documentChanges;
	}

	private boolean comparePaths(IPath targetICUPath, TestSourceReference externalReference) {
		CompilationUnit cu = externalReference.getCompilationUnit();
		ICompilationUnit icu = (ICompilationUnit) cu.getJavaElement();
		return targetICUPath.equals(icu.getPath());
	}

	private void addTestReferencesTextEdits(DocumentChange documentChange, TestSourceReference externalReference) {
		for (MethodDeclaration test : externalReference.getTestDeclarations()) {
			DeleteEdit deleteEdit = new DeleteEdit(test.getStartPosition(),
					test.getLength());
			documentChange.addEdit(deleteEdit);
		}
	}

	private void addDeclarationTextEdits(UnusedMethodWrapper unusedMethod, ICompilationUnit iCompilationUnit,
			MethodDeclaration declaration, DocumentChange documentChange) {
		int offset = declaration.getStartPosition();
		int length = declaration.getLength();
		IPath declarationPath = unusedMethod.getDeclarationPath();
		IPath currentPath = iCompilationUnit.getPath();
		if (currentPath.equals(declarationPath)) {
			DeleteEdit declDeleteEdit = new DeleteEdit(offset, length);
			documentChange.addEdit(declDeleteEdit);
		}
	}

	public void dropUnusedMethod(UnusedClassMemberWrapper unusedMethod) {
		this.unusedMethods.remove(unusedMethod);
	}

	public void addUnusedMethod(UnusedClassMemberWrapper unusedMethod) {
		if (unusedMethod instanceof UnusedMethodWrapper) {
			this.unusedMethods.add((UnusedMethodWrapper) unusedMethod);
		}
	}
}
