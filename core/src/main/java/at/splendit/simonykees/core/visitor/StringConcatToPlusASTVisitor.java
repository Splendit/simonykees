package at.splendit.simonykees.core.visitor;

/**
 * Removes all StringVariable.concat(Parameter) and transforms it into an
 * Infixoperation StringVariable + Parameter.
 * 
 * ex.: a.concat(b) -> a + b
 * 		a.concat(b.concat(c) -> a + b + c
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
public class StringConcatToPlusASTVisitor extends AbstractCompilationUnitASTVisitor {

	private static final Integer STRING_KEY = 1;
	private static final String STRING_FULLY_QUALLIFIED_NAME = "java.lang.String"; //$NON-NLS-1$

	public StringConcatToPlusASTVisitor() {
		super();
		this.fullyQuallifiedNameMap.put(STRING_KEY, generateFullyQuallifiedNameList(STRING_FULLY_QUALLIFIED_NAME));
	}

}
