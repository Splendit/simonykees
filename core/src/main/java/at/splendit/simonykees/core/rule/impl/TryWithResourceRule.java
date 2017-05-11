package at.splendit.simonykees.core.rule.impl;

import org.apache.commons.lang3.JavaVersion;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.ltk.core.refactoring.DocumentChange;

import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.visitor.tryStatement.TryWithResourceASTVisitor;
import at.splendit.simonykees.i18n.Messages;
/** 
 * @see TryWithResourceASTVisitor
 * 
 * @author Martin Huter
 * @since 0.9
 *
 */
public class TryWithResourceRule extends RefactoringRule<TryWithResourceASTVisitor> {

	public TryWithResourceRule(Class<TryWithResourceASTVisitor> visitor) {
		super(visitor);
		this.name = Messages.TryWithResourceRule_name;
		this.description = Messages.TryWithResourceRule_description;
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_7;
	}

	@Override
	protected DocumentChange applyRuleImpl(ICompilationUnit workingCopy)
			throws ReflectiveOperationException, JavaModelException {
		
		/*
		 * The TryWithResourceRule has to be applied twice. 
		 * 
		 * See: 
		 * FIXME SIM-396: Make all changes of the TryWithResourceRule visible
		 */
		DocumentChange tmp1 = super.applyRuleImpl(workingCopy);
		DocumentChange tmp2 = super.applyRuleImpl(workingCopy);
		
		return null == tmp2 ? tmp1 : tmp2;
	}
	
	
}
