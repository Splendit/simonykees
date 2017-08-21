package at.splendit.simonykees.core.visitor.renaming;

import java.util.List;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.text.edits.TextEditGroup;

/**
 * 
 * @author Ardit Ymeri
 * @since 2.1.0
 *
 */
public interface FieldMetadata {

	/**
	 * 
	 * @return
	 */
	CompilationUnit getCompilationUnit();

	/**
	 * 
	 * @return
	 */
	VariableDeclarationFragment getFieldDeclaration();

	/**
	 * 
	 * @return
	 */
	List<ReferenceSearchMatch> getReferences();

	/**
	 * 
	 * @return
	 */
	String getNewIdentifier();
	
	/**
	 * 
	 * @return
	 */
	TextEditGroup getTextEditGroup();
}
