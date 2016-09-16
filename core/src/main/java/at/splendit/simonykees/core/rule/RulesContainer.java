package at.splendit.simonykees.core.rule;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;

import at.splendit.simonykees.core.visitor.BracketsToControlASTVisitor;
import at.splendit.simonykees.core.visitor.StringUtilsASTVisitor;
import at.splendit.simonykees.core.visitor.arithmetic.ArithmethicAssignmentASTVisitor;
import at.splendit.simonykees.core.visitor.tryWithResource.MultiCatchASTVisitor;
import at.splendit.simonykees.core.visitor.tryWithResource.TryWithResourceASTVisitor;

public class RulesContainer {

	private RulesContainer() {
		// hiding the default constructor
	}

	public static List<RefactoringRule<? extends ASTVisitor>> getAllRules() {
		return Arrays.asList(
				new ArithmethicAssignmentRule(ArithmethicAssignmentASTVisitor.class),
				new TryWithResourceRule(TryWithResourceASTVisitor.class),
				new StringUtilsRule(StringUtilsASTVisitor.class),
				new MultiCatchRule(MultiCatchASTVisitor.class),
				new BracketsToControlRule(BracketsToControlASTVisitor.class)
				);
	}

}
