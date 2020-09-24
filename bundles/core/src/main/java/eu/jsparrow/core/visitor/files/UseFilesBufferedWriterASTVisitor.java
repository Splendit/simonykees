package eu.jsparrow.core.visitor.files;

import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;

/**
 * Replaces the initializations of {@link java.io.BufferedWriter} objects with
 * the non-blocking alternative
 * {@link java.nio.file.Files#newBufferedWriter(java.nio.file.Path, java.nio.charset.Charset)}.
 * 
 * For example, the following code:
 * <p>
 * {@code BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File("path/to/file")));}
 * <p>
 * is transformed to:
 * <p>
 * {@code BufferedWriter bufferedWriter = Files.newBufferedWriter(Paths.get("path/to/file"), Charset.defaultCharset());}
 * <p>
 * 
 * @since 3.22.0
 *
 */
public class UseFilesBufferedWriterASTVisitor extends AbstractUseFilesMethodsASTVisitor {

	@Override
	public boolean visit(VariableDeclarationFragment fragment) {
		return super.visit(fragment);
	}

}
