package eu.jsparrow.core.rule.impl.unused;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jface.text.Document;
import org.eclipse.ltk.core.refactoring.DocumentChange;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.TextEditGroup;

import eu.jsparrow.core.visitor.unused.RemoveUnusedFieldsASTVisitor;
import eu.jsparrow.core.visitor.unused.UnusedFieldWrapper;
import eu.jsparrow.core.visitor.unused.UnusedFieldsEngine;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;
import eu.jsparrow.rules.common.statistics.RuleApplicationCount;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * Removes the provided list of unused fields ({@link UnusedFieldWrapper}). See
 * {@link UnusedFieldsEngine} to search for unused fields.
 * 
 * @see RemoveUnusedFieldsASTVisitor
 * 
 * @since 4.8.0
 *
 */
public class RemoveUnusedFieldsRule extends RefactoringRuleImpl<RemoveUnusedFieldsASTVisitor> {

	private List<UnusedFieldWrapper> unusedFields;

	public RemoveUnusedFieldsRule(List<UnusedFieldWrapper> unusedFields) {
		this.visitorClass = RemoveUnusedFieldsASTVisitor.class;
		this.id = "RemoveUnusedFields"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(Messages.RemoveUnusedFieldsRule_name,
				Messages.RemoveUnusedFieldsRule_description, Duration.ofMinutes(2),
				Arrays.asList(Tag.JAVA_1_1, Tag.READABILITY, Tag.CODING_CONVENTIONS));
		this.unusedFields = unusedFields;
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_1;
	}

	@Override
	protected AbstractASTRewriteASTVisitor visitorFactory() {
		RemoveUnusedFieldsASTVisitor visitor = new RemoveUnusedFieldsASTVisitor(unusedFields);
		visitor.addRewriteListener(RuleApplicationCount.getFor(this));
		return visitor;
	}

	public Map<UnusedFieldWrapper, Map<ICompilationUnit, DocumentChange>> computeDocumentChangesPerField()
			throws JavaModelException {
		Map<UnusedFieldWrapper, Map<ICompilationUnit, DocumentChange>> map = new HashMap<>();
		for (UnusedFieldWrapper unusedField : unusedFields) {
			Map<ICompilationUnit, DocumentChange> unusedFieldDocumentChanges = computeDocumentChangesForUnusedField(
					unusedField);
			map.put(unusedField, unusedFieldDocumentChanges);
		}
		return map;

	}

	public Map<ICompilationUnit, DocumentChange> computeDocumentChangesForUnusedField(UnusedFieldWrapper unusedField)
			throws JavaModelException {
		List<ICompilationUnit> targetCompilationUnits = unusedField.getTargetICompilationUnits();
		Map<ICompilationUnit, DocumentChange> documentChanges = new HashMap<>();
		for (ICompilationUnit iCompilationUnit : targetCompilationUnits) {
			TextEditGroup editGroup = unusedField.getTextEditGroup(iCompilationUnit);
			if (!editGroup.isEmpty()) {

				VariableDeclarationFragment oldFragment = unusedField.getFragment();
				Document doc = new Document(iCompilationUnit.getPrimary()
					.getSource());
				DocumentChange documentChange = new DocumentChange(
						iCompilationUnit.getElementName() + " - " + getPathString(iCompilationUnit), doc); //$NON-NLS-1$
				TextEdit rootEdit = new MultiTextEdit();
				documentChange.setEdit(rootEdit);
				FieldDeclaration fieldDeclaration = (FieldDeclaration) oldFragment.getParent();
				addDeclarationTextEdits(unusedField, iCompilationUnit, oldFragment, documentChange, fieldDeclaration);

				unusedField.getUnusedExternalReferences()
					.stream()
					.filter(externalReference -> {
						CompilationUnit cu = externalReference.getCompilationUnit();
						ICompilationUnit icu = (ICompilationUnit) cu.getJavaElement();
						return !comparePaths(icu.getPath(), unusedField.getDeclarationPath());
					})
					.forEach(externalReference -> {
						CompilationUnit cu = externalReference.getCompilationUnit();
						ICompilationUnit icu = (ICompilationUnit) cu.getJavaElement();
						if (comparePaths(iCompilationUnit, icu)) {
							for (ExpressionStatement statement : externalReference.getUnusedReassignments()) {
								DeleteEdit deleteEdit = new DeleteEdit(statement.getStartPosition(),
										statement.getLength());
								documentChange.addEdit(deleteEdit);
							}
						}
					});
				documentChange.setTextType("java"); //$NON-NLS-1$
				documentChanges.put(iCompilationUnit, documentChange);
			}
		}

		return documentChanges;
	}

	private void addDeclarationTextEdits(UnusedFieldWrapper unusedField, ICompilationUnit iCompilationUnit,
			VariableDeclarationFragment oldFragment, DocumentChange documentChange, FieldDeclaration fieldDeclaration) {
		List<VariableDeclarationFragment> allFragments = ASTNodeUtil.convertToTypedList(fieldDeclaration.fragments(),
				VariableDeclarationFragment.class);
		int declOffset;
		int length;
		if (allFragments.size() != 1) {
			declOffset = oldFragment.getStartPosition();
			length = oldFragment.getLength();
		} else {
			declOffset = fieldDeclaration.getStartPosition();
			length = fieldDeclaration.getLength();
		}

		if (comparePaths(iCompilationUnit.getPath(), unusedField.getDeclarationPath())) {

			DeleteEdit declDeleteEdit = new DeleteEdit(declOffset, length);
			documentChange.addEdit(declDeleteEdit);
			unusedField.getUnusedReassignments()
				.forEach(reassignment -> {
					DeleteEdit deleteEdit = new DeleteEdit(reassignment.getStartPosition(), reassignment.getLength());
					documentChange.addEdit(deleteEdit);
				});
		}
	}

	private boolean comparePaths(ICompilationUnit iCompilationUnit, ICompilationUnit icu) {
		return comparePaths(icu.getPath(), iCompilationUnit.getPath());
	}

	private boolean comparePaths(IPath path1, IPath path2) {
		return path1.toString()
			.equals(path2.toString());
	}

	/**
	 * Returns the path of an {@link ICompilationUnit} without leading slash
	 * (the same as in the Externalize Strings refactoring view).
	 * 
	 * @param compilationUnit
	 * @return
	 */
	private String getPathString(ICompilationUnit compilationUnit) {
		String temp = compilationUnit.getParent()
			.getPath()
			.toString();
		return temp.startsWith("/") ? temp.substring(1) : temp; //$NON-NLS-1$
	}

	public List<UnusedFieldWrapper> getUnusedFieldWrapperList() {
		return this.unusedFields;
	}
}
