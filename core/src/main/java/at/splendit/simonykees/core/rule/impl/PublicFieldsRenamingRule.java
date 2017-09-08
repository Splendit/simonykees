package at.splendit.simonykees.core.rule.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.JavaVersion;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.Document;
import org.eclipse.ltk.core.refactoring.DocumentChange;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEditGroup;

import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.visitor.renaming.FieldMetadata;
import at.splendit.simonykees.core.visitor.renaming.PublicFieldsRenamingASTVisitor;
import at.splendit.simonykees.i18n.Messages;

/**
 * @see PublicFieldsRenamingASTVisitor
 * 
 * @author Ardit Ymeri
 * @since 2.1.0
 *
 */
public class PublicFieldsRenamingRule extends RefactoringRule<PublicFieldsRenamingASTVisitor> {

	private List<FieldMetadata> metaData;
	
	public PublicFieldsRenamingRule(Class<PublicFieldsRenamingASTVisitor> visitor,
			List<FieldMetadata> metaData) {
		super(visitor);
		this.metaData = metaData;
		this.name = Messages.PublicFieldsRenamingRule_name;
		this.description = Messages.PublicFieldsRenamingRule_description;
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_1;
	}

	@Override
	public PublicFieldsRenamingASTVisitor visitorFactory() {
		return new PublicFieldsRenamingASTVisitor(metaData);
	}
	
	/**
	 * Computes the list of document changes related to the renaming of a field
	 * represented by the given {@link FieldMetadata}.
	 * 
	 * @param metaData
	 *            the metadata containing information about a field being
	 *            renamed.
	 * @return the list of document changes for all complation units that are
	 *         affected by the renaming of the field.
	 * @throws JavaModelException
	 */
	public List<DocumentChange> computeDocumentChangesPerFiled(FieldMetadata metaData) throws JavaModelException {
		List<ICompilationUnit> targetCompilationUnits = metaData.getTargetICompilationUnits();
		List<DocumentChange> documentChanges = new ArrayList<>();
		for (ICompilationUnit iCompilationUnit : targetCompilationUnits) {
			TextEditGroup editGroup = metaData.getTextEditGroup(iCompilationUnit);
			if (!editGroup.isEmpty()) {
				Document document = new Document(iCompilationUnit.getSource());
				DocumentChange documentChange = new DocumentChange(metaData.getNewIdentifier(), document);
				documentChange.setEdit(new MultiTextEdit());
				documentChange.addTextEditGroup(editGroup);
				
				documentChanges.add(documentChange);
			}
		}

		return documentChanges;
	}
	
	/**
	 * Clears all the text edits related to the renaming of a field. 
	 * 
	 * @param metaData the metadata representing the field being renamed. 
	 */
	public void clearTextEdits(FieldMetadata metaData) {
		List<ICompilationUnit> targetCompilationUnits = metaData.getTargetICompilationUnits();
		for(ICompilationUnit iCompilationUnit : targetCompilationUnits) {
			TextEditGroup editGroup = metaData.getTextEditGroup(iCompilationUnit);
			editGroup.clearTextEdits();
		}
	}
}
