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

abstract class ArgumentAnalyser<T extends Expression> {

	protected static final List<String> collectionTypes = unmodifiableList(Arrays.asList(java.util.List.class.getName(),
			java.util.Set.class.getName(), java.util.Map.class.getName()));

	protected static final String ADD = "add"; //$NON-NLS-1$
	protected static final String PUT = "put"; //$NON-NLS-1$
	protected List<Expression> elements;

	public abstract void analyzeArgument(T t);

	public List<Expression> getElements() {
		return elements;
	}

	public List<ExpressionStatement> getReplacedStatements() {
		return Collections.emptyList();
	}

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
	
	public boolean requiresNewDeclaration() {
		return false;
	}
}
