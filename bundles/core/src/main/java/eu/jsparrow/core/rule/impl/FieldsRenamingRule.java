package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jface.text.Document;
import org.eclipse.ltk.core.refactoring.DocumentChange;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.TextEditGroup;

import eu.jsparrow.core.visitor.renaming.FieldMetaData;
import eu.jsparrow.core.visitor.renaming.FieldsRenamingASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;
import eu.jsparrow.rules.common.statistics.RuleApplicationCount;

/**
 * @see FieldsRenamingASTVisitor
 * 
 * @author Ardit Ymeri
 * @since 2.3.0
 *
 */
public class FieldsRenamingRule extends RefactoringRuleImpl<FieldsRenamingASTVisitor> {

	private List<FieldMetaData> metaData;
	private List<FieldMetaData> todosMetaData;
	public static final String FIELDS_RENAMING_RULE_ID = "FieldRenaming"; //$NON-NLS-1$

	public FieldsRenamingRule(List<FieldMetaData> metaData, List<FieldMetaData> todosMetaData) {
		this.visitorClass = FieldsRenamingASTVisitor.class;
		this.metaData = metaData;
		this.todosMetaData = todosMetaData;
		this.id = FIELDS_RENAMING_RULE_ID;
		this.ruleDescription = new RuleDescription(Messages.PublicFieldsRenamingRule_name,
				Messages.PublicFieldsRenamingRule_description, Duration.ofMinutes(15),
				Arrays.asList(Tag.JAVA_1_1, Tag.CODING_CONVENTIONS, Tag.READABILITY));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_1;
	}

	@Override
	public FieldsRenamingASTVisitor visitorFactory() {
		FieldsRenamingASTVisitor visitor = new FieldsRenamingASTVisitor(metaData, todosMetaData);
		visitor.addRewriteListener(RuleApplicationCount.getFor(this));
		return visitor;

	}

	/**
	 * Computes the list of document changes related to the renaming of a field
	 * represented by the given {@link FieldMetaData}.
	 * 
	 * @param metaData
	 *            the metaData containing information about a field being
	 *            renamed.
	 * @return the list of document changes for all compilation units that are
	 *         affected by the renaming of the field.
	 * @throws JavaModelException
	 */
	public Map<ICompilationUnit, DocumentChange> computeDocumentChangesPerFiled(FieldMetaData metaData)
			throws JavaModelException {
		List<ICompilationUnit> targetCompilationUnits = metaData.getTargetICompilationUnits();
		Map<ICompilationUnit, DocumentChange> documentChanges = new HashMap<>();
		for (ICompilationUnit iCompilationUnit : targetCompilationUnits) {
			TextEditGroup editGroup = metaData.getTextEditGroup(iCompilationUnit);
			if (!editGroup.isEmpty()) {
				String newIdentifier = metaData.getNewIdentifier();
				int newIdentifierLength = newIdentifier.length();
				VariableDeclarationFragment oldFragment = metaData.getFieldDeclaration();
				Document doc = new Document(iCompilationUnit.getPrimary()
					.getSource());
				DocumentChange documentChange = new DocumentChange(
						iCompilationUnit.getElementName() + " - " + getPathString(iCompilationUnit), doc); //$NON-NLS-1$
				TextEdit rootEdit = new MultiTextEdit();
				documentChange.setEdit(rootEdit);
				int delta = oldFragment.getName()
					.getLength() - newIdentifierLength;
				if (iCompilationUnit.getPath()
					.toString()
					.equals(metaData.getDeclarationPath()
						.toString())) {
					int declOffset = oldFragment.getStartPosition();
					InsertEdit declInsertEdit = new InsertEdit(declOffset, newIdentifier);
					DeleteEdit declDeleteEdit = new DeleteEdit(declOffset, delta + newIdentifierLength);
					documentChange.addEdit(declInsertEdit);
					documentChange.addEdit(declDeleteEdit);
				}
				metaData.getReferences()
					.forEach(match -> {
						if (match.getResource()
							.getFullPath()
							.toString()
							.equals(iCompilationUnit.getPath()
								.toString())) {
							InsertEdit insertEdit = new InsertEdit(match.getOffset(), newIdentifier);
							DeleteEdit deleteEdit = new DeleteEdit(match.getOffset(), delta + newIdentifierLength);
							documentChange.addEdit(insertEdit);
							documentChange.addEdit(deleteEdit);
						}
					});
				documentChange.setTextType("java"); //$NON-NLS-1$
				documentChanges.put(iCompilationUnit, documentChange);
			}
		}

		return documentChanges;
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

	/**
	 * Computes the list of the document changes related to the comment nodes
	 * inserted above the fields that could not be renamed.
	 * 
	 * @param todosEditGroups
	 *            edit groups for each compilation unit.
	 * @return list of document changes containing the inserted comments.
	 * @throws JavaModelException
	 *             if an exception occurs when reading the resource of an
	 *             {@link ICompilationUnit}
	 */
	public List<DocumentChange> computeTodosDocumentChanges(Map<ICompilationUnit, TextEditGroup> todosEditGroups)
			throws JavaModelException {
		List<DocumentChange> documentChanges = new ArrayList<>();

		for (Map.Entry<ICompilationUnit, TextEditGroup> entry : todosEditGroups.entrySet()) {
			ICompilationUnit iCompilationUnit = entry.getKey();
			TextEditGroup editGroup = entry.getValue();
			if (!editGroup.isEmpty()) {
				Document document = new Document(iCompilationUnit.getSource());
				DocumentChange documentChange = new DocumentChange(editGroup.getName(), document);
				documentChange.setEdit(new MultiTextEdit());
				for (TextEdit edit : editGroup.getTextEdits()) {
					documentChange.addEdit(edit.copy());
				}
				documentChanges.add(documentChange);
			}
		}

		return documentChanges;
	}
	
	public List<FieldMetaData> getMetaData() {
		return this.metaData;
	}
}
