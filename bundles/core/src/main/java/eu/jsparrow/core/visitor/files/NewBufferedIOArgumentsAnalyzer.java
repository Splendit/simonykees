package eu.jsparrow.core.visitor.files;

import static eu.jsparrow.rules.common.util.ClassRelationUtil.isContentOfType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;

/**
 * Analyzes the arguments of a {@link ClassInstanceCreation} where the following
 * requirements must be fulfilled:
 * <ul>
 * <li>It must be possible to extract a list of path string expressions from the
 * arguments of the constructor mentioned above.</li>
 * <li>Additionally, an optional {@link java.nio.charset.Charset}-argument may
 * be extracted.</li>
 * </ul>
 * 
 * 
 * @see UseFilesBufferedReaderASTVisitor
 * @see UseFilesBufferedWriterASTVisitor
 * @see UseFilesWriteStringASTVisitor
 * 
 * @since 3.21.0
 *
 */
class NewBufferedIOArgumentsAnalyzer {
	private List<Expression> pathExpressions = new ArrayList<>();
	private Expression charsetExpression;

	/**
	 * Checks if the arguments of the {@link ClassInstanceCreation} represent
	 * the expected parameters for constructors of {@link java.io.FileReader} or
	 * {@link java.io.FileWriter}.
	 * 
	 * @param newInstanceCreation
	 * @return {@code true} if the arguments meet the requirements mentioned
	 *         above.
	 */
	public boolean analyzeInitializer(ClassInstanceCreation newInstanceCreation) {

		List<Expression> arguments = ASTNodeUtil.convertToTypedList(newInstanceCreation.arguments(), Expression.class);
		if (arguments.isEmpty()) {
			return false;
		}

		int argumentsSize = arguments.size();
		if (argumentsSize > 2) {
			return false;
		}

		if (argumentsSize == 2) {
			Expression secondArgument = arguments.get(1);
			ITypeBinding secondArgType = secondArgument.resolveTypeBinding();
			if (!isContentOfType(secondArgType, FilesConstants.CHARSET_QUALIFIED_NAME)) {
				return false;
			}
			this.charsetExpression = secondArgument;
		}

		Expression firstArgument = arguments.get(0);
		if (isContentOfType(firstArgument.resolveTypeBinding(), FilesConstants.STRING_QUALIFIED_NAME)) {
			pathExpressions.add(firstArgument);
			return true;
		} else if (ClassRelationUtil.isNewInstanceCreationOf(firstArgument, FilesConstants.FILE_QUALIFIED_NAME)) {
			ClassInstanceCreation fileInstanceCreation = (ClassInstanceCreation) firstArgument;
			List<Expression> fileArgs = ASTNodeUtil.convertToTypedList(fileInstanceCreation.arguments(),
					Expression.class);
			pathExpressions.addAll(fileArgs);
			return fileArgs
				.stream()
				.map(Expression::resolveTypeBinding)
				.allMatch(fileArgType -> isContentOfType(fileArgType, FilesConstants.STRING_QUALIFIED_NAME));
		}
		return false;
	}

	List<Expression> getPathExpressions() {
		return pathExpressions;
	}

	Optional<Expression> getCharsetExpression() {
		return Optional.ofNullable(charsetExpression);
	}
}
