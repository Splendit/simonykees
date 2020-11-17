package eu.jsparrow.core.visitor.files;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;

class FilesUtil {

	private FilesUtil() {
		/*
		 * Hide default constructor.
		 */
	}

	/**
	 * This method looks for a {@link VariableDeclarationFragment} within the
	 * resources of a given {@link TryStatement} which is referenced by the
	 * specified {@link SimpleName}.
	 * 
	 * @return an {@link Optional} storing the
	 *         {@link VariableDeclarationFragment} corresponding to the given
	 *         {@link SimpleName} if such a resource has been found, otherwise
	 *         an empty {@link Optional}.
	 */
	static Optional<VariableDeclarationFragment> findVariableDeclarationFragmentAsResource(SimpleName bufferedIOArg,
			TryStatement tryStatement) {
		List<VariableDeclarationExpression> resources = ASTNodeUtil
			.convertToTypedList(tryStatement.resources(), VariableDeclarationExpression.class);
		return resources.stream()
			.flatMap(resource -> ASTNodeUtil
				.convertToTypedList(resource.fragments(), VariableDeclarationFragment.class)
				.stream())
			.filter(resource -> resource.getName()
				.getIdentifier()
				.equals((bufferedIOArg).getIdentifier()))
			.findFirst();
	}

	static Optional<ClassInstanceCreation> findClassInstanceCreationAsInitializer(VariableDeclarationFragment fragment,
			String qualifiedTypeName) {

		Expression initializer = fragment.getInitializer();
		if (ClassRelationUtil.isNewInstanceCreationOf(initializer, qualifiedTypeName)) {
			return Optional.of((ClassInstanceCreation) initializer);
		}
		return Optional.empty();
	}

	/**
	 * 
	 * @param classInstanceCreation
	 *            expecting a {@link ClassInstanceCreation} of either
	 *            {@link java.io.BufferedReader} or
	 *            {@link java.io.BufferedWriter}
	 * @param fileIOQualifiedTypeName
	 *            expecting the qualified name of either
	 *            {@link java.io.FileReader} or {@link java.io.FileWriter}
	 * @return an {@link java.util.Optional} storing the argument of the class
	 *         instance creation if it has exactly one argument of the required
	 *         type, otherwise an empty {@link java.util.Optional}.
	 */
	static Optional<Expression> findBufferedIOArgument(ClassInstanceCreation classInstanceCreation,
			String fileIOQualifiedTypeName) {

		List<Expression> newBufferedIOArgs = ASTNodeUtil.convertToTypedList(classInstanceCreation.arguments(),
				Expression.class);
		if (newBufferedIOArgs.size() != 1) {
			return Optional.empty();
		}
		Expression bufferedIOArg = newBufferedIOArgs.get(0);
		ITypeBinding firstArgType = bufferedIOArg.resolveTypeBinding();
		if (!ClassRelationUtil.isContentOfType(firstArgType, fileIOQualifiedTypeName)) {
			return Optional.empty();
		}
		return Optional.of(bufferedIOArg);
	}
}
