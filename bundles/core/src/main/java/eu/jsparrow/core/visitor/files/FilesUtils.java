package eu.jsparrow.core.visitor.files;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;

public class FilesUtils {

	private FilesUtils() {
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

}
