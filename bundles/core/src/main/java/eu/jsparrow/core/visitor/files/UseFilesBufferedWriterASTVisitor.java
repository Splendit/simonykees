package eu.jsparrow.core.visitor.files;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

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

	private static final String BUFFERED_WRITER_QUALIFIED_NAME = java.io.BufferedWriter.class.getName();

	@Override
	public boolean visit(VariableDeclarationFragment fragment) {

		ClassInstanceCreation newBufferedWriter = findClassInstanceCreationAsInitializer(fragment,
				BUFFERED_WRITER_QUALIFIED_NAME);
		if (newBufferedWriter == null) {
			return true;
		}
		Expression bufferedWriterArg = findFirstArgumentOfType(newBufferedWriter, java.io.FileWriter.class.getName());
		if (bufferedWriterArg == null) {
			return true;
		}
		return super.visit(fragment);
	}

}
