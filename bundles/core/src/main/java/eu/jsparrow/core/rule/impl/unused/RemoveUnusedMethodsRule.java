package eu.jsparrow.core.rule.impl.unused;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jface.text.Document;
import org.eclipse.ltk.core.refactoring.DocumentChange;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.TextEditGroup;

import eu.jsparrow.core.visitor.unused.UnusedClassMemberWrapper;
import eu.jsparrow.core.visitor.unused.method.RemoveUnusedMethodsASTVisitor;
import eu.jsparrow.core.visitor.unused.method.UnusedMethodWrapper;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;
import eu.jsparrow.rules.common.statistics.RuleApplicationCount;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

public class RemoveUnusedMethodsRule extends RefactoringRuleImpl<RemoveUnusedMethodsASTVisitor> {
	
	private List<UnusedMethodWrapper> unusedMethods;

	public RemoveUnusedMethodsRule(List<UnusedMethodWrapper> unusedMethods) {
		this.visitorClass = RemoveUnusedMethodsASTVisitor.class;
		this.id = "RemoveUnusedmethods"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription("Remove Unused Methods", //$NON-NLS-1$
				"Finds and removes unused methods", Duration.ofMinutes(2), //$NON-NLS-1$
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
		for(UnusedMethodWrapper unusedMethod : unusedMethods) {
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
		for (ICompilationUnit iCompilationUnit : targetCompilationUnits) {
			TextEditGroup editGroup = unusedMethod.getTextEditGroup(iCompilationUnit);
			if (!editGroup.isEmpty()) {

				MethodDeclaration declaration = unusedMethod.getMethodDeclaration();
				Document doc = new Document(iCompilationUnit.getPrimary()
					.getSource());
				DocumentChange documentChange = new DocumentChange(
						iCompilationUnit.getElementName() + " - " + getPathString(iCompilationUnit), doc); //$NON-NLS-1$
				TextEdit rootEdit = new MultiTextEdit();
				documentChange.setEdit(rootEdit);
				addDeclarationTextEdits(unusedMethod, iCompilationUnit, declaration, documentChange);
				unusedMethod.getTestReferences()
					.stream()
					.forEach(externalReference -> {
						CompilationUnit cu = externalReference.getCompilationUnit();
						ICompilationUnit icu = (ICompilationUnit) cu.getJavaElement();
						if (comparePaths(iCompilationUnit.getPath(), icu.getPath())) {
							for (MethodDeclaration test : externalReference.getTestDeclarations()) {
								DeleteEdit deleteEdit = new DeleteEdit(test.getStartPosition(),
										test.getLength());
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

	private void addDeclarationTextEdits(UnusedMethodWrapper unusedMethod, ICompilationUnit iCompilationUnit,
			MethodDeclaration declaration, DocumentChange documentChange) {
		int offset = declaration.getStartPosition();
		int length = declaration.getLength();
		if(comparePaths(iCompilationUnit.getPath(), unusedMethod.getDeclarationPath())) {
			DeleteEdit declDeleteEdit = new DeleteEdit(offset, length);
			documentChange.addEdit(declDeleteEdit);
		}
		
	}

	public List<UnusedClassMemberWrapper> getUnusedMethodWrapperList() {
		return unusedMethods.stream()
				.map(UnusedClassMemberWrapper.class::cast)
				.collect(Collectors.toList());
	}
	
	/**
	 * Returns the path of an {@link ICompilationUnit} without leading slash
	 * (the same as in the Externalize Strings refactoring view).
	 * 
	 * @param compilationUnit
	 * @return
	 */
	private String getPathString(ICompilationUnit compilationUnit) { //FIXME: extract me
		String temp = compilationUnit.getParent()
			.getPath()
			.toString();
		return temp.startsWith("/") ? temp.substring(1) : temp; //$NON-NLS-1$
	}
	
	private boolean comparePaths(IPath path1, IPath path2) { //FIXME: extract me
		return path1.toString()
			.equals(path2.toString());
	}
}
