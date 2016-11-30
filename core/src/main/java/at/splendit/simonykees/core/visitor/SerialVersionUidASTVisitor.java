package at.splendit.simonykees.core.visitor;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;

/**
 * Checks if the serialversionUID is static and final and adds the modifier if
 * absent
 * 
 * @author Martin Huter
 * @since 0.9.2
 *
 */
public class SerialVersionUidASTVisitor extends AbstractCompilationUnitASTVisitor {

	private static Integer COLLECTION_KEY = 1;
	private static String COLLECTION_FULLY_QUALLIFIED_NAME = "java.util.Collection"; //$NON-NLS-1$

	public SerialVersionUidASTVisitor() {
		super();
		this.fullyQuallifiedNameMap.put(COLLECTION_KEY,
				generateFullyQuallifiedNameList(COLLECTION_FULLY_QUALLIFIED_NAME));
	}
	
	@Override
	public boolean visit(CompilationUnit node){
		
		return true;
	}
}
