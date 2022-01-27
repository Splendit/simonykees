package eu.jsparrow.core.visitor.unused;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import eu.jsparrow.rules.common.util.RefactoringUtil;

public class UnusedFieldsEngine {
	
	/*
	 * - Gets the list of the selected sources. 
	 * - Uses UnusedFieldsCandidatesVisitor to make a list of potential fields to be removed. 
	 * 	 -- the unused private fields are immediately classified to be removed. 
	 *   -- for non-private field candidates, we need to analyze the search scope. Either with the eclipse search engine, or by implementing our own search engine. 
	 *   -- As a result, should get a list of 'UnusedFieldWrapper' that contains all the information about the fields that should be removed. 
	 *  
	 * - From the list of 'UnusedFieldWrapper', prepare the list of relevant compilation units. 
	 * - Use the 'RemoveUnusedFieldsASTVisitor' to remove the unused fields. 
	 */

	private String scope;
	
	public UnusedFieldsEngine(String scope) {
		this.scope = scope;
	}

	public List<UnusedFieldWrapper> findUnusedFields(List<ICompilationUnit> selectedJavaElements,
			Map<String, Boolean> optionsMap) {
		List<CompilationUnit> compilationUnits = new ArrayList<>();
		for (ICompilationUnit icu : selectedJavaElements) {
			CompilationUnit cu = RefactoringUtil.parse(icu);
			compilationUnits.add(cu);
		}
		
		List<UnusedFieldWrapper> list = new ArrayList<>();
		for (CompilationUnit cu : compilationUnits) {
			UnusedFieldsCandidatesVisitor visitor = new UnusedFieldsCandidatesVisitor(optionsMap);
			cu.accept(visitor);
			List<UnusedFieldWrapper> unusedPrivateFields = visitor.getUnusedPrivateFields();
			list.addAll(unusedPrivateFields);
			List<VariableDeclarationFragment> nonPrivateCandidates = visitor.getNonPrivateCandidates();
			List<UnusedFieldWrapper> nonPrivate = findExternalUnusedReferences(cu, nonPrivateCandidates);
			list.addAll(nonPrivate);
			
		}
		return list;
	}
	
	private List<UnusedFieldWrapper> findExternalUnusedReferences(CompilationUnit compilationUnit, List<VariableDeclarationFragment> nonPrivateCandidates) {
		List<UnusedFieldWrapper> list = new ArrayList<>();
		/* 
		 * Get the compilation units of the search scope. 
		 * Parse them and find the external references. Existing search engine? Or our own?
		 * 		--> Maybe implement both, and figure out which one performs faster? Also which one is more accurate. 
		 * Put all the reference in UnusedFieldWrapper and add it to the resulting list. 
		 */
		return list;
	}
	

}
