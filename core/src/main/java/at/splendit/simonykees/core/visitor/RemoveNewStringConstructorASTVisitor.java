package at.splendit.simonykees.core.visitor;

/**
 * Finds all class instantiations of string over no input parameter or string
 * because its an useless construction. The wrapping of the string is resolved
 * by removing the constructor and replacing it with the parameter string The
 * wrapping of no parameter is resolved by replacing the constructor with an
 * empty string
 * 
 * @author Martin Huter
 * @since 0.9.2
 *
 */
public class RemoveNewStringConstructorASTVisitor extends AbstractCompilationUnitASTVisitor {

	private static final Integer STRING_KEY = 1;
	private static final String STRING_FULLY_QUALLIFIED_NAME = "java.lang.String"; //$NON-NLS-1$

	public RemoveNewStringConstructorASTVisitor() {
		super();
		this.fullyQuallifiedNameMap.put(STRING_KEY, generateFullyQuallifiedNameList(STRING_FULLY_QUALLIFIED_NAME));
	}
}
