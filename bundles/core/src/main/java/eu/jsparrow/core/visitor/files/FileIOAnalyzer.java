package eu.jsparrow.core.visitor.files;

import static eu.jsparrow.rules.common.util.ASTNodeUtil.convertToTypedList;
import static eu.jsparrow.rules.common.util.ClassRelationUtil.isContentOfType;
import static eu.jsparrow.rules.common.util.ClassRelationUtil.isNewInstanceCreationOf;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import eu.jsparrow.rules.common.util.ClassRelationUtil;

/**
 * Analyzes a {@link VariableDeclarationFragment} which is assumed to be
 * declared in a TWR-header. Furthermore, the following requirements must be
 * fulfilled:
 * <ul>
 * <li>If this class is used to analyze a file input resource, then the
 * initializer of the declaration fragment must be an invocation of a
 * constructor of {@link java.io.FileReader}, and if this class is used to
 * analyze a file output resource, then a constructor of
 * {@link java.io.FileWriter} is expected.</li>
 * <li>It must be possible to extract a list of path string expressions from the
 * arguments of the constructor mentioned above.</li>
 * <li>Additionally, an optional {@link java.nio.charset.Charset}-argument may
 * be extracted.</li>
 * </ul>
 * Example for use cases of {@link FileIOAnalyzer}: A {@link java.io.FileReader}
 * used by a {@link BufferedReader}, see the following code:
 * 
 * <pre>
 * try (FileReader fileReader = new FileReader(new File("path/to/file"));
 * 		BufferedReader buffer = new BufferedReader(fileReader)) {
 * }
 * </pre>
 * 
 * which can be: transformed to
 * 
 * <pre>
 *  try(BufferedReader buffer = Files.newBufferedReader(Paths.get("pat/to/file"), Charset.defaultCharset()) {}
 * </pre>
 * 
 * @see UseFilesBufferedReaderASTVisitor
 * @see UseFilesBufferedWriterASTVisitor
 * @see UseFilesWriteStringASTVisitor
 * 
 * @since 3.21.0
 *
 */
class FileIOAnalyzer {
	private Expression charsetExpression;
	private List<Expression> pathExpressions = new ArrayList<>();
	private final String fileIOClassQualifiedName;

	public FileIOAnalyzer(String fileIOClassQualifiedName) {
		this.fileIOClassQualifiedName = fileIOClassQualifiedName;
	}

	public boolean analyzeFileIO(VariableDeclarationFragment fragmentDeclaringFileIO) {

		Expression initializer = fragmentDeclaringFileIO.getInitializer();
		if (!ClassRelationUtil.isNewInstanceCreationOf(initializer, fileIOClassQualifiedName)) {
			return false;
		}

		ClassInstanceCreation fileIOCreation = (ClassInstanceCreation) initializer;

		List<Expression> arguments = convertToTypedList(fileIOCreation.arguments(), Expression.class);
		int argumentSize = arguments.size();
		if (argumentSize == 0 || argumentSize > 2) {
			return false;
		}
		Expression file = arguments.get(0);
		if (!isFileInstanceCreation(file) && !isStringExpression(file)) {
			return false;
		}

		if (argumentSize == 2) {
			Expression charset = arguments.get(1);
			ITypeBinding charsetBinding = charset.resolveTypeBinding();
			if (!isContentOfType(charsetBinding, FilesConstants.CHARSET_QUALIFIED_NAME)) {
				return false;
			}
			this.charsetExpression = charset;
		}
		return true;
	}

	private boolean isStringExpression(Expression expression) {
		ITypeBinding typeBinding = expression.resolveTypeBinding();
		boolean isString = isContentOfType(typeBinding, FilesConstants.STRING_QUALIFIED_NAME);
		if (isString) {
			this.pathExpressions = new ArrayList<>();
			this.pathExpressions.add(expression);
		}
		return isString;
	}

	private boolean isFileInstanceCreation(Expression expression) {
		boolean isNewInstanceCreation = isNewInstanceCreationOf(expression, FilesConstants.FILE_QUALIFIED_NAME);
		if (!isNewInstanceCreation) {
			return false;
		}
		ClassInstanceCreation fileInstanceCreation = (ClassInstanceCreation) expression;
		List<Expression> arguments = convertToTypedList(fileInstanceCreation.arguments(), Expression.class);
		boolean allStrings = arguments
			.stream()
			.allMatch(argument -> isContentOfType(argument.resolveTypeBinding(), FilesConstants.STRING_QUALIFIED_NAME));
		if (allStrings) {
			this.pathExpressions = new ArrayList<>();
			this.pathExpressions.addAll(arguments);
		}
		return allStrings;
	}

	public Optional<Expression> getCharset() {
		return Optional.ofNullable(charsetExpression);
	}

	public List<Expression> getPathExpressions() {
		return pathExpressions;
	}
}
