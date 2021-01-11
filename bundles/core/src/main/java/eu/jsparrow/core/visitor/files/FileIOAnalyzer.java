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

/**
 * Analyzes whether the declaration of a {@link java.io.FileReader} or a
 * {@link java.io.FileWriter} satisfies the preconditions for replacing, as
 * shown in the following example with a {@link java.io.FileReader}:
 * 
 * <pre>
 * try (FileReader fileReader = new FileReader(new File("path/to/file"));
 * 		BufferedReader buffer = new BufferedReader(fileReader)) {
 * }
 * </pre>
 * 
 * by:
 * 
 * <pre>
 *  try(BufferedReader buffer = Files.newBufferedReader(Paths.get("pat/to/file"), Charset.defaultCharset()) {}
 * </pre>
 * 
 * @see UseFilesBufferedReaderASTVisitor
 * @see UseFilesBufferedWriterASTVisitor
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

		ClassInstanceCreation fileIOCreation = FilesUtil
			.findClassInstanceCreationAsInitializer(fragmentDeclaringFileIO, fileIOClassQualifiedName)
			.orElse(null);

		if (fileIOCreation == null) {
			return false;
		}

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
			if (!isContentOfType(charsetBinding, java.nio.charset.Charset.class.getName())) {
				return false;
			}
			this.charsetExpression = charset;
		}

		return true;
	}

	private boolean isStringExpression(Expression expression) {
		ITypeBinding typeBinding = expression.resolveTypeBinding();
		boolean isString = isContentOfType(typeBinding, java.lang.String.class.getName());
		if (isString) {
			this.pathExpressions = new ArrayList<>();
			this.pathExpressions.add(expression);
		}
		return isString;
	}

	private boolean isFileInstanceCreation(Expression expression) {
		boolean isNewInstanceCreation = isNewInstanceCreationOf(expression, java.io.File.class.getName());
		if (!isNewInstanceCreation) {
			return false;
		}
		ClassInstanceCreation fileInstanceCreation = (ClassInstanceCreation) expression;
		List<Expression> arguments = convertToTypedList(fileInstanceCreation.arguments(), Expression.class);
		boolean allStrings = arguments
			.stream()
			.allMatch(argument -> isContentOfType(argument.resolveTypeBinding(), java.lang.String.class.getName()));
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
