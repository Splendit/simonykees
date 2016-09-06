package at.splendit.simonykees.core.visitor;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;

import at.splendit.simonykees.core.visitor.arithmetic.ArithmethicAssignmentASTVisitor;
import at.splendit.simonykees.core.visitor.tryWithResource.TryWithResourceASTVisitor;

public class RulesContainer {

	private RulesContainer() {
		// hiding the default constructor
	}

	public static List<Class<? extends ASTVisitor>> getAllRules() {
		return Arrays.asList(
				ArithmethicAssignmentASTVisitor.class,
				TryWithResourceASTVisitor.class
				);
	}

}
