package at.splendit.simonykees.core.visitor.renaming;

import java.util.List;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.search.SearchMatch;

/**
 * 
 * @author Ardit Ymeri
 * @since 2.1.0
 *
 */
public interface FieldDeclarationMetadata {

	CompilationUnit getCompilationUnit();

	VariableDeclarationFragment getFieldDeclaration();

	List<SearchMatch> getReferences();

	String getNewIdentifier();
}
