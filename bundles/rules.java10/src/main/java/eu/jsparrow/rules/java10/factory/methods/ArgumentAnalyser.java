package eu.jsparrow.rules.java10.factory.methods;

import static java.util.Collections.unmodifiableList;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import eu.jsparrow.rules.common.util.ClassRelationUtil;

/**
 * The base class for analyzing the argument of
 * {@code Collections.unmodifiableList}, {@code Collections.unmodifiableSet} and
 * {@code Collections.unmodifiableMap}.
 * 
 * @since 3.6.0
 *
 * @param <T>
 *            type of the expression analyzed. Subtype of {@link Expression}.
 */
abstract class ArgumentAnalyser<T extends Expression> {

	protected static final List<String> collectionTypes = unmodifiableList(Arrays.asList(java.util.List.class.getName(),
			java.util.Set.class.getName(), java.util.Map.class.getName()));

	protected static final String ADD = "add"; //$NON-NLS-1$
	protected static final String PUT = "put"; //$NON-NLS-1$
	protected List<Expression> elements;

	/**
	 * Analyzes the argument of {@code Collections.unmodifiable...}. Verifies
	 * the precondition for transforming to Factory methods for collections.
	 * Saves the elements of the collection and the nodes to be removed after
	 * the transformation.
	 * 
	 * @param argument
	 *            an {@link Expression} representing the argument of
	 *            {@code Collections.unmodifiable...}.
	 */
	public abstract void analyzeArgument(T argument);

	/**
	 * 
	 * @return the list of elements to be for the factory method
	 *         {@code List/Set/Map.of(..)}, or {@code null} if the
	 *         transformation is not possible.
	 */
	public List<Expression> getElements() {
		return elements;
	}

	/**
	 * 
	 * @return the list of nodes to be removed after the transformation.
	 */
	public List<ExpressionStatement> getReplacedStatements() {
		return Collections.emptyList();
	}

	/**
	 * 
	 * @return a list with at most one element representing the
	 *         {@link VariableDeclarationFragment} to be removed after
	 *         transformation.
	 */
	public List<VariableDeclarationFragment> getNameDeclaration() {
		return Collections.emptyList();
	}

	protected String findInsertMethodName(ITypeBinding type) {
		return ClassRelationUtil.isInheritingContentOfTypes(type,
				Collections.singletonList(java.util.Map.class.getName()))
				|| ClassRelationUtil.isContentOfTypes(type, Collections.singletonList(java.util.Map.class.getName()))
						? PUT
						: ADD;
	}

	/**
	 * 
	 * @return if the assignment statement
	 *         {@code name = Collections.unmodifiableList/Set/Map(name)} must be
	 *         replaced with a variable declaration statement
	 *         {@code List/Set/Map<..> name = List/Set/Map.of(name);}
	 */
	public boolean requiresNewDeclaration() {
		return false;
	}
}
