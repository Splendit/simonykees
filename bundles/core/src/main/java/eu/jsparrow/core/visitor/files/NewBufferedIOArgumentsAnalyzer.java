package eu.jsparrow.core.visitor.files;

import static eu.jsparrow.rules.common.util.ClassRelationUtil.isContentOfType;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;

/**
 * An analyzer for the arguments of new {@link BufferedReader}s and
 * {@link BufferedWriter}s initializers.
 * 
 * 
 * 
 * @see UseFilesBufferedReaderASTVisitor
 * @see UseFilesBufferedWriterASTVisitor
 * 
 * @since 3.21.0
 *
 */
class NewBufferedIOArgumentsAnalyzer {

	private List<Expression> pathExpressions = new ArrayList<>();
	private Expression charsetExpression;

	/**
	 * Checks if the arguments of the {@link ClassInstanceCreation} represent
	 * the expected parameters for constructors of {@link FileReader} or
	 * {@link FileWriter}.
	 * 
	 * @param newInstanceCreation
	 * @return
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
			if (!isContentOfType(secondArgType, java.nio.charset.Charset.class.getName())) {
				return false;
			}
			this.charsetExpression = secondArgument;
		}

		Expression firstArgument = arguments.get(0);
		if (isContentOfType(firstArgument.resolveTypeBinding(), java.lang.String.class.getName())) {
			pathExpressions.add(firstArgument);
			return true;
		} else if (ClassRelationUtil.isNewInstanceCreationOf(firstArgument, java.io.File.class.getName())) {
			ClassInstanceCreation fileInstanceCreation = (ClassInstanceCreation) firstArgument;
			List<Expression> fileArgs = ASTNodeUtil.convertToTypedList(fileInstanceCreation.arguments(),
					Expression.class);
			pathExpressions.addAll(fileArgs);
			return fileArgs
				.stream()
				.map(Expression::resolveTypeBinding)
				.allMatch(fileArgType -> isContentOfType(fileArgType, java.lang.String.class.getName()));

		}
		return false;
	}

	TransformationData createTransformationData(ClassInstanceCreation newBufferedIO) {
		if (charsetExpression != null) {
			return new TransformationData(newBufferedIO, pathExpressions, charsetExpression);
		}
		return new TransformationData(newBufferedIO, pathExpressions);
	}
}
