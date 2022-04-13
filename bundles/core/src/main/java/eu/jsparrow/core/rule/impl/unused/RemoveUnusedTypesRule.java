package eu.jsparrow.core.rule.impl.unused;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.ltk.core.refactoring.DocumentChange;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.TextEditGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.visitor.unused.UnusedClassMemberWrapper;
import eu.jsparrow.core.visitor.unused.type.RemoveUnusedTypesASTVisitor;
import eu.jsparrow.core.visitor.unused.type.TestReferenceOnType;
import eu.jsparrow.core.visitor.unused.type.UnusedTypeWrapper;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;
import eu.jsparrow.rules.common.exception.RefactoringException;
import eu.jsparrow.rules.common.statistics.RuleApplicationCount;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

public class RemoveUnusedTypesRule extends RefactoringRuleImpl<RemoveUnusedTypesASTVisitor> {

	private static final Logger logger = LoggerFactory.getLogger(RemoveUnusedTypesRule.class);

	private List<UnusedTypeWrapper> unusedTypes;

	public RemoveUnusedTypesRule(List<UnusedTypeWrapper> unusedTypes) {
		this.visitorClass = RemoveUnusedTypesASTVisitor.class;
		this.id = "RemoveUnusedTypes"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription("Remove Unused Types", //$NON-NLS-1$
				"Finds and removes types that are not used.", Duration.ofMinutes(2), //$NON-NLS-1$
				Arrays.asList(Tag.JAVA_1_1, Tag.READABILITY, Tag.CODING_CONVENTIONS));
		this.unusedTypes = unusedTypes;
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_1;
	}

	@Override
	protected AbstractASTRewriteASTVisitor visitorFactory() {
		RemoveUnusedTypesASTVisitor visitor = new RemoveUnusedTypesASTVisitor(unusedTypes);
		visitor.addRewriteListener(RuleApplicationCount.getFor(this));
		return visitor;
	}

	public Map<UnusedClassMemberWrapper, Map<ICompilationUnit, DocumentChange>> computeDocumentChangesPerType()
			throws JavaModelException {
		Map<UnusedClassMemberWrapper, Map<ICompilationUnit, DocumentChange>> map = new HashMap<>();
		for (UnusedTypeWrapper unusedType : unusedTypes) {
			Map<ICompilationUnit, DocumentChange> documentChanges = computeDocumentChangesForUnusedType(
					unusedType);
			map.put(unusedType, documentChanges);
		}
		return map;
	}

	private Map<ICompilationUnit, DocumentChange> computeDocumentChangesForUnusedType(
			UnusedTypeWrapper unusedTypeWrapper) throws JavaModelException {

		List<ICompilationUnit> targetCompilationUnits = unusedTypeWrapper.getTargetICompilationUnits();
		Map<ICompilationUnit, DocumentChange> documentChanges = new HashMap<>();
		for (ICompilationUnit targetICU : targetCompilationUnits) {
			TextEditGroup editGroup = unusedTypeWrapper.getTextEditGroup(targetICU);
			if (!editGroup.isEmpty()) {
				AbstractTypeDeclaration declaration = unusedTypeWrapper.getTypeDeclaration();
				DocumentChange documentChange = DocumentChangeUtil.createDocumentChange(targetICU);
				TextEdit rootEdit = new MultiTextEdit();
				documentChange.setEdit(rootEdit);
				addDeclarationTextEdits(unusedTypeWrapper, targetICU, declaration, documentChange);

				unusedTypeWrapper.getTestReferencesOnType()
					.stream()
					.filter(externalreference -> comparePaths(targetICU.getPath(), externalreference))
					.forEach(externalReference -> addTestReferencesTextEdits(documentChange, externalReference));
				documentChange.setTextType("java"); //$NON-NLS-1$
				documentChanges.put(targetICU, documentChange);
			}
		}

		return documentChanges;
	}

	private void addTestReferencesTextEdits(DocumentChange documentChange, TestReferenceOnType externalReference) {
		CompilationUnit cu = externalReference.getCompilationUnit();
		DeleteEdit deleteEdit = new DeleteEdit(0, cu.getLength());
		documentChange.addEdit(deleteEdit);
	}

	private boolean comparePaths(IPath path, TestReferenceOnType externalReference) {
		CompilationUnit cu = externalReference.getCompilationUnit();
		ICompilationUnit icu = (ICompilationUnit) cu.getJavaElement();
		return path.equals(icu.getPath());
	}

	private void addDeclarationTextEdits(UnusedTypeWrapper unusedTypeWrapper, ICompilationUnit iCompilationUnit,
			AbstractTypeDeclaration declaration, DocumentChange documentChange) {
		int offset;
		int length;
		if (unusedTypeWrapper.isMainType()) {
			offset = 0;
			length = unusedTypeWrapper.getCompilationUnit()
				.getLength();
		} else {
			offset = declaration.getStartPosition();
			length = declaration.getLength();
		}

		IPath declarationPath = unusedTypeWrapper.getDeclarationPath();
		IPath currentPath = iCompilationUnit.getPath();
		if (currentPath.equals(declarationPath)) {
			DeleteEdit declDeleteEdit = new DeleteEdit(offset, length);
			documentChange.addEdit(declDeleteEdit);
		}
	}

	public void dropUnusedType(UnusedClassMemberWrapper unusedType) {
		this.unusedTypes.remove(unusedType);
	}

	public void addUnusedType(UnusedClassMemberWrapper unusedType) {
		if (unusedType instanceof UnusedTypeWrapper) {
			this.unusedTypes.add((UnusedTypeWrapper) unusedType);
		}
	}

	public void deleteEmptyCompilationUnits() throws RefactoringException {
		List<ICompilationUnit> unableToRemove = new ArrayList<>();
		for (UnusedTypeWrapper typeWrapper : unusedTypes) {
			if (typeWrapper.isMainType()) {
				CompilationUnit compilationUnit = typeWrapper.getCompilationUnit();
				ICompilationUnit icu = (ICompilationUnit) compilationUnit.getJavaElement();
				try {
					icu.delete(true, null);
				} catch (JavaModelException e) {
					String message = String.format("Cannot delete %s. %s.", icu.getElementName(), e.getMessage());//$NON-NLS-1$
					logger.error(message, e);
					unableToRemove.add(icu);
				}
			}
			List<TestReferenceOnType> testReferencesOnType = typeWrapper.getTestReferencesOnType();
			for (TestReferenceOnType testReference : testReferencesOnType) {
				ICompilationUnit icu = testReference.getICompilationUnit();
				try {
					icu.delete(true, null);
				} catch (JavaModelException e) {
					String message = String.format("Cannot delete %s. %s.", icu.getElementName(), e.getMessage());//$NON-NLS-1$
					logger.error(message, e);
					unableToRemove.add(icu);
				}
			}
		}
		if (!unableToRemove.isEmpty()) {
			String names = unableToRemove.stream()
				.map(ICompilationUnit::getElementName)
				.collect(Collectors.joining(",")); //$NON-NLS-1$
			String message = "The following compilation units could not be removed: " + names; //$NON-NLS-1$
			throw new RefactoringException(message);
		}
	}
}
