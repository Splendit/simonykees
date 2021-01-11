package eu.jsparrow.core.visitor.files;

import java.util.List;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.visitor.helper.LocalVariableUsagesVisitor;

/**
 * Helper class with a method to determine whether for the given
 * {@link TryStatement} a resource with a specified simple name can be found.
 * 
 *
 */
public class TryResourceAnalyzer {
	private TryStatement tryStatement;
	private VariableDeclarationExpression resource;
	private VariableDeclarationFragment resourceFragment;
	private Expression resourceInitializer;

	/**
	 * 
	 * @return true if in a given {@link TryStatement} a resource can be found
	 *         which has the specified name, otherwise false;
	 */
	boolean analyze(TryStatement tryStatement, SimpleName resourceNameExpected) {
		this.tryStatement = tryStatement;
		List<VariableDeclarationExpression> resources = ASTNodeUtil.convertToTypedList(tryStatement.resources(),
				VariableDeclarationExpression.class);
		for (VariableDeclarationExpression r : resources) {
			resource = r;
			List<VariableDeclarationFragment> resourceFragments = ASTNodeUtil.convertToTypedList(resource.fragments(),
					VariableDeclarationFragment.class);
			if (resourceFragments.size() == 1) {
				resourceFragment = resourceFragments.get(0);
				SimpleName resourceName = resourceFragment.getName();
				if (resourceName.getIdentifier()
					.equals(resourceNameExpected.getIdentifier())) {
					this.resourceInitializer = resourceFragment.getInitializer();
					if (this.resourceInitializer != null) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * 
	 * @return true if in a given {@link TryStatement} a resource can be found
	 *         which has the specified name and - after declaration - is used
	 *         exactly once.
	 */
	boolean analyzeResourceUsedOnce(TryStatement tryStatement, SimpleName expectedResourceUsage) {
		if (analyze(tryStatement, expectedResourceUsage)) {
			LocalVariableUsagesVisitor visitor = new LocalVariableUsagesVisitor(
					expectedResourceUsage);
			tryStatement.accept(visitor);
			List<SimpleName> usagesList = visitor.getUsages();
			SimpleName nameAtDeclaration = resourceFragment.getName();
			for (SimpleName usage : usagesList) {
				if (usage != nameAtDeclaration && usage != expectedResourceUsage) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	TryStatement getTryStatement() {
		return tryStatement;
	}

	VariableDeclarationExpression getResource() {
		return resource;
	}

	Expression getResourceInitializer() {
		return resourceInitializer;
	}

}
