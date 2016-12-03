package at.splendit.simonykees.core.visitor;

/**
 * Every usage of the function {@link Object#toString()} on an java object is
 * removed, if it is used on an element with the type String
 * 
 * @author Martin Huter
 * @since 0.9.2
 *
 */
public class RemoveToStringOnStringASTVisitor extends AbstractCompilationUnitASTVisitor {

	private static Integer STRING_KEY = 1;
	private static String STRING_FULLY_QUALLIFIED_NAME = "java.lang.String"; //$NON-NLS-1$

	public RemoveToStringOnStringASTVisitor() {
		super();
		this.fullyQuallifiedNameMap.put(STRING_KEY, generateFullyQuallifiedNameList(STRING_FULLY_QUALLIFIED_NAME));
	}
	
}
