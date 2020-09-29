package eu.jsparrow.core.visitor.files;

/**
 * Replaces the initializations of {@link java.io.BufferedReader} objects with
 * the non-blocking alternative
 * {@link java.nio.file.Files#newBufferedReader(java.nio.file.Path, java.nio.charset.Charset)}.
 * 
 * For example, the following code:
 * <p>
 * {@code BufferedReader bufferedReader = new BufferedReader(new FileReader(new File("path/to/file")));}
 * <p>
 * is transformed to:
 * <p>
 * {@code BufferedReader bufferedReader = Files.newBufferedReader(Paths.get("path/to/file"), Charset.defaultCharset());}
 * <p>
 * 
 * @since 3.21.0
 *
 */
public class UseFilesBufferedReaderASTVisitor extends AbstractUseFilesMethodsASTVisitor {

	public UseFilesBufferedReaderASTVisitor() {
		super(java.io.BufferedReader.class.getName(), java.io.FileReader.class.getName(), "newBufferedReader"); //$NON-NLS-1$
	}
}
