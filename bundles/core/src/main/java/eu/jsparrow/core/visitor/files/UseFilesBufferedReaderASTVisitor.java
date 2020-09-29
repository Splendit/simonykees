package eu.jsparrow.core.visitor.files;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

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

	private static final String BUFFERED_READER_QUALIFIED_NAME = java.io.BufferedReader.class.getName();
	private static final String NEW_BUFFERED_READER = "newBufferedReader"; //$NON-NLS-1$

	@Override
	public boolean visit(VariableDeclarationFragment fragment) {

		ClassInstanceCreation newBufferedReader = findClassInstanceCreationAsInitializer(fragment,
				BUFFERED_READER_QUALIFIED_NAME);
		if (newBufferedReader == null) {
			return true;
		}
		Expression bufferedReaderArg = findFirstArgumentOfType(newBufferedReader, java.io.FileReader.class.getName());
		if (bufferedReaderArg == null) {
			return true;
		}

		NewBufferedIOArgumentsAnalyzer newBufferedIOArgumentsAnalyzer = new NewBufferedIOArgumentsAnalyzer();
		TransformationData transformationData = null;
		if (bufferedReaderArg.getNodeType() == ASTNode.CLASS_INSTANCE_CREATION
				&& newBufferedIOArgumentsAnalyzer.analyzeInitializer((ClassInstanceCreation) bufferedReaderArg)) {

			List<Expression> pathExpressions = newBufferedIOArgumentsAnalyzer.getPathExpressions();
			Optional<Expression> optionalCharset = newBufferedIOArgumentsAnalyzer.getCharset();
			transformationData = new TransformationData(newBufferedReader, pathExpressions, optionalCharset);

		} else if (isDeclarationInTWRHeader(fragment, bufferedReaderArg)) {
			FileIOAnalyzer fileReaderAnalyzer = new FileIOAnalyzer(java.io.FileReader.class.getName());
			transformationData = createAnalysisDataUsingFileIOResource(fragment, newBufferedReader, bufferedReaderArg,
					fileReaderAnalyzer);
		}

		if (transformationData != null) {
			transform(transformationData, NEW_BUFFERED_READER);
		}
		return true;
	}
}
