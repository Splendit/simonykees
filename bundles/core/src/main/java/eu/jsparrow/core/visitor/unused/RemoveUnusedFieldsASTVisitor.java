package eu.jsparrow.core.visitor.unused;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.text.edits.TextEditGroup;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

public class RemoveUnusedFieldsASTVisitor extends AbstractASTRewriteASTVisitor {
	
	private List<UnusedFieldWrapper> unusedFields;
	/*
	 * Gets a list of unused field wrappers.
	 * Finds the relevant ones for the given compilation unit. 
	 * Removes them. 
	 */
	
	/*
	 * TODO: make a list of fragments to be removed. 
	 * TODO: make a list of assignment statements to be removed. 
	 */
	
	public RemoveUnusedFieldsASTVisitor(List<UnusedFieldWrapper> unusedFields) {
		this.unusedFields = unusedFields;
	}
	
	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		// TOODO: find all relevant declaration fragments and reassignments that should be removed from this compilation unit. 
		IPath currentPath = compilationUnit.getJavaElement().getPath();
		List<UnusedFieldWrapper> relevantUnusedFields = new ArrayList<>();
		for(UnusedFieldWrapper unusedField : unusedFields) {
			IPath path = unusedField.getDeclarationPath();
			if (path.equals(currentPath)) {
				relevantUnusedFields.add(unusedField);
			} else {
				List<ICompilationUnit> targetCompilationUnits = unusedField.getTargetICompilationUnits();
				for (ICompilationUnit targetICU : targetCompilationUnits) {
					if(currentPath.equals(targetICU.getPath())) {
						relevantUnusedFields.add(unusedField);
						break;
					}
				}
			}
			
		}
		return super.visit(compilationUnit);
	}
	
	@Override
	public boolean visit(FieldDeclaration fieldDeclaration) {
		List<VariableDeclarationFragment> fragments = ASTNodeUtil.convertToTypedList(fieldDeclaration.fragments(), VariableDeclarationFragment.class);
		for (UnusedFieldWrapper unusedField : unusedFields) {
			VariableDeclarationFragment fragment = unusedField.getFragment();
			
			for(VariableDeclarationFragment declaratingFragment : fragments) {
				if(declaratingFragment.subtreeMatch(new ASTMatcher(), fragment)) {
					TextEditGroup editGroup = unusedField.getTextEditGroup((ICompilationUnit)this.getCompilationUnit().getJavaElement());
					if(fragments.size() == 1) {
						astRewrite.remove(fieldDeclaration, editGroup);
						
					} else {
						astRewrite.remove(declaratingFragment, editGroup);
					}
					onRewrite();
				}
			}
			
		}
		return true;
	}
	
	@Override
	public boolean visit(SimpleName simpleName) {
		// TODO: here we need a map from assignments to UnusedFieldWrapper. We should visit assignments instead of simple names. it is more efficient. 
		if(simpleName.getLocationInParent() == Assignment.LEFT_HAND_SIDE_PROPERTY) {
			TextEditGroup editGroup = unusedFields.get(0).getTextEditGroup((ICompilationUnit)this.getCompilationUnit().getJavaElement());
			astRewrite.remove(simpleName.getParent().getParent(), editGroup);
		}
		return true;
	}

}
