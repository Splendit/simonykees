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
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.ltk.core.refactoring.DocumentChange;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.TextEditGroup;

import eu.jsparrow.core.visitor.unused.UnusedClassMemberWrapper;
import eu.jsparrow.core.visitor.unused.type.RemoveUnusedTypesASTVisitor;
import eu.jsparrow.core.visitor.unused.type.UnusedTypeWrapper;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;
import eu.jsparrow.rules.common.statistics.RuleApplicationCount;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

public class RemoveUnusedTypesRule extends RefactoringRuleImpl<RemoveUnusedTypesASTVisitor> {

	private List<UnusedTypeWrapper> unusedTypes;

	public RemoveUnusedTypesRule(List<UnusedTypeWrapper> unusedTypes) {
		this.visitorClass = RemoveUnusedTypesASTVisitor.class;
		this.id = "RemoveUnusedTypesRule"; //$NON-NLS-1$
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
			Map<ICompilationUnit, DocumentChange> documentChanges = computeDocumentChangesForUnusedMethod(
					unusedType);
			map.put(unusedType, documentChanges);
		}
		return map;
	}

	private Map<ICompilationUnit, DocumentChange> computeDocumentChangesForUnusedMethod(
			UnusedTypeWrapper unusedMethod) throws JavaModelException {

		List<ICompilationUnit> targetCompilationUnits = unusedMethod.getTargetICompilationUnits();
		Map<ICompilationUnit, DocumentChange> documentChanges = new HashMap<>();
		for (ICompilationUnit targetICU : targetCompilationUnits) {
			TextEditGroup editGroup = unusedMethod.getTextEditGroup(targetICU);
			if (!editGroup.isEmpty()) {
				AbstractTypeDeclaration declaration = unusedMethod.getTypeDeclaration();
				DocumentChange documentChange = DocumentChangeUtil.createDocumentChange(targetICU);
				TextEdit rootEdit = new MultiTextEdit();
				documentChange.setEdit(rootEdit);
				addDeclarationTextEdits(unusedMethod, targetICU, declaration, documentChange);

				documentChange.setTextType("java"); //$NON-NLS-1$
				documentChanges.put(targetICU, documentChange);
			}
		}

		return documentChanges;
	}

	private void addDeclarationTextEdits(UnusedTypeWrapper unusedMethod, ICompilationUnit iCompilationUnit,
			AbstractTypeDeclaration declaration, DocumentChange documentChange) {
		int offset = declaration.getStartPosition();
		int length = declaration.getLength();
		IPath declarationPath = unusedMethod.getDeclarationPath();
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
}
